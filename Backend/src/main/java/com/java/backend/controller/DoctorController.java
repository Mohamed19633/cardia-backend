package com.java.backend.controller;

import com.java.backend.dto.PatientListItemDTO;
import com.java.backend.dto.PredictionResultDTO;
import com.java.backend.dto.PrescriptionDTO;
import com.java.backend.service.DoctorService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/doctor")
public class DoctorController {

    private final DoctorService doctorService;

    public DoctorController(DoctorService doctorService){
        this.doctorService = doctorService;
    }

    @GetMapping("/patients")
    public ResponseEntity<List<PatientListItemDTO>> viewPatients(@AuthenticationPrincipal UserDetails userDetails){
        // get person from persistence layer
        String email = userDetails.getUsername();

        List<PatientListItemDTO> patientListItemDTO = doctorService.getPatientList(email);
        return ResponseEntity.ok(patientListItemDTO);
    }

    @GetMapping("/patients/{patientId}/predictions")
    public ResponseEntity<List<PredictionResultDTO>> viewPatientPredictionsList(@PathVariable Long patientId,
                                                                                @AuthenticationPrincipal UserDetails userDetails) {
        // get person from persistence layer
        String doctorEmail = userDetails.getUsername();

        List<PredictionResultDTO> predictionsDTOs = doctorService.viewPatientPredictionsList(patientId,doctorEmail);
        return ResponseEntity.ok(predictionsDTOs);
    }

    @GetMapping("/patients/{patientId}/prescription")
    public ResponseEntity<PrescriptionDTO> initializePrescription(@PathVariable Long patientId, @AuthenticationPrincipal UserDetails userDetails) {
        // get person from persistence layer
        String doctorEmail = userDetails.getUsername();

        PrescriptionDTO prescriptiondto = doctorService.initializePrescription(patientId,doctorEmail);
        return ResponseEntity.ok(prescriptiondto);
    }

    @PostMapping("/save-prescription")
    public ResponseEntity<String> savePrescription(@RequestBody @Valid PrescriptionDTO prescriptionDTO) {

        doctorService.savePrescription(prescriptionDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body("Prescription saved successfully!");
    }
}
