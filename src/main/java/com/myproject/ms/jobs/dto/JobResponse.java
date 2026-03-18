package com.myproject.ms.jobs.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "État actuel et configuration d'un job dans le scheduler")
public record JobResponse(
        @Schema(description = "Nom du job", example = "CleanupTemporaryFiles")
        String name,

        @Schema(description = "Groupe du job", example = "MAINTENANCE")
        String group,

        @Schema(description = "Description fournie lors de la création du job", example = "Supprime les fichiers temporaires de plus de 24h")
        String description,

        @Schema(description = "Expression CRON actuellement active", example = "0 0/5 * * * ?")
        String cronExpression,

        @Schema(description = "Statut actuel du déclencheur (Trigger)",
                example = "NORMAL",
                allowableValues = {"NORMAL", "PAUSED", "COMPLETE", "ERROR", "BLOCKED", "NONE"})
        String status,

        @Schema(description = "Date et heure de la prochaine exécution prévue", example = "2026-03-18T14:35:00")
        LocalDateTime nextFireTime
) {}