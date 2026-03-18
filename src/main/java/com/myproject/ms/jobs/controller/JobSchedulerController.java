package com.myproject.ms.jobs.controller;

import com.myproject.ms.jobs.dto.JobRequest;
import com.myproject.ms.jobs.service.JobSchedulerService;
import jakarta.validation.Valid;
import org.quartz.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/jobs/scheduler")
public class JobSchedulerController {
    private final JobSchedulerService jobSchedulerService;

    public JobSchedulerController(JobSchedulerService jobSchedulerService) {
        this.jobSchedulerService = jobSchedulerService;
    }

    @PostMapping("/schedule")
    public ResponseEntity<String> schedule(@Valid @RequestBody JobRequest req) throws SchedulerException {
        jobSchedulerService.schedule(req.name(), req.group(), req.cron(), req.jobType());
        return ResponseEntity.ok("Job planifié.");
    }

    @PostMapping("/{group}/{name}/pause")
    public ResponseEntity<Void> pause(@PathVariable String group, @PathVariable String name) throws SchedulerException {
        jobSchedulerService.pause(name, group);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{group}/{name}/resume")
    public ResponseEntity<Void> resume(@PathVariable String group, @PathVariable String name) throws SchedulerException {
        jobSchedulerService.resume(name, group);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{group}/{name}/cron")
    public ResponseEntity<String> updateCron(@PathVariable String group, @PathVariable String name, @RequestBody String newCron) throws SchedulerException {
        jobSchedulerService.updateJobCron(name, group, newCron.replace("\"", ""));

        return ResponseEntity.ok("CRON mis à jour. Prochaine exécution prise en compte.");
    }
}
