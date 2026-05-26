package com.cdl.ajustement.repository;

import com.cdl.ajustement.entity.ScheduledTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScheduledTaskRepository extends JpaRepository<ScheduledTask, Long> {
    List<ScheduledTask> findByStatusOrderByScheduledTimeAsc(String status);
    List<ScheduledTask> findByConfigNameAndStatus(String configName, String status);
}
