package com.myproject.ms.jobs.controller;

import com.myproject.ms.jobs.config.SecurityConfig;
import com.myproject.ms.jobs.dto.CronUpdateRequest;
import com.myproject.ms.jobs.dto.JobRequest;
import com.myproject.ms.jobs.service.JobSchedulerService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(JobSchedulerController.class)
@Import(SecurityConfig.class)
@WithMockUser
class JobSchedulerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JobSchedulerService jobSchedulerService;

    @Test
    @DisplayName("POST /schedule - Doit planifier un job avec succès")
    void schedule_ShouldReturnOk() throws Exception {
        // Arrange
        JobRequest request = new JobRequest("MyJob", "DEFAULT", "0 * * * * ?", "MyTask");

        // Act & Assert
        mockMvc.perform(post("/v1/jobs/scheduler/schedule")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        verify(jobSchedulerService).schedule("MyJob", "DEFAULT", "0 * * * * ?", "MyTask");
    }

    @Test
    @DisplayName("POST /pause - Doit mettre le job en pause")
    void pause_ShouldReturnNoContent() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/v1/jobs/scheduler/DEFAULT/MyJob/pause"))
                .andExpect(status().isNoContent());

        verify(jobSchedulerService).pause("MyJob", "DEFAULT");
    }

    @Test
    @DisplayName("POST /resume - Doit relancer le job")
    void resume_ShouldReturnNoContent() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/v1/jobs/scheduler/DEFAULT/MyJob/resume"))
                .andExpect(status().isNoContent());

        verify(jobSchedulerService).resume("MyJob", "DEFAULT");
    }

    @Test
    @DisplayName("PATCH /cron - Doit mettre à jour le CRON en nettoyant les quotes")
    void updateCron_ShouldReturnOk() throws Exception {
        // Arrange
        CronUpdateRequest updateRequest = new CronUpdateRequest("0 0/5 * * * ?");

        // Act & Assert
        mockMvc.perform(patch("/v1/jobs/scheduler/DEFAULT/MyJob/cron")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNoContent());

        verify(jobSchedulerService).updateJobCron("MyJob", "DEFAULT", updateRequest);
    }
}