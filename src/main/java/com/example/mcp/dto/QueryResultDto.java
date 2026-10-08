package com.example.mcp.dto;

import java.util.List;
import java.util.Map;

public record QueryResultDto(
        boolean success,
        String sql,
        int rowsAffected,
        List<Map<String, Object>> data,
        String errorMessage
) {}
