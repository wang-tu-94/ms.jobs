package com.myproject.ms.jobs.service;

import com.myproject.ms.jobs.dto.JobResponse;
import com.myproject.ms.jobs.dto.JobTypeDto;
import com.myproject.ms.jobs.exception.NotFoundException;
import com.myproject.ms.jobs.jobs.MyDynamicJob;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.context.ApplicationContext;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobServiceTest {
    @Mock
    private Scheduler scheduler;

    @Mock
    private ApplicationContext context;

    @InjectMocks
    private JobService jobService;

    @Test
    @DisplayName("should delete a job if it exists")
    void deleteScheduledJob_Success() throws SchedulerException {
        // Arrange
        JobKey jobKey = JobKey.jobKey("testName", "testGroup");
        when(scheduler.checkExists(jobKey)).thenReturn(true);
        when(scheduler.deleteJob(jobKey)).thenReturn(true);

        // Act
        boolean result = jobService.deleteScheduledJob("testName", "testGroup");

        // Assert
        assertThat(result).isTrue();
        verify(scheduler).deleteJob(jobKey);
    }

    @Test
    @DisplayName("should return false if job to delete does not exist")
    void deleteScheduledJob_NotExists() throws SchedulerException {
        // Arrange
        when(scheduler.checkExists(any(JobKey.class))).thenReturn(false);

        // Act
        boolean result = jobService.deleteScheduledJob("name", "group");

        // Assert
        assertThat(result).isFalse();
        verify(scheduler, never()).deleteJob(any());
    }

    @Test
    @DisplayName("should throw NotFoundException when job details are requested for missing job")
    void getJobDetails_NotFound() throws SchedulerException {
        // Arrange
        when(scheduler.checkExists(any(JobKey.class))).thenReturn(false);

        // Act & Assert
        assertThrows(NotFoundException.class, () -> jobService.getJobDetails("name", "group"));
    }

    @Test
    @DisplayName("should correctly map Quartz Job and Trigger to JobResponse")
    void getJobDetails_Success() throws SchedulerException {
        // Arrange
        String name = "job1";
        String group = "group1";
        JobKey jobKey = JobKey.jobKey(name, group);

        JobDetail jobDetail = mock(JobDetail.class);
        CronTrigger cronTrigger = mock(CronTrigger.class);
        TriggerKey triggerKey = TriggerKey.triggerKey("t1", group);

        when(scheduler.checkExists(jobKey)).thenReturn(true);
        when(scheduler.getJobDetail(jobKey)).thenReturn(jobDetail);
        when(jobDetail.getDescription()).thenReturn("Description du job");

        // Mock du Trigger et de son état
        when(scheduler.getTriggersOfJob(jobKey)).thenReturn((List) List.of(cronTrigger));
        when(cronTrigger.getCronExpression()).thenReturn("0 0 * * * ?");
        when(cronTrigger.getNextFireTime()).thenReturn(new Date());
        when(cronTrigger.getKey()).thenReturn(triggerKey);
        when(scheduler.getTriggerState(triggerKey)).thenReturn(Trigger.TriggerState.NORMAL);

        // Act
        JobResponse response = jobService.getJobDetails(name, group);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.name()).isEqualTo(name);
        assertThat(response.cronExpression()).isEqualTo("0 0 * * * ?");
        assertThat(response.status()).isEqualTo("NORMAL");
        assertThat(response.nextFireTime()).isNotNull();
    }

    @Test
    @DisplayName("should return all jobs grouped by group name")
    @SuppressWarnings("unchecked")
    void getAllJobsGrouped_Success() throws SchedulerException {
        // Arrange
        String group = "DEFAULT";
        JobKey jobKey = JobKey.jobKey("job1", group);

        when(scheduler.getJobGroupNames()).thenReturn(List.of(group));
        when(scheduler.getJobKeys(GroupMatcher.jobGroupEquals(group))).thenReturn(Set.of(jobKey));

        // Mocks pour la partie privée mapToResponse (appelée indirectement)
        JobDetail jobDetail = mock(JobDetail.class);
        when(scheduler.getJobDetail(jobKey)).thenReturn(jobDetail);
        when(scheduler.getTriggersOfJob(jobKey)).thenReturn(Collections.emptyList());

        // Act
        Map<String, List<JobResponse>> result = jobService.getAllJobsGrouped();

        // Assert
        assertThat(result).containsKey(group);
        assertThat(result.get(group)).hasSize(1);
        assertThat(result.get(group).getFirst().status()).isEqualTo("NO_TRIGGER");
    }

    @Test
    @DisplayName("Devrait retourner les types de jobs triés par leur ID (nom du bean)")
    void getAvailableJobTypes_ShouldReturnSortedById() {
        // Arrange
        // On simule des noms de beans dans le désordre
        String[] beanNames = {"ZebraJob", "AlphaJob", "CleanupJob"};
        when(context.getBeanNamesForType(Job.class)).thenReturn(beanNames);

        // On mocke le type pour chaque bean (nécessaire pour l'annotation)
        when(context.getType("ZebraJob")).thenReturn((Class) MyDynamicJob.class);
        when(context.getType("AlphaJob")).thenReturn((Class) MyDynamicJob.class);
        when(context.getType("CleanupJob")).thenReturn((Class) MyDynamicJob.class);

        // Act
        List<JobTypeDto> result = jobService.getAvailableJobTypes();

        // Assert
        assertThat(result).hasSize(3);
        // Vérification de l'ordre alphabétique des IDs
        assertThat(result.get(0).id()).isEqualTo("AlphaJob");
        assertThat(result.get(1).id()).isEqualTo("CleanupJob");
        assertThat(result.get(2).id()).isEqualTo("ZebraJob");
    }
}