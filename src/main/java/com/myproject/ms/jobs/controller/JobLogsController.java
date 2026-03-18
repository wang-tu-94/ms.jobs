package com.myproject.ms.jobs.controller;

import com.myproject.ms.jobs.dto.JobLogDto;
import com.myproject.ms.jobs.entity.JobLog;
import com.myproject.ms.jobs.service.JobLogsService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/logs")
public class JobLogsController {
    private final JobLogsService jobLogsService;

    public JobLogsController(JobLogsService jobLogsService) {
        this.jobLogsService = jobLogsService;
    }

    @GetMapping("/{jobName}")
    public ResponseEntity<Page<JobLogDto>> getLogs(
            @PathVariable String jobName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(jobLogsService.getLogs(jobName, page, size));
    }
    @GetMapping("/{jobName}/{jobLogId}")
    public ResponseEntity<JobLogDto> getById(@PathVariable String jobName, @PathVariable Long jobLogId) {
        return ResponseEntity.ok(jobLogsService.getLogById(jobLogId));
    }

    @DeleteMapping("/{jobName}")
    public ResponseEntity<Void> deleteLogs(@PathVariable String jobName) {
        jobLogsService.clearLogs(jobName);
        return ResponseEntity.noContent().build();
    }
}
