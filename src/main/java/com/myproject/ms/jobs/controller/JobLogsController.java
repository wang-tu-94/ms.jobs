package com.myproject.ms.jobs.controller;

import com.myproject.ms.jobs.dto.JobLogDto;
import com.myproject.ms.jobs.service.JobLogsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/logs")
@Tag(name = "Logs", description = "Consultation et purge de l'historique d'exécution des jobs")
@SecurityRequirement(name = "bearerAuth")
public class JobLogsController {
    private final JobLogsService jobLogsService;

    public JobLogsController(JobLogsService jobLogsService) {
        this.jobLogsService = jobLogsService;
    }

    @Operation(
            summary = "Récupérer les logs d'un job (paginés)",
            description = "Retourne une page de logs pour un job donné, triée par défaut du plus récent au plus ancien."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Liste des logs récupérée"),
            @ApiResponse(responseCode = "401", description = "Non authentifié"),
            @ApiResponse(responseCode = "404", description = "Job non trouvé ou sans historique")
    })
    @GetMapping("/{jobName}")
    public ResponseEntity<Page<JobLogDto>> getLogs(
            @Parameter(description = "Nom du job dont on veut voir l'historique", example = "CleanTempFilesJob")
            @PathVariable String jobName,
            @Parameter(description = "Numéro de la page (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Nombre d'éléments par page", example = "20")
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(jobLogsService.getLogs(jobName, page, size));
    }

    @Operation(summary = "Récupérer un log spécifique", description = "Permet de consulter le détail complet d'une étape de log via son identifiant technique.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Détail du log trouvé"),
            @ApiResponse(responseCode = "400", description = "ID invalide (doit être un nombre)"),
            @ApiResponse(responseCode = "404", description = "Log non trouvé")
    })
    @GetMapping("/{jobName}/{jobLogId}")
    public ResponseEntity<JobLogDto> getById(
            @PathVariable String jobName,
            @Parameter(description = "ID technique du log", example = "1024")
            @PathVariable Long jobLogId) {
        return ResponseEntity.ok(jobLogsService.getLogById(jobLogId));
    }

    @Operation(summary = "Purger les logs d'un job", description = "Supprime définitivement tout l'historique lié à un job. Action irréversible.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Historique supprimé avec succès"),
            @ApiResponse(responseCode = "401", description = "Accès refusé")
    })
    @DeleteMapping("/{jobName}")
    public ResponseEntity<Void> deleteLogs(
            @Parameter(description = "Nom du job à purger")
            @PathVariable String jobName) {
        jobLogsService.clearLogs(jobName);
        return ResponseEntity.noContent().build();
    }
}
