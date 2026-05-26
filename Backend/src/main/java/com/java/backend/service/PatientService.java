package com.java.backend.service;

import com.java.backend.dto.DoctorListItemDTO;
import com.java.backend.dto.PatientDTO;
import com.java.backend.dto.PatientMedicalDataDTO;
import com.java.backend.dto.PredictionResultDTO;
import com.java.backend.exception.EmailAlreadyUsedException;
import com.java.backend.exception.UserNotFoundException;
import com.java.backend.mapper.PatientMapper;
import com.java.backend.mapper.PredictionMapper;
import com.java.backend.model.Address;
import com.java.backend.model.Doctor;
import com.java.backend.model.Patient;
import com.java.backend.model.Prediction;
import com.java.backend.repository.DoctorRepository;
import com.java.backend.repository.PatientRepository;
import com.java.backend.repository.PersonRepository;
import com.java.backend.repository.PredictionRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatusCode;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.*;

@Service
@Transactional
public class PatientService {
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final PatientMapper patientMapper;
    private final PredictionRepository predictionRepository;
    private final PersonRepository personRepository;
    private WebClient predictionWebClient;
    private PredictionMapper predictionMapper;

    public PatientService(PatientRepository patientRepository, PredictionMapper predictionMapper, WebClient predictionWebClient, PatientMapper patientMapper, DoctorRepository doctorRepository, PredictionRepository predictionRepository, PersonRepository personRepository){
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.patientMapper = patientMapper;
        this.predictionWebClient = predictionWebClient;
        this.predictionMapper = predictionMapper;
        this.predictionRepository = predictionRepository;
        this.personRepository = personRepository;
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
       PredictionResultDTO predictionResultDTO =   predictionWebClient.post()
                .uri("/predict")
                .bodyValue(patientMedicalDataDTO)
                .retrieve().onStatus(HttpStatusCode::isError, response ->
                       response.bodyToMono(String.class)
                               .flatMap(errorBody -> {
                                   System.err.println("Python service error: " + errorBody);
                                   return Mono.error(new RuntimeException("Prediction service failed: " + errorBody));
                               })
               )
                .bodyToMono(PredictionResultDTO.class)
                .block();

        customizeResult(predictionResultDTO, patientMedicalDataDTO, userDetails);
        return predictionResultDTO;
    }

    private void customizeResult(PredictionResultDTO predictionResultDTO, PatientMedicalDataDTO patientMedicalDataDTO, UserDetails userDetails) {
        String diagnosis = predictionResultDTO.getDiagnosis(), recommendationMessage = "Enjoy your day!",riskCategory = "";
        List<DoctorListItemDTO> recommendedDoctors = new ArrayList<>();
        if(diagnosis != null && !diagnosis.equals("Healthy")) {
            recommendationMessage = "Please consult a healthcare professional.";
            riskCategory = classifyRiskCategory(patientMedicalDataDTO);
            Patient patient = patientRepository.findByEmail(userDetails.getUsername()).orElseThrow(() -> new UserNotFoundException("Patient not found"));
            recommendedDoctors = getRecommendedDoctor(riskCategory, patient.getAddress());
        }


        predictionResultDTO.setRecommendationMsg(recommendationMessage);
        predictionResultDTO.setRiskCategory(riskCategory);
        predictionResultDTO.setRecommendedDoctors(recommendedDoctors);
        predictionResultDTO.setDateAndTime(LocalDateTime.now());
        predictionResultDTO.setBelongsTo(userDetails.getUsername());

        Prediction prediction = predictionMapper.toEntity(predictionResultDTO);
        predictionRepository.save(prediction);

    }

    private List<DoctorListItemDTO> getRecommendedDoctor(String category, Address address) {
        String doctorSpecialization =  "";
        switch(category) {
            case "Exercise-Induced Ischemic Risk Pattern":
                doctorSpecialization = "Sports Cardiologist";
                break;

            case "ECG Abnormality Pattern":
                doctorSpecialization =  "Cardiac Electrophysiologist";
                break;

            case "Severe Cardiovascular Risk Pattern":
                doctorSpecialization =  "Cardiac Surgery Unit";
                break;

            default:
                doctorSpecialization =  "General Cardiologist";
        }
        List< DoctorListItemDTO> doctorListItemDTOList = getSpecializedDoctorInArea(doctorSpecialization, address);
        return doctorListItemDTOList;
    }

    private List<DoctorListItemDTO> getSpecializedDoctorInArea(String specialization, Address address) {
        List<DoctorListItemDTO> doctorListItemDTOList = new ArrayList<>();
        doctorListItemDTOList = personRepository.getSpecializedDoctorsBasedonState(specialization, address.getState());
        doctorListItemDTOList.addAll(personRepository.getSpecializedDoctorsBasedonCity(specialization, address.getCity()));
        if(doctorListItemDTOList.isEmpty())
            doctorListItemDTOList.addAll(personRepository.getSpecializedDoctorsBasedonCountry(specialization, address.getCountry()));
        return doctorListItemDTOList;
    }

    private String classifyRiskCategory(PatientMedicalDataDTO medicalData) {
            if (medicalData.getExang() == 1 && medicalData.getOldpeak() > 2.0)
                return "Exercise-Induced Ischemic Risk Pattern";

            else if (medicalData.getChol() > 240 && medicalData.getCp() >= 3)
                return "Atherosclerotic Risk Pattern";

            else if (medicalData.getTrestbps() >= 140)
                return "Hypertensive Cardiac Stress Pattern";

            else if (medicalData.getFbs() == 1 && medicalData.getCp() >= 2)
                return "Diabetes-Associated Cardiac Risk";

            else if (medicalData.getRestecg() > 1)
                return "ECG Abnormality Pattern";

            else if (medicalData.getThal() < 120 && medicalData.getOldpeak() > 1.0)
                return "Reduced Cardiac Performance Pattern";

            else if (medicalData.getCa() >= 2 || medicalData.getThal() >= 6)
                return "Severe Cardiovascular Risk Pattern";

            return "General Cardiac Risk Pattern";
    }
}
