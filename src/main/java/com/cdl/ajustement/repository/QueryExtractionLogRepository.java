package com.cdl.ajustement.repository;

import com.cdl.ajustement.entity.QueryExtractionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QueryExtractionLogRepository extends JpaRepository<QueryExtractionLog, Long> {
    List<QueryExtractionLog> findAllByOrderByExtractionDateDesc();

    java.util.Optional<QueryExtractionLog> findTopByConfigNameAndExtractionIndexOrderByExtractionDateDesc(
            String configName,
            Integer extractionIndex);
}
