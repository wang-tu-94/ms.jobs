package com.myproject.ms.jobs.service;

import com.myproject.ms.jobs.dto.JobResponse;
import com.myproject.ms.jobs.exception.ApiException;
import com.myproject.ms.jobs.exception.NotFoundException;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class JobService {
    private final Scheduler scheduler;

    public JobService(Scheduler scheduler) {
        this.scheduler = scheduler;
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

            LocalDateTime nextFire = trigger
                    .map(Trigger::getNextFireTime)
                    .map(date -> date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime())
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
}
