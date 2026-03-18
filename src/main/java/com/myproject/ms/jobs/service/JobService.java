package com.myproject.ms.jobs.service;

import com.myproject.ms.jobs.config.JobDescription;
import com.myproject.ms.jobs.dto.JobResponse;
import com.myproject.ms.jobs.dto.JobTypeDto;
import com.myproject.ms.jobs.exception.ApiException;
import com.myproject.ms.jobs.exception.NotFoundException;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class JobService {
    private final Scheduler scheduler;

    private final ApplicationContext context;

    public JobService(Scheduler scheduler, ApplicationContext context) {
        this.scheduler = scheduler;
        this.context = context;
    }

    public boolean deleteScheduledJob(String name, String group) throws SchedulerException {
        JobKey jobKey = JobKey.jobKey(name, group);
        if (scheduler.checkExists(jobKey)) {
            return scheduler.deleteJob(jobKey);
        }
        return false;
    }

    public Map<String, List<JobResponse>> getAllJobsGrouped() throws SchedulerException {
        return scheduler.getJobGroupNames().stream()
                .collect(Collectors.toMap(
                        group -> group,
                        this::getJobsByGroup
                ));
    }

    public List<JobResponse> getJobsByGroup(String group) {
        try {
            return scheduler.getJobKeys(GroupMatcher.jobGroupEquals(group)).stream()
                    .map(this::mapToResponse)
                    .toList();
        } catch (SchedulerException e) {
            throw new ApiException("Erreur lors de la récupération du groupe : " + group, e);
        }
    }

    public JobResponse getJobDetails(String name, String group) throws SchedulerException {
        JobKey jobKey = JobKey.jobKey(name, group);
        if (!scheduler.checkExists(jobKey)) {
            throw new NotFoundException("Job " + name + " " + group + " not found");
        }
        return mapToResponse(jobKey);
    }

    private JobResponse mapToResponse(JobKey jobKey) {
        try {
            JobDetail jobDetail = scheduler.getJobDetail(jobKey);
            List<? extends Trigger> triggers = scheduler.getTriggersOfJob(jobKey);

            // On prend le premier trigger pour les infos de scheduling
            var trigger = triggers.stream().findFirst();

            String cron = trigger
                    .filter(t -> t instanceof CronTrigger)
                    .map(t -> ((CronTrigger) t).getCronExpression())
                    .orElse("N/A");

            Instant nextFire = trigger
                    .map(Trigger::getNextFireTime)
                    .map(date -> date.toInstant().atZone(ZoneId.systemDefault()).toInstant())
                    .orElse(null);

            String status = trigger
                    .map(t -> {
                        try { return scheduler.getTriggerState(t.getKey()).name(); }
                        catch (SchedulerException e) { return "UNKNOWN"; }
                    })
                    .orElse("NO_TRIGGER");

            return new JobResponse(
                    jobKey.getName(),
                    jobKey.getGroup(),
                    jobDetail.getDescription(),
                    cron,
                    status,
                    nextFire
            );
        } catch (SchedulerException e) {
            throw new RuntimeException("Erreur de mapping pour le job " + jobKey.getName(), e);
        }
    }

    public List<JobTypeDto> getAvailableJobTypes() {
        // Récupère les noms de tous les beans de type org.quartz.Job
        String[] beanNames = context.getBeanNamesForType(Job.class);

        return Arrays.stream(beanNames)
                .map(this::mapToJobTypeDto)
                .sorted(Comparator.comparing(JobTypeDto::id)) // Tri par description pour l'UX
                .toList();
    }

    /**
     * Méthode d'aide pour extraire la description d'un Bean
     */
    private JobTypeDto mapToJobTypeDto(String beanName) {
        try {
            // On récupère la classe réelle du Bean
            Class<?> beanClass = context.getType(beanName);

            // On cherche l'annotation @JobDescription
            String description = Optional.ofNullable(beanClass)
                    .filter(clazz -> clazz.isAnnotationPresent(JobDescription.class))
                    .map(clazz -> clazz.getAnnotation(JobDescription.class).value())
                    .orElse("Aucune description pour ce job (" + beanName + ")");

            return new JobTypeDto(beanName, description);
        } catch (Exception e) {
            // En cas d'erreur de parsing, on renvoie au moins l'ID
            return new JobTypeDto(beanName, "Erreur lors de la récupération de la description");
        }
    }
}
