package com.myproject.ms.jobs.dto;

import jakarta.validation.constraints.NotBlank;

public record JobRequest(
        @NotBlank String name,
        @NotBlank String group,
        @NotBlank String cron, // Ex: "0 0/5 * * * ?" pour toutes les 5 mins
        @NotBlank String jobType
) {}
