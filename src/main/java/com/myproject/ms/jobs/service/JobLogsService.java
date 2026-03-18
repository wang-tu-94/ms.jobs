package com.myproject.ms.jobs.service;

import com.myproject.ms.jobs.dto.JobLogDto;
import com.myproject.ms.jobs.entity.JobLog;
import com.myproject.ms.jobs.exception.NotFoundException;
import com.myproject.ms.jobs.repository.JobLogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class JobLogsService {
    private final JobLogRepository logRepository;

    public JobLogsService(JobLogRepository logRepository) {
        this.logRepository = logRepository;
    }

    @Async
    public void save(JobLog jobLog) {
        logRepository.save(jobLog);
    }

    @Transactional(readOnly = true)
    public Page<JobLogDto> getLogs(String name, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("timestamp").descending());
        return logRepository.findByJobName(name, pageable).map(this::toDto);
    }

    @Transactional(readOnly = true)
    public JobLogDto getLogById(Long id) {
        return logRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new NotFoundException("Log " + id + " non trouvé"));
    }

    public void clearLogs(String name) {
        logRepository.deleteByJobName(name);
    }

    private JobLogDto toDto(JobLog entity) {
        if (entity == null) {
            return null;
        }

        return new JobLogDto(
                entity.getId(),
                entity.getJobName(),
                entity.getTimestamp(),
                entity.getStepName(),
                entity.getStatus(),
                entity.getMessage()
        );
    }
}
