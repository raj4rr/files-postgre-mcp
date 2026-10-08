package com.example.mcp.service;

import com.example.mcp.dto.FileInfoDto;
import com.example.mcp.dto.FileOperationResultDto;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class FileToolService {

    @Tool(name = "list_directory", description = "List files and subdirectories inside a specified folder with optional glob filtering.")
    public FileOperationResultDto listDirectory(
            @ToolParam(description = "Absolute or relative folder path", required = true) String path,
            @ToolParam(description = "Glob pattern filter (e.g., '*.txt', '*.java', '*'), default is '*'", required = false) String pattern,
            @ToolParam(description = "Whether to list recursively in subdirectories", required = false) Boolean recursive) {

        try {
            Path dirPath = Paths.get(path).toAbsolutePath().normalize();
            if (!Files.exists(dirPath)) {
                return new FileOperationResultDto(false, "list_directory", path, "Directory does not exist", null);
            }
            if (!Files.isDirectory(dirPath)) {
                return new FileOperationResultDto(false, "list_directory", path, "Path is not a directory", null);
            }

            String globPattern = (pattern != null && !pattern.isBlank()) ? pattern : "*";
            boolean isRecursive = Boolean.TRUE.equals(recursive);

            List<FileInfoDto> fileList = new ArrayList<>();
            PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + globPattern);

            int maxDepth = isRecursive ? 20 : 1;
            try (Stream<Path> stream = Files.walk(dirPath, maxDepth)) {
                List<Path> paths = stream
                        .filter(p -> !p.equals(dirPath))
                        .filter(p -> matcher.matches(p.getFileName()))
                        .collect(Collectors.toList());

                for (Path p : paths) {
                    fileList.add(buildFileInfoDto(p));
                }
            }

            return new FileOperationResultDto(true, "list_directory", dirPath.toString(),
                    "Successfully retrieved " + fileList.size() + " entries", fileList);

        } catch (Exception e) {
            return new FileOperationResultDto(false, "list_directory", path, "Error listing directory: " + e.getMessage(), null);
        }
    }

    @Tool(name = "read_file", description = "Read text content from a file.")
    public FileOperationResultDto readFile(
            @ToolParam(description = "File path to read", required = true) String filePath,
            @ToolParam(description = "Optional maximum number of lines to read", required = false) Integer maxLines) {

        try {
            Path path = Paths.get(filePath).toAbsolutePath().normalize();
            if (!Files.exists(path)) {
                return new FileOperationResultDto(false, "read_file", filePath, "File does not exist", null);
            }
            if (Files.isDirectory(path)) {
                return new FileOperationResultDto(false, "read_file", filePath, "Path is a directory, not a file", null);
            }

            List<String> lines;
            try (Stream<String> lineStream = Files.lines(path, StandardCharsets.UTF_8)) {
                if (maxLines != null && maxLines > 0) {
                    lines = lineStream.limit(maxLines).collect(Collectors.toList());
                } else {
                    lines = lineStream.collect(Collectors.toList());
                }
            }

            String content = String.join("\n", lines);
            return new FileOperationResultDto(true, "read_file", path.toString(),
                    "Successfully read " + lines.size() + " lines", content);

        } catch (Exception e) {
            return new FileOperationResultDto(false, "read_file", filePath, "Error reading file: " + e.getMessage(), null);
        }
    }

    @Tool(name = "write_file", description = "Write or append content to a file. Parent folders are created automatically if missing.")
    public FileOperationResultDto writeFile(
            @ToolParam(description = "File path to write to", required = true) String filePath,
            @ToolParam(description = "Text content to write into file", required = true) String content,
            @ToolParam(description = "Append to existing file if true, otherwise replace", required = false) Boolean append,
            @ToolParam(description = "Overwrite if file already exists", required = false) Boolean overwrite) {

        try {
            Path path = Paths.get(filePath).toAbsolutePath().normalize();

            if (Files.exists(path) && !Boolean.TRUE.equals(overwrite) && !Boolean.TRUE.equals(append)) {
                return new FileOperationResultDto(false, "write_file", filePath,
                        "File already exists and neither 'overwrite' nor 'append' was specified.", null);
            }

            if (path.getParent() != null) {
                Files.createDirectories(path.getParent());
            }

            StandardOpenOption openOption = Boolean.TRUE.equals(append)
                    ? StandardOpenOption.APPEND
                    : StandardOpenOption.TRUNCATE_EXISTING;

            if (!Files.exists(path)) {
                Files.writeString(path, content, StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW);
            } else {
                Files.writeString(path, content, StandardCharsets.UTF_8, openOption);
            }

            FileInfoDto info = buildFileInfoDto(path);
            return new FileOperationResultDto(true, "write_file", path.toString(),
                    "File saved successfully (" + info.sizeBytes() + " bytes)", info);

        } catch (Exception e) {
            return new FileOperationResultDto(false, "write_file", filePath, "Error writing file: " + e.getMessage(), null);
        }
    }

    @Tool(name = "delete_file", description = "Delete a file at the specified path.")
    public FileOperationResultDto deleteFile(
            @ToolParam(description = "File path to delete", required = true) String filePath) {

        try {
            Path path = Paths.get(filePath).toAbsolutePath().normalize();
            if (!Files.exists(path)) {
                return new FileOperationResultDto(false, "delete_file", filePath, "File does not exist", null);
            }

            Files.delete(path);
            return new FileOperationResultDto(true, "delete_file", path.toString(), "File deleted successfully", null);

        } catch (Exception e) {
            return new FileOperationResultDto(false, "delete_file", filePath, "Error deleting file: " + e.getMessage(), null);
        }
    }

    @Tool(name = "get_file_info", description = "Retrieve metadata information for a specific file or folder.")
    public FileOperationResultDto getFileInfo(
            @ToolParam(description = "File or directory path", required = true) String filePath) {

        try {
            Path path = Paths.get(filePath).toAbsolutePath().normalize();
            if (!Files.exists(path)) {
                return new FileOperationResultDto(false, "get_file_info", filePath, "Path does not exist", null);
            }

            FileInfoDto info = buildFileInfoDto(path);
            return new FileOperationResultDto(true, "get_file_info", path.toString(), "File info fetched", info);

        } catch (Exception e) {
            return new FileOperationResultDto(false, "get_file_info", filePath, "Error getting file info: " + e.getMessage(), null);
        }
    }

    private FileInfoDto buildFileInfoDto(Path path) {
        try {
            File file = path.toFile();
            BasicFileAttributes attrs = Files.readAttributes(path, BasicFileAttributes.class);
            LocalDateTime lastModified = LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(attrs.lastModifiedTime().toMillis()), ZoneId.systemDefault());

            Integer lineCount = null;
            if (attrs.isRegularFile() && file.length() < 10_000_000) { // < 10MB
                try (Stream<String> lines = Files.lines(path, StandardCharsets.UTF_8)) {
                    lineCount = (int) lines.count();
                } catch (Exception ignored) {
                }
            }

            return new FileInfoDto(
                    path.getFileName().toString(),
                    path.toString(),
                    attrs.size(),
                    attrs.isDirectory(),
                    file.canRead(),
                    file.canWrite(),
                    lastModified,
                    lineCount
            );
        } catch (IOException e) {
            File file = path.toFile();
            return new FileInfoDto(path.getFileName().toString(), path.toString(), file.length(),
                    file.isDirectory(), file.canRead(), file.canWrite(), LocalDateTime.now(), null);
        }
    }
}
