package com.myproject.ms.jobs.controller;

import com.myproject.ms.jobs.dto.JobResponse;
import com.myproject.ms.jobs.service.JobService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.quartz.SchedulerException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/jobs")
@Tag(name = "Jobs Management", description = "Consultation et suppression des définitions de jobs")
@SecurityRequirement(name = "bearerAuth")
public class JobController {

    private final JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    @Operation(
            summary = "Lister tous les jobs groupés",
            description = "Récupère l'intégralité des jobs présents dans le scheduler, organisés par nom de groupe."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Map retournée avec succès",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(description = "Clé : Nom du groupe, Valeur : Liste des jobs", example = "{ 'DEFAULT': [ {...} ] }")))
    })
    @GetMapping("/jobs")
    public ResponseEntity<Map<String, List<JobResponse>>> listAllGrouped() throws SchedulerException {
        return ResponseEntity.ok(jobService.getAllJobsGrouped());
    }

    @Operation(summary = "Lister les jobs d'un groupe", description = "Récupère uniquement les jobs appartenant au groupe spécifié.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Liste des jobs du groupe"),
            @ApiResponse(responseCode = "404", description = "Groupe non trouvé ou vide")
    })
    @GetMapping("/groups/{group}")
    public ResponseEntity<List<JobResponse>> listByGroup(
            @Parameter(description = "Nom du groupe Quartz", example = "REPORTS") @PathVariable String group) {
        return ResponseEntity.ok(jobService.getJobsByGroup(group));
    }

    @Operation(summary = "Obtenir les détails d'un job", description = "Récupère les métadonnées complètes d'un job spécifique (CRON, Statut, Prochaine exécution).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Détails du job récupérés"),
            @ApiResponse(responseCode = "404", description = "Le couple Nom/Groupe spécifié n'existe pas")
    })
    @GetMapping("/groups/{group}/jobs/{name}")
    public ResponseEntity<JobResponse> getOne(
            @Parameter(description = "Groupe du job") @PathVariable String group,
            @Parameter(description = "Nom du job") @PathVariable String name) throws SchedulerException {
        return ResponseEntity.ok(jobService.getJobDetails(name, group));
    }

    @Operation(summary = "Supprimer définitivement un job", description = "Arrête les triggers et supprime la définition du job de la base Quartz.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Job supprimé avec succès"),
            @ApiResponse(responseCode = "404", description = "Le job n'a pas pu être trouvé pour suppression")
    })
    @DeleteMapping("/groups/{group}/jobs/{name}")
    public ResponseEntity<String> deleteJob(
            @PathVariable String group,
            @PathVariable String name) throws SchedulerException {
        boolean deleted = jobService.deleteScheduledJob(name, group);
        return deleted ?
                ResponseEntity.ok("Job supprimé définitivement.") :
                ResponseEntity.status(HttpStatus.NOT_FOUND).body("Job non trouvé.");
    }
}
