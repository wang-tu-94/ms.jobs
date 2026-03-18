package com.myproject.ms.jobs.jobs;

import com.myproject.ms.jobs.config.JobDescription;
import com.myproject.ms.jobs.repository.JobLogRepository;
import org.quartz.JobExecutionContext;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component("LogCleanupJob")
@JobDescription("Nettoyage automatique des vieux logs") // <-- Ajoute ceci
public class LogCleanupJob extends QuartzJobBean {
    private final JobLogRepository repository;

    public LogCleanupJob(JobLogRepository repository) {
        this.repository = repository;
    }

    @Override
    protected void executeInternal(JobExecutionContext context) {
        LocalDateTime limitDate = LocalDateTime.now().minusDays(30);
        repository.deleteByTimestampBefore(limitDate);
        System.out.println("Nettoyage des logs terminé pour les données avant le " + limitDate);
    }
}