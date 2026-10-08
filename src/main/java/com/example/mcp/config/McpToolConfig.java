package com.example.mcp.config;

import com.example.mcp.service.DatabaseToolService;
import com.example.mcp.service.FileToolService;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class McpToolConfig {

    @Bean
    public ToolCallbackProvider fileAndDatabaseTools(
            FileToolService fileToolService,
            DatabaseToolService databaseToolService) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(fileToolService, databaseToolService)
                .build();
    }
}
