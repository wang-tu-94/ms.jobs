package com.myproject.ms.jobs.dto;

import java.time.LocalDateTime;

public record JobLogDto(
        Long id,
        String jobName,
        LocalDateTime timestamp,
        String stepName,
        String status,
        String message
) {
}
