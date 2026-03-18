package com.myproject.ms.jobs.controller;

import com.myproject.ms.jobs.dto.JobResponse;
import com.myproject.ms.jobs.service.JobService;
import org.quartz.SchedulerException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/jobs")
public class JobController {

    private final JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    @GetMapping("/jobs")
    public ResponseEntity<Map<String, List<JobResponse>>> listAllGrouped() throws SchedulerException {
        return ResponseEntity.ok(jobService.getAllJobsGrouped());
    }

    @GetMapping("/groups/{group}")
    public ResponseEntity<List<JobResponse>> listByGroup(@PathVariable String group) {
        return ResponseEntity.ok(jobService.getJobsByGroup(group));
    }

    @GetMapping("/groups/{group}/jobs/{name}")
    public ResponseEntity<JobResponse> getOne(@PathVariable String group, @PathVariable String name) throws SchedulerException {
        return ResponseEntity.ok(jobService.getJobDetails(name, group));
    }

    @DeleteMapping("/groups/{group}/jobs/{name}")
    public ResponseEntity<String> deleteJob(@PathVariable String group, @PathVariable String name) throws SchedulerException {
        boolean deleted = jobService.deleteScheduledJob(name, group);
        return deleted ?
                ResponseEntity.ok("Job supprimé définitivement.") :
                ResponseEntity.status(HttpStatus.NOT_FOUND).body("Job non trouvé.");
    }
}
