package com.cdl.ajustement.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "CDL_SCHEDULED_TASKS")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduledTask {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "CONFIG_NAME")
    private String configName;

    @Column(name = "SCHEDULED_TIME")
    private LocalDateTime scheduledTime;

    @Column(name = "SCHEDULED_BY")
    private String scheduledBy;

    @Column(name = "STATUS")
    private String status; // PENDING, EXECUTED, CANCELLED
}
