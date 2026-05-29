package com.java.backend.mapper;

import com.java.backend.dto.PredictionResultDTO;
import com.java.backend.model.Prediction;
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
        predictionResultDTO.setDiagnosis(prediction.getDiagnosis());
        predictionResultDTO.setRiskProbability(prediction.getRiskProbability().toString()+"%");
        predictionResultDTO.setRiskCategory(prediction.getRiskCategory());
        predictionResultDTO.setBelongsTo(prediction.getPatient().getEmail());
        return predictionResultDTO;
    }

    public Prediction toEntity(PredictionResultDTO predictionResultDTO){
        Prediction prediction = new Prediction();
        prediction.setDiagnosis(predictionResultDTO.getDiagnosis());
        prediction.setRiskProbability(Double.parseDouble(predictionResultDTO.getRiskProbability().substring(0,predictionResultDTO.getRiskProbability().length()-2)));
        prediction.setRiskCategory(predictionResultDTO.getRiskCategory());
        prediction.setCreatedAt(predictionResultDTO.getDateAndTime());
        prediction.setPatient(patientRepository.findByEmail(predictionResultDTO.getBelongsTo()).get());

        return prediction;
    }
}
