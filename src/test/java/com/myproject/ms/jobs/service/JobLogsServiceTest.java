package com.myproject.ms.jobs.service;

import com.myproject.ms.jobs.dto.JobLogDto;
import com.myproject.ms.jobs.entity.JobLog;
import com.myproject.ms.jobs.exception.NotFoundException;
import com.myproject.ms.jobs.repository.JobLogRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobLogsServiceTest {
    @Mock
    private JobLogRepository logRepository;

    @InjectMocks
    private JobLogsService jobLogsService;

    @Test
    @DisplayName("Devrait sauvegarder un log de job")
    void shouldSaveJobLog() {
        // Arrange
        JobLog log = new JobLog();
        log.setJobName("TestJob");
        log.setMessage("Process started");

        // Act
        jobLogsService.save(log);

        // Assert
        verify(logRepository, times(1)).save(log);
    }

    @Test
    @DisplayName("Devrait appeler la suppression des logs pour un job donné")
    void shouldClearLogsByJobName() {
        // Arrange
        String jobName = "CleanupJob";

        // Act
        jobLogsService.clearLogs(jobName);

        // Assert
        verify(logRepository, times(1)).deleteByJobName(jobName);
    }

    @Test
    @DisplayName("Devrait retourner un DTO pour un ID existant")
    void getLogById_Success() {
        // Arrange
        Long id = 1L;
        JobLog entity = new JobLog();
        entity.setId(id);
        entity.setJobName("TestJob");
        when(logRepository.findById(id)).thenReturn(Optional.of(entity));

        // Act
        JobLogDto result = jobLogsService.getLogById(id);

        // Assert
        assertThat(result).isExactlyInstanceOf(JobLogDto.class);
        assertThat(result.id()).isEqualTo(id);
        assertThat(result.jobName()).isEqualTo("TestJob");
    }

    @Test
    @DisplayName("Devrait lever une NotFoundException si le log n'existe pas")
    void getLogById_NotFound() {
        when(logRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> jobLogsService.getLogById(99L));
    }

    @Test
    @DisplayName("Devrait retourner une Page de DTO")
    void getLogs_ShouldReturnDtoPage() {
        // Arrange
        String name = "JobA";
        JobLog entity = new JobLog();
        entity.setJobName(name);

        Page<JobLog> entityPage = new PageImpl<>(List.of(entity));
        when(logRepository.findByJobName(eq(name), any(Pageable.class))).thenReturn(entityPage);

        // Act
        Page<JobLogDto> result = jobLogsService.getLogs(name, 0, 10);

        // Assert
        assertThat(result.getContent()).isNotEmpty();
        assertThat(result.getContent().getFirst()).isExactlyInstanceOf(JobLogDto.class);
        assertThat(result.getContent().getFirst().jobName()).isEqualTo(name);
    }
}