package com.myproject.ms.jobs.service;

import com.myproject.ms.jobs.exception.NotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quartz.*;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;

import java.util.Date;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JobSchedulerServiceTest {
    @Mock
    private Scheduler scheduler;

    @Mock
    private ApplicationContext context;

    @InjectMocks
    private JobSchedulerService jobSchedulerService;

    // Classe de test interne pour simuler un Job valide
    private static class SampleJob implements Job {
        @Override
        public void execute(JobExecutionContext context) {}
    }

    @Test
    @DisplayName("should schedule a job successfully with correct parameters")
    void schedule_Success() throws SchedulerException {
        // Arrange
        String name = "myJob";
        String group = "myGroup";
        String cron = "0 0/5 * * * ?";
        String jobType = "SampleJob";

        when(context.getBean(jobType)).thenReturn(new SampleJob());

        // Capturons les arguments envoyés à Quartz
        ArgumentCaptor<JobDetail> jobDetailCaptor = ArgumentCaptor.forClass(JobDetail.class);
        ArgumentCaptor<Set<? extends Trigger>> triggersCaptor = ArgumentCaptor.forClass(Set.class);

        // Act
        jobSchedulerService.schedule(name, group, cron, jobType);

        // Assert
        verify(scheduler).scheduleJob(jobDetailCaptor.capture(), triggersCaptor.capture(), eq(true));

        JobDetail capturedJob = jobDetailCaptor.getValue();
        assertThat(capturedJob.getKey().getName()).isEqualTo(name);
        assertThat(capturedJob.getKey().getGroup()).isEqualTo(group);
        assertThat(capturedJob.getJobClass()).isEqualTo(SampleJob.class);

        CronTrigger capturedTrigger = (CronTrigger) triggersCaptor.getValue().iterator().next();
        assertThat(capturedTrigger.getCronExpression()).isEqualTo(cron);
        assertThat(capturedTrigger.getKey().getName()).isEqualTo(name + "-trigger");
    }

    @Test
    @DisplayName("should throw IllegalArgumentException when jobType is not found in context")
    void schedule_JobTypeNotFound() {
        // Arrange
        when(context.getBean("UnknownJob")).thenThrow(new NoSuchBeanDefinitionException("UnknownJob"));

        // Act & Assert
        assertThatThrownBy(() -> jobSchedulerService.schedule("n", "g", "0 * * * * ?", "UnknownJob"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Job non trouvé");
    }

    @Test
    @DisplayName("should pause a job correctly")
    void pause_Success() throws SchedulerException {
        // Act
        jobSchedulerService.pause("myJob", "myGroup");

        // Assert
        verify(scheduler).pauseJob(JobKey.jobKey("myJob", "myGroup"));
    }

    @Test
    @DisplayName("should resume a job correctly")
    void resume_Success() throws SchedulerException {
        // Act
        jobSchedulerService.resume("myJob", "myGroup");

        // Assert
        verify(scheduler).resumeJob(JobKey.jobKey("myJob", "myGroup"));
    }

    @Test
    @DisplayName("should update cron and reschedule job")
    void updateJobCron_Success() throws SchedulerException {
        // Arrange
        String name = "myJob";
        String group = "myGroup";
        String newCron = "0 0 12 * * ?";
        TriggerKey triggerKey = TriggerKey.triggerKey(name + "-trigger", group);

        when(scheduler.rescheduleJob(eq(triggerKey), any(Trigger.class))).thenReturn(new Date());

        // Act
        jobSchedulerService.updateJobCron(name, group, newCron);

        // Assert
        verify(scheduler).rescheduleJob(eq(triggerKey), any(CronTrigger.class));
    }

    @Test
    @DisplayName("should throw NotFoundException when rescheduling a non-existent job")
    void updateJobCron_NotFound() throws SchedulerException {
        // Arrange
        when(scheduler.rescheduleJob(any(TriggerKey.class), any(Trigger.class))).thenReturn(null);

        // Act & Assert
        assertThatThrownBy(() -> jobSchedulerService.updateJobCron("ghost", "group", "0 * * * * ?"))
                .isInstanceOf(NotFoundException.class);
    }
}