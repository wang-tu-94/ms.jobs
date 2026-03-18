package com.myproject.ms.jobs.service;

import com.myproject.ms.jobs.exception.NotFoundException;
import org.quartz.*;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class JobSchedulerService {
    private final Scheduler scheduler;

    private final ApplicationContext context;

    public JobSchedulerService(Scheduler scheduler, ApplicationContext context) {
        this.scheduler = scheduler;
        this.context = context;
    }

    public void schedule(String name, String group, String cron, String jobType) throws SchedulerException {
        Class<? extends Job> jobClass = getJobClass(jobType);

        JobDetail jobDetail = JobBuilder.newJob(jobClass)
                .withIdentity(name, group)
                .storeDurably()
                .build();

        CronTrigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(name + "-trigger", group)
                .withSchedule(CronScheduleBuilder.cronSchedule(cron))
                .build();

        scheduler.scheduleJob(jobDetail, Set.of(trigger), true);
    }

    public void pause(String name, String group) throws SchedulerException {
        scheduler.pauseJob(JobKey.jobKey(name, group));
    }

    public void resume(String name, String group) throws SchedulerException {
        scheduler.resumeJob(JobKey.jobKey(name, group));
    }

    public void updateJobCron(String name, String group, String newCron) throws SchedulerException {
        TriggerKey triggerKey = TriggerKey.triggerKey(name + "-trigger", group);

        CronTrigger newTrigger = TriggerBuilder.newTrigger()
                .withIdentity(triggerKey)
                .withSchedule(CronScheduleBuilder.cronSchedule(newCron))
                .build();

        Date nextFireTime = scheduler.rescheduleJob(triggerKey, newTrigger);

        if (nextFireTime == null) {
            throw new NotFoundException("Job ou Trigger non trouvé");
        }
    }


    private Class<? extends Job> getJobClass(String jobType) {
        try {
            Object jobBean = context.getBean(jobType);
            if (jobBean instanceof Job) {
                return (Class<? extends Job>) jobBean.getClass();
            }
            throw new IllegalArgumentException("Le type " + jobType + " n'est pas un Job Quartz valide.");
        } catch (NoSuchBeanDefinitionException e) {
            throw new IllegalArgumentException("Job non trouvé : " + jobType);
        }
    }
}
