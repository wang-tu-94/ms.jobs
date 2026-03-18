package com.myproject.ms.jobs.repository;

import com.myproject.ms.jobs.entity.JobLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Repository
public interface JobLogRepository extends JpaRepository<JobLog, Long> {
    void deleteByJobName(String jobName);

    Page<JobLog> findByJobName(String jobName, Pageable pageable);

    @Modifying
    @Transactional
    void deleteByTimestampBefore(LocalDateTime limitDate);
}
