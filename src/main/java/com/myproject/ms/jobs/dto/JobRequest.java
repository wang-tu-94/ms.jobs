package com.myproject.ms.jobs.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Requête de création ou mise à jour de planification d'un job")
public record JobRequest(
        @NotBlank
        @Schema(description = "Nom unique du job", example = "CleanupTemporaryFiles")
        String name,

        @NotBlank
        @Schema(description = "Groupe de classification du job", example = "MAINTENANCE")
        String group,

        @NotBlank
        @Schema(
                description = "Expression CRON Quartz (Format : Seconde Minute Heure Jour Mois Jour-Semaine)",
                example = "0 0/15 * * * ?"
        )
        String cron,

        @NotBlank
        @Schema(description = "Identifiant technique du Job (Nom du Bean Spring)", example = "LogCleanupJob")
        String jobType
) {}