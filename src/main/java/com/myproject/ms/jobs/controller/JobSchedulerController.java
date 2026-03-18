package com.myproject.ms.jobs.controller;

import com.myproject.ms.jobs.dto.CronUpdateRequest;
import com.myproject.ms.jobs.dto.JobRequest;
import com.myproject.ms.jobs.service.JobSchedulerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.quartz.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/jobs/scheduler")
@Tag(name = "Scheduler", description = "Pilotage du cycle de vie des jobs (CRON, Pause, Resume)")
@SecurityRequirement(name = "bearerAuth") // Indique que l'API nécessite un JWT
public class JobSchedulerController {
    private final JobSchedulerService jobSchedulerService;

    public JobSchedulerController(JobSchedulerService jobSchedulerService) {
        this.jobSchedulerService = jobSchedulerService;
    }

    @Operation(summary = "Planifier un nouveau job", description = "Crée un JobDetail et un CronTrigger dans Quartz. Si le job existe déjà, il est mis à jour.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Job planifié avec succès"),
            @ApiResponse(responseCode = "400", description = "Requête invalide (Expression CRON incorrecte ou type de job inconnu)"),
            @ApiResponse(responseCode = "500", description = "Erreur interne du moteur Quartz")
    })
    @PostMapping("/schedule")
    public ResponseEntity<Void> schedule(@Valid @RequestBody JobRequest req) throws SchedulerException {
        jobSchedulerService.schedule(req.name(), req.group(), req.cron(), req.jobType());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Mettre un job en pause", description = "Suspend l'exécution d'un job sans le supprimer. Les déclenchements prévus ne seront pas honorés tant que le job est en pause.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Job mis en pause"),
            @ApiResponse(responseCode = "404", description = "Job non trouvé")
    })
    @PostMapping("/{group}/{name}/pause")
    public ResponseEntity<Void> pause(
            @Parameter(description = "Nom du groupe de jobs", example = "MAILS")
            @PathVariable String group,
            @Parameter(description = "Nom unique du job", example = "newsletter-job")
            @PathVariable String name) throws SchedulerException {
        jobSchedulerService.pause(name, group);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Relancer un job", description = "Reprend l'exécution d'un job précédemment mis en pause.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Job relancé"),
            @ApiResponse(responseCode = "404", description = "Job non trouvé")
    })
    @PostMapping("/{group}/{name}/resume")
    public ResponseEntity<Void> resume(
            @Parameter(description = "Nom du groupe", example = "DEFAULT")
            @PathVariable String group,
            @Parameter(description = "Nom du job", example = "purge-log-job")
            @PathVariable String name) throws SchedulerException {
        jobSchedulerService.resume(name, group);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Modifier le CRON d'un job", description = "Met à jour uniquement le déclencheur (trigger) du job sans modifier sa définition.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Expression CRON mise à jour"),
            @ApiResponse(responseCode = "400", description = "Format CRON invalide"),
            @ApiResponse(responseCode = "404", description = "Job ou Trigger non trouvé")
    })
    @PatchMapping("/{group}/{name}/cron")
    public ResponseEntity<Void> updateCron(
            @PathVariable String group,
            @PathVariable String name,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Nouvelle expression CRON", content = @Content(schema = @Schema(implementation = String.class, example = "0 0/15 * * * ?")))
            @RequestBody CronUpdateRequest cronUpdateRequest) throws SchedulerException {

        jobSchedulerService.updateJobCron(name, group, cronUpdateRequest);
        return ResponseEntity.noContent().build();
    }
}
