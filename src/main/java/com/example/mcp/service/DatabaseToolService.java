package com.example.mcp.service;

import com.example.mcp.dto.QueryResultDto;
import com.example.mcp.entity.FileRecordEntity;
import com.example.mcp.repository.FileRecordRepository;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

@Service
public class DatabaseToolService {

    private final FileRecordRepository fileRecordRepository;
    private final JdbcTemplate jdbcTemplate;

    public DatabaseToolService(FileRecordRepository fileRecordRepository, JdbcTemplate jdbcTemplate) {
        this.fileRecordRepository = fileRecordRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Tool(name = "save_file_record", description = "Save file metadata, content summary, and processing status into PostgreSQL database.")
    @Transactional
    public FileRecordEntity saveFileRecord(
            @ToolParam(description = "Path of the file being saved/recorded", required = true) String filePath,
            @ToolParam(description = "Summary or description of file contents", required = false) String contentSummary,
            @ToolParam(description = "Processing status (e.g. 'PROCESSED', 'SUCCESS', 'SAVED', 'ERROR')", required = false) String status,
            @ToolParam(description = "JSON formatted metadata string or extra details", required = false) String metadataJson) {

        Path path = Paths.get(filePath).toAbsolutePath().normalize();
        String fileName = path.getFileName().toString();
        long fileSize = 0L;

        if (Files.exists(path)) {
            File f = path.toFile();
            fileSize = f.length();
        }

        String recordStatus = (status != null && !status.isBlank()) ? status : "SAVED";

        FileRecordEntity entity = new FileRecordEntity(
                path.toString(),
                fileName,
                fileSize,
                recordStatus,
                contentSummary,
                metadataJson
        );

        return fileRecordRepository.save(entity);
    }

    @Tool(name = "get_file_records", description = "Retrieve saved file records from PostgreSQL database with optional status or keyword filters.")
    public List<FileRecordEntity> getFileRecords(
            @ToolParam(description = "Filter by status (e.g., 'SUCCESS', 'PROCESSED')", required = false) String statusFilter,
            @ToolParam(description = "Search keyword in file name", required = false) String searchKeyword) {

        if (statusFilter != null && !statusFilter.isBlank()) {
            return fileRecordRepository.findByStatusIgnoreCase(statusFilter);
        }

        if (searchKeyword != null && !searchKeyword.isBlank()) {
            return fileRecordRepository.findByFileNameContainingIgnoreCase(searchKeyword);
        }

        return fileRecordRepository.findAll();
    }

    @Tool(name = "execute_sql", description = "Execute custom SQL query (SELECT, INSERT, UPDATE, DELETE, CREATE TABLE) on PostgreSQL database.")
    public QueryResultDto executeSql(
            @ToolParam(description = "SQL query string to execute", required = true) String sqlQuery) {

        try {
            String trimmed = sqlQuery.trim();
            String lower = trimmed.toLowerCase();

            if (lower.startsWith("select") || lower.startsWith("with") || lower.startsWith("show")) {
                List<Map<String, Object>> results = jdbcTemplate.queryForList(sqlQuery);
                return new QueryResultDto(true, sqlQuery, results.size(), results, null);
            } else {
                int affected = jdbcTemplate.update(sqlQuery);
                return new QueryResultDto(true, sqlQuery, affected, List.of(), null);
            }
        } catch (Exception e) {
            return new QueryResultDto(false, sqlQuery, 0, null, e.getMessage());
        }
    }
}
