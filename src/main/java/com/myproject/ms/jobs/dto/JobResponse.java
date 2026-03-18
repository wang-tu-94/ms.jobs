package com.myproject.ms.jobs.dto;

import java.time.LocalDateTime;

public record JobResponse(
        String name,
        String group,
        String description,
        String cronExpression,
        String status,         // PAUSED, NORMAL, ERROR, etc.
        LocalDateTime nextFireTime
) {}