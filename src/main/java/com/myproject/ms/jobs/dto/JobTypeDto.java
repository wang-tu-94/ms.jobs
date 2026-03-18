package com.myproject.ms.jobs.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Représente un type de job disponible dans le système pour la planification")
public record JobTypeDto(
        @Schema(description = "Identifiant unique du type de job (nom du Bean Spring)", example = "ExampleJob")
        String id,
        @Schema(description = "Description lisible par l'utilisateur de ce que fait le job", example = "Exemple de job dynamique avec logs")
        String description
) {
}