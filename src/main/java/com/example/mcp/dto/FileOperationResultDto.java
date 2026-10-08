package com.example.mcp.dto;

public record FileOperationResultDto(
        boolean success,
        String operation,
        String filePath,
        String message,
        Object details
) {}
