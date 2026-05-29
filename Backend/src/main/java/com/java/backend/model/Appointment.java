package com.java.backend.model;

import com.java.backend.utilities.AppointmentStatus;
import com.java.backend.utilities.ConnectivityType;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Entity
@Data
public class Appointment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    private Patient patient;
    @ManyToOne(fetch = FetchType.LAZY)
    private Doctor doctor;

    private LocalDateTime time;

    @NotNull(message = "Connectivity type cannot be null")
    private ConnectivityType connectivityType;

    @NotNull(message = "Status cannot be null")
    private AppointmentStatus status;

    @Nullable
    private String meetingLink;
}
