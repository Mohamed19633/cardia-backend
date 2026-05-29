package com.java.backend.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class PredictionResultDTO {

    private LocalDateTime DateAndTime;
    private String diagnosis; // YES OR NO
    private String riskProbability;
    private String riskCategory;
    private List<DoctorListItemDTO> recommendedDoctors;
    private String belongsTo;
    private String recommendationMsg;
}
