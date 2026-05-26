package com.java.backend.service;

import com.java.backend.dto.DoctorListItemDTO;
import com.java.backend.dto.PatientListItemDTO;
import com.java.backend.dto.PredictionResultDTO;
import com.java.backend.dto.PrescriptionDTO;
import com.java.backend.exception.UserNotFoundException;
import com.java.backend.mapper.DoctorMapper;
import com.java.backend.mapper.PatientMapper;
import com.java.backend.mapper.PredictionMapper;
import com.java.backend.mapper.PrescriptionMapper;
import com.java.backend.model.Doctor;
import com.java.backend.model.Patient;
import com.java.backend.model.Prediction;
import com.java.backend.model.Prescription;
import com.java.backend.repository.DoctorRepository;
import com.java.backend.repository.PatientRepository;
import com.java.backend.repository.PrescriptionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final DoctorMapper doctorMapper;
    private final PatientMapper patientMapper;
    private final PredictionMapper predictionMapper;
    private final PrescriptionMapper prescriptionMapper;
    private PrescriptionRepository prescriptionRepository;

    public DoctorService(DoctorRepository doctorRepository,PrescriptionRepository prescriptionRepository, PatientRepository patientRepository, DoctorMapper doctorMapper, PatientMapper patientMapper, PredictionMapper predictionMapper, PrescriptionMapper prescriptionMapper) {
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.doctorMapper = doctorMapper;
        this.predictionMapper = predictionMapper;
        this.patientMapper = patientMapper;
        this.prescriptionMapper = prescriptionMapper;
        this.prescriptionRepository = prescriptionRepository;
    }

    public List<DoctorListItemDTO> getAllDoctors() {
        List<Doctor> doctors = doctorRepository.findAll();
        List<DoctorListItemDTO> doctorDTOsList = new ArrayList<>();
        for (Doctor doctor : doctors) {
            doctorDTOsList.add(doctorMapper.toDoctorListItemDTO(doctor));
        }
        return doctorDTOsList;
    }

    public List<PatientListItemDTO> getPatientList(String email) {
        Optional<Doctor> doctor = doctorRepository.findByEmail(email);
        if (doctor.isEmpty()) {
            throw new UserNotFoundException("User With Email = " + email + " Not Found");
        }
        List<Patient> patientList = doctor.get().getPatientList();
        return patientList.stream().map(patientMapper::toPatientDTOList).toList();
    }

    public List<PredictionResultDTO> viewPatientPredictionsList(Long patientId, String doctorEmail) {

        Patient patient = isPatientBelongToDoctor(patientId, doctorEmail);
        if (patient == null)
            throw new RuntimeException("Doctor with email " + doctorEmail + " Does not have this patient in his/her list");

       List<Prediction> predictions = patient.getPredictionList();

        //  Map to DTO list
        return predictions.stream()
                .map(predictionMapper::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    protected Patient isPatientBelongToDoctor(Long id, String doctorEmail) {
        Doctor doctor = doctorRepository.findByEmail(doctorEmail)
                .orElseThrow(() ->
                        new UserNotFoundException("Doctor not found"));
        for (Patient patient : doctor.getPatientList()) {
            if (patient.getId().equals(id))
                return patient;
        }
        return null;
    }

    public void savePrescription(PrescriptionDTO prescriptiondto) {
        Prescription prescription = prescriptionMapper.toEntity(prescriptiondto);
        prescription.getPatient().getPrescriptions().add(prescription);
        prescriptionRepository.save(prescription);
    }

    public PrescriptionDTO initializePrescription(Long patientId, String doctorEmail){
        Patient patient = isPatientBelongToDoctor(patientId, doctorEmail);
        if (patient == null) {
            throw new RuntimeException("Doctor with email " + doctorEmail + " Does not have this patient in his/her list");
        }

        Doctor doctor = doctorRepository.findByEmail(doctorEmail)
                .orElseThrow(() ->
                new UserNotFoundException("Doctor not found"));

        PrescriptionDTO prescriptiondto = new PrescriptionDTO();
        prescriptiondto.setPatientName(patient.getName());
        prescriptiondto.setDoctorName(doctor.getName());
        prescriptiondto.setPrescriptionDate(LocalDateTime.now());
        return prescriptiondto;
    }
}
