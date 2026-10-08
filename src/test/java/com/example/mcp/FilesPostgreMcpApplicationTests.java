//package com.example.mcp;
//
//import com.example.mcp.dto.FileInfoDto;
//import com.example.mcp.dto.FileOperationResultDto;
//import com.example.mcp.entity.FileRecordEntity;
//import com.example.mcp.service.DatabaseToolService;
//import com.example.mcp.service.FileToolService;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.io.TempDir;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.ActiveProfiles;
//
//import java.nio.file.Path;
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//@SpringBootTest
//@ActiveProfiles("h2")
//class FilesPostgreMcpApplicationTests {
//
//    @Autowired
//    private FileToolService fileToolService;
//
//    @Autowired
//    private DatabaseToolService databaseToolService;
//
//    @Test
//    void testFileAndDatabaseOperations(@TempDir Path tempDir) {
//        Path testFile = tempDir.resolve("sample.txt");
//
//        // 1. Write file
//        FileOperationResultDto writeResult = fileToolService.writeFile(
//                testFile.toString(), "Hello MCP Server World!\nLine 2 content.", false, true);
//        assertTrue(writeResult.success());
//        assertEquals("write_file", writeResult.operation());
//
//        // 2. Get file info
//        FileOperationResultDto infoResult = fileToolService.getFileInfo(testFile.toString());
//        assertTrue(infoResult.success());
//        FileInfoDto info = (FileInfoDto) infoResult.details();
//        assertEquals(2, info.lineCount());
//        assertFalse(info.isDirectory());
//
//        // 3. Read file
//        FileOperationResultDto readResult = fileToolService.readFile(testFile.toString(), null);
//        assertTrue(readResult.success());
//        assertTrue(((String) readResult.details()).contains("Hello MCP Server World!"));
//
//        // 4. List directory
//        FileOperationResultDto listResult = fileToolService.listDirectory(tempDir.toString(), "*.txt", false);
//        assertTrue(listResult.success());
//        List<?> files = (List<?>) listResult.details();
//        assertEquals(1, files.size());
//
//        // 5. Save record to DB
//        FileRecordEntity savedRecord = databaseToolService.saveFileRecord(
//                testFile.toString(), "Sample text file summary", "SUCCESS", "{\"tested\": true}");
//        assertNotNull(savedRecord.getId());
//        assertEquals("sample.txt", savedRecord.getFileName());
//        assertEquals("SUCCESS", savedRecord.getStatus());
//
//        // 6. Query records from DB
//        List<FileRecordEntity> records = databaseToolService.getFileRecords("SUCCESS", null);
//        assertFalse(records.isEmpty());
//        assertEquals(savedRecord.getId(), records.get(0).getId());
//
//        // 7. Delete file
//        FileOperationResultDto deleteResult = fileToolService.deleteFile(testFile.toString());
//        assertTrue(deleteResult.success());
//    }
//}
