package com.java.backend.service;

import com.java.backend.exception.UserNotFoundException;
import com.java.backend.model.Patient;
import com.java.backend.model.Prediction;
import com.java.backend.repository.PredictionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PredictionService {

    private final PredictionRepository predictionRepository;

    public PredictionService(PredictionRepository predictionRepository) {
        this.predictionRepository = predictionRepository;
    }
@Transactional(readOnly = true)
    public Patient getPatientOfPrediction(Long id) {
        return predictionRepository.findById(id)
                .map(Prediction::getPatient)
                .filter(patient -> patient != null)
                .orElseThrow(() -> new UserNotFoundException("Prediction or associated user not found for id: " + id));
    }

    @Transactional
    public void deleteById(Long id) {
        predictionRepository.deleteById(id);
    }
}
