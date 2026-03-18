package com.myproject.ms.jobs.controller;

import com.myproject.ms.jobs.config.SecurityConfig;
import com.myproject.ms.jobs.dto.JobResponse;
import com.myproject.ms.jobs.dto.JobTypeDto;
import com.myproject.ms.jobs.service.JobService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(JobController.class)
@Import(SecurityConfig.class)
@WithMockUser
class JobControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JobService jobService;

    @Test
    @DisplayName("GET /v1/jobs/jobs - Doit retourner tous les jobs groupés")
    void listAllGrouped_ShouldReturnMap() throws Exception {
        // Arrange
        JobResponse response = new JobResponse("Job1", "G1", "Desc", "0 * * * * ?", "NORMAL", LocalDateTime.now());
        when(jobService.getAllJobsGrouped()).thenReturn(Map.of("G1", List.of(response)));

        // Act & Assert
        mockMvc.perform(get("/v1/jobs/jobs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.G1").isArray())
                .andExpect(jsonPath("$.G1[0].name").value("Job1"));
    }

    @Test
    @DisplayName("GET /v1/jobs/groups/{group} - Doit retourner la liste d'un groupe")
    void listByGroup_ShouldReturnList() throws Exception {
        // Arrange
        JobResponse response = new JobResponse("Job1", "G1", "Desc", "0 * * * * ?", "NORMAL", LocalDateTime.now());
        when(jobService.getJobsByGroup("G1")).thenReturn(List.of(response));

        // Act & Assert
        mockMvc.perform(get("/v1/jobs/groups/G1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Job1"))
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @DisplayName("GET /v1/jobs/groups/{group}/jobs/{name} - Doit retourner un job précis")
    void getOne_ShouldReturnJob() throws Exception {
        // Arrange
        JobResponse response = new JobResponse("Job1", "G1", "Desc", "0 * * * * ?", "NORMAL", LocalDateTime.now());
        when(jobService.getJobDetails("Job1", "G1")).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/v1/jobs/groups/G1/jobs/Job1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Job1"))
                .andExpect(jsonPath("$.group").value("G1"));
    }

    @Test
    @DisplayName("DELETE - Doit retourner 200 quand le job est supprimé")
    void deleteJob_Success_ShouldReturnOk() throws Exception {
        // Arrange
        when(jobService.deleteScheduledJob("Job1", "G1")).thenReturn(true);

        // Act & Assert
        mockMvc.perform(delete("/v1/jobs/groups/G1/jobs/Job1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Job supprimé définitivement."));
    }

    @Test
    @DisplayName("DELETE - Doit retourner 404 quand le job n'existe pas")
    void deleteJob_NotFound_ShouldReturn404() throws Exception {
        // Arrange
        when(jobService.deleteScheduledJob("Ghost", "G1")).thenReturn(false);

        // Act & Assert
        mockMvc.perform(delete("/v1/jobs/groups/G1/jobs/Ghost"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Job non trouvé."));
    }

    @Test
    @DisplayName("GET /v1/jobs/types - Doit retourner la liste triée des types de jobs")
    void getAvailableTypes_ShouldReturnList() throws Exception {
        // Arrange
        List<JobTypeDto> mockTypes = List.of(
                new JobTypeDto("AlphaJob", "Description Alpha"),
                new JobTypeDto("CleanupJob", "Description Cleanup")
        );

        when(jobService.getAvailableJobTypes()).thenReturn(mockTypes);

        // Act & Assert
        mockMvc.perform(get("/v1/jobs/types")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                // On vérifie que le JSON correspond à notre liste
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value("AlphaJob"))
                .andExpect(jsonPath("$[1].id").value("CleanupJob"));
    }
}