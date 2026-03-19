package com.myproject.ms.jobs.jobs;

import com.myproject.ms.jobs.config.JobDescription;
import com.myproject.ms.jobs.config.JobName;
import com.myproject.ms.jobs.repository.JobLogRepository;
import org.quartz.JobExecutionContext;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;


@Component("LogCleanupJob")
@JobName("Job de nettoyage de logs")
@JobDescription("Nettoyage automatique des vieux logs") // <-- Ajoute ceci
public class LogCleanupJob extends QuartzJobBean {
    private final JobLogRepository repository;

    public LogCleanupJob(JobLogRepository repository) {
        this.repository = repository;
    }

    @Override
    protected void executeInternal(JobExecutionContext context) {
        Instant limitDate = Instant.now().minus(30, ChronoUnit.DAYS);
        repository.deleteByTimestampBefore(limitDate);
        System.out.println("Nettoyage des logs terminé pour les données avant le " + limitDate);
    }
}