package com.java.backend.mapper;

import com.java.backend.dto.PredictionResultDTO;
import com.java.backend.model.Prediction;
import com.java.backend.repository.DoctorRepository;
import com.java.backend.repository.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class  PredictionMapper {

    @Autowired
    private PatientRepository patientRepository;

    public PredictionResultDTO toDTO(Prediction prediction){
        PredictionResultDTO predictionResultDTO = new PredictionResultDTO();
        predictionResultDTO.setDateAndTime(prediction.getCreatedAt());
        predictionResultDTO.setResult(prediction.getPredictionResult());
        predictionResultDTO.setRiskScore(prediction.getRiskScore());
        predictionResultDTO.setBelongsTo(prediction.getPatient().getEmail());
        return predictionResultDTO;
    }

    public Prediction toEntity(PredictionResultDTO predictionResultDTO){
        Prediction prediction = new Prediction();
        prediction.setPredictionResult(predictionResultDTO.getResult());
        prediction.setRiskScore(predictionResultDTO.getRiskScore());
        prediction.setCreatedAt(predictionResultDTO.getDateAndTime());
        prediction.setPatient(patientRepository.findByEmail(predictionResultDTO.getBelongsTo()).get());

        return prediction;
    }
}
