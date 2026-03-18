package com.myproject.ms.jobs.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Détail d'une ligne de log d'exécution d'un job")
public record JobLogDto(
        @Schema(description = "ID technique unique du log", example = "150")
        Long id,

        @Schema(description = "Nom du job ayant généré ce log", example = "EmailSyncJob")
        String jobName,

        @Schema(description = "Date et heure précise de l'événement", example = "2026-03-18T14:30:00")
        LocalDateTime timestamp,

        @Schema(description = "Nom de l'étape au sein du job", example = "DATA_FETCHING")
        String stepName,

        @Schema(description = "Statut de l'étape", example = "SUCCESS", allowableValues = {"START", "INFO", "SUCCESS", "ERROR", "END"})
        String status,

        @Schema(description = "Message détaillé de l'événement", example = "1500 records fetched successfully from external API")
        String message
) {
}
