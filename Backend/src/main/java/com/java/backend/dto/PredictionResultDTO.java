package com.java.backend.dto;

import com.java.backend.model.Prediction;
import com.java.backend.repository.DoctorRepository;
import com.java.backend.repository.PatientRepository;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PredictionResultDTO {

    private LocalDateTime DateAndTime;
    private String result; // YES OR NO
    private String riskScore;
    private String belongsTo;
}
