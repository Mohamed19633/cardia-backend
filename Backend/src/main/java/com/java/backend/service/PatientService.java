package com.java.backend.service;

import com.java.backend.dto.PatientDTO;
import com.java.backend.dto.PatientMedicalDataDTO;
import com.java.backend.dto.PredictionResultDTO;
import com.java.backend.exception.EmailAlreadyUsedException;
import com.java.backend.exception.UserNotFoundException;
import com.java.backend.mapper.PatientMapper;
import com.java.backend.mapper.PredictionMapper;
import com.java.backend.model.Doctor;
import com.java.backend.model.Patient;
import com.java.backend.model.Prediction;
import com.java.backend.repository.DoctorRepository;
import com.java.backend.repository.PatientRepository;
import com.java.backend.repository.PredictionRepository;
import jakarta.validation.Valid;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class PatientService {
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final PatientMapper patientMapper;
    private final PredictionRepository predictionRepository;
    private WebClient predictionWebClient;
    private PredictionMapper predictionMapper;

    public PatientService(PatientRepository patientRepository, PredictionMapper predictionMapper, WebClient predictionWebClient, PatientMapper patientMapper, DoctorRepository doctorRepository, PredictionRepository predictionRepository){
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.patientMapper = patientMapper;
        this.predictionWebClient = predictionWebClient;
        this.predictionMapper = predictionMapper;
        this.predictionRepository = predictionRepository;
    }

    public Patient getPatientByEmail(String email) {
        Optional<Patient> patient = patientRepository.findByEmail(email);
        if(patient.isEmpty())
            throw new UserNotFoundException("User With Email = "+email+" Not Found");
        return patient.get();
    }

    public Map<String,String> registerNewPatient(@Valid  PatientDTO patientDTO){
        if(patientRepository.existsByEmail(patientDTO.getEmail())){
            throw new EmailAlreadyUsedException("Email already in use!");
        }
        Patient patient = patientRepository.save( patientMapper.toPatientEntity(patientDTO,"SAVE",null));

        Map<String, String> map = new HashMap<>();
        map.put("message","Registered Successfully");
        map.put("role",patient.getRole().getName());
        return map;
    }

    public String updatePatient(@Valid PatientDTO patientDTO, Patient existingPatient) {
        Patient patient  = patientMapper.toPatientEntity(patientDTO,"EDIT",existingPatient);

        patientRepository.save(patient);// return will never be null.
        return "Patient Saved successfully.";
    }

    public void bookAppointment(String patientEmail, Long doctorId) {
        Optional<Patient> patient = patientRepository.findByEmail(patientEmail);
        if(patient.isEmpty()){
            throw new UserNotFoundException("No such Patient with this Email: "+patientEmail);
        }

        Optional<Doctor> doctor = doctorRepository.findById(doctorId);
        if(doctor.isEmpty())
            throw new UserNotFoundException("No Doctors with id: "+ doctorId);

        doctor.get().getPatientList().add(patient.get());
        patient.get().setDoctor(doctor.get());
        patient.get().setBookingDateAndTime(LocalDateTime.now());
        doctorRepository.save(doctor.get());
        patientRepository.save(patient.get());
    }

    public PredictionResultDTO predictHeartDisease(PatientMedicalDataDTO patientMedicalDataDTO, UserDetails userDetails) {
       Map<String,String> predictionApiResult =   predictionWebClient.post()
                .uri("/predict")
                .bodyValue(patientMedicalDataDTO)
                .retrieve()
                .bodyToMono((new ParameterizedTypeReference<Map<String, String>>() {}))
                .block();

       String result = predictionApiResult.get("prediction");
        String score = "";
        if(result.equals("YES")) {
            score = predictionApiResult.get("probability");
        }

        PredictionResultDTO predictionResultDTO = new PredictionResultDTO();
        predictionResultDTO.setResult(result);
        predictionResultDTO.setRiskScore(score);
        predictionResultDTO.setDateAndTime(LocalDateTime.now());
        predictionResultDTO.setBelongsTo(userDetails.getUsername());

        Prediction prediction = predictionMapper.toEntity(predictionResultDTO);
        predictionRepository.save(prediction);

        return predictionResultDTO;
    }
}
