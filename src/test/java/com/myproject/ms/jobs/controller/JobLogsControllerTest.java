package com.myproject.ms.jobs.controller;

import com.myproject.ms.jobs.dto.JobLogDto;
import com.myproject.ms.jobs.exception.NotFoundException;
import com.myproject.ms.jobs.service.JobLogsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(JobLogsController.class)
class JobLogsControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JobLogsService jobLogsService;

    @Test
    @DisplayName("GET /v1/logs/{name} - Doit retourner une page de logs")
    void getLogs_ShouldReturnPagedLogs() throws Exception {
        JobLogDto dto = new JobLogDto(1L, "JobA", LocalDateTime.now(), "STEP1", "SUCCESS", "OK");
        when(jobLogsService.getLogs(anyString(), anyInt(), anyInt()))
                .thenReturn(new PageImpl<>(List.of(dto)));

        mockMvc.perform(get("/v1/logs/JobA")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].jobName").value("JobA"))
                .andExpect(jsonPath("$.content[0].status").value("SUCCESS"));
    }

    @Test
    @DisplayName("GET /v1/logs/{name}/{id} - Succès avec ID valide")
    void getById_ValidId_ShouldReturnLog() throws Exception {
        JobLogDto dto = new JobLogDto(123L, "JobA", LocalDateTime.now(), "STEP1", "INFO", "Message");
        when(jobLogsService.getLogById(123L)).thenReturn(dto);

        mockMvc.perform(get("/v1/logs/JobA/123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(123));
    }

    @Test
    @DisplayName("GET /v1/logs/{name}/{id} - Erreur 400 si l'ID n'est pas un nombre")
    void getById_InvalidId_ShouldReturnBadRequest() throws Exception {
        // Ici, on teste ta "vérification" de parsing.
        // Spring va échouer à convertir "abc" en Long et renvoyer 400.
        mockMvc.perform(get("/v1/logs/JobA/abc"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /v1/logs/{name}/{id} - Erreur 404 si le log n'existe pas")
    void getById_NotFound_ShouldReturn404() throws Exception {
        when(jobLogsService.getLogById(999L)).thenThrow(new NotFoundException("Log non trouvé"));

        mockMvc.perform(get("/v1/logs/JobA/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /v1/logs/{name} - Succès")
    void deleteLogs_ShouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/v1/logs/JobA"))
                .andExpect(status().isNoContent());
    }
}