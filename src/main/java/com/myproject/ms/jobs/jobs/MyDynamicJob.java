package com.myproject.ms.jobs.jobs;

import com.myproject.ms.jobs.config.JobDescription;
import com.myproject.ms.jobs.entity.JobLog;
import com.myproject.ms.jobs.service.JobLogsService;
import org.quartz.JobExecutionContext;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

import java.time.Instant;


@Component("ExampleJob")
@JobDescription("Exemple de job dynamique avec logs") // <-- Ajoute ceci
public class MyDynamicJob extends QuartzJobBean {

    private final JobLogsService JobLogsService;

    public MyDynamicJob(JobLogsService JobLogsService) {
        this.JobLogsService = JobLogsService;
    }

    @Override
    protected void executeInternal(JobExecutionContext context) {
        String jobName = context.getJobDetail().getKey().getName();

        saveLog(jobName, "START", "Début de l'exécution du job");

        try {
            Thread.sleep(2000); // Simulation
            saveLog(jobName, "STEP 1", "Traitement des données terminé");

        } catch (Exception e) {
            saveLog(jobName, "ERROR", e.getMessage());
        }

        saveLog(jobName, "END", "Job terminé avec succès");
    }

    private void saveLog(String name, String step, String msg) {
        JobLog log = new JobLog();
        log.setJobName(name);
        log.setStepName(step);
        log.setMessage(msg);
        log.setTimestamp(Instant.now());
        JobLogsService.save(log);
    }
}
