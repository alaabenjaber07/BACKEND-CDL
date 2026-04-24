package com.cdl.ajustement.entity;

import lombok.*;
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "CDL_QUERY_EXTRACTION_LOG")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QueryExtractionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "CONFIG_NAME", nullable = false)
    private String configName;

    @Column(name = "EXTRACTED_BY", nullable = false)
    private String extractedBy;

    @Column(name = "EXTRACTION_DATE", nullable = false)
    private LocalDateTime extractionDate;

    // To identify if it's extraction 1 (data) or 2 (logs)
    @Column(name = "EXTRACTION_INDEX")
    private Integer extractionIndex;

    @Column(name = "STATUS")
    private String status; // STARTED, SUCCESS, FAILED

    @Column(name = "FILE_PATH")
    private String filePath;

    @Column(name = "PROCESSED_ROWS")
    private Long processedRows;

    @Column(name = "TOTAL_ROWS")
    private Long totalRows;
}
