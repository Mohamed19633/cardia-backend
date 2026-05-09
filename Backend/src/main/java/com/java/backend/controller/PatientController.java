package com.java.backend.controller;

import com.java.backend.dto.DoctorListItemDTO;
import com.java.backend.dto.PatientDTO;
import com.java.backend.dto.PatientMedicalDataDTO;
import com.java.backend.dto.PredictionResultDTO;
import com.java.backend.mapper.PatientMapper;
import com.java.backend.model.Patient;
import com.java.backend.service.DoctorService;
import com.java.backend.service.PatientService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/patient")
public class PatientController {

   private final PatientService patientService;
   private final DoctorService doctorService;
    private PatientMapper patientMapper;

    public PatientController(PatientService patientService,PatientMapper patientMapper,DoctorService doctorService){
        this.patientService = patientService;
        this.doctorService = doctorService;
        this.patientMapper = patientMapper;
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@RequestBody @Valid PatientDTO patientDTO){
        Map<String ,String> result =  patientService.registerNewPatient(patientDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }


    @GetMapping("/me")
    public ResponseEntity<PatientDTO> viewPersonalDetails(@AuthenticationPrincipal UserDetails userDetails){

        // get person from persistence layer
        String email = userDetails.getUsername();
        Patient patient = patientService.getPatientByEmail(email);

        // map it to person DTO
        PatientDTO patientDTO = patientMapper.toPatientDTO(patient);

        // forward it
        return ResponseEntity.ok(patientDTO);
    }

    @PutMapping("/update")
    public ResponseEntity<Map<String, String>> updatePatient(@RequestBody @Valid PatientDTO patientDTO, 
                                                           @AuthenticationPrincipal UserDetails userDetails){

        String email = userDetails.getUsername();
        Patient patient = patientService.getPatientByEmail(email);


        String result = patientService.updatePatient(patientDTO,patient);

        return ResponseEntity.ok(Map.of("Message",result));
    }


    @PostMapping("/predict")
    public ResponseEntity<PredictionResultDTO> predictionHeartDisease(@AuthenticationPrincipal UserDetails userDetails, @RequestBody @Valid PatientMedicalDataDTO patientMedicalDataDTO){
        PredictionResultDTO predictionResultDTO = patientService.predictHeartDisease(patientMedicalDataDTO,userDetails);
        return ResponseEntity.ok(predictionResultDTO);
    }


    @GetMapping("/doctors")
    public ResponseEntity<List<DoctorListItemDTO>>viewAllDoctors(){
        List<DoctorListItemDTO> doctors = doctorService.getAllDoctors();
        return ResponseEntity.ok(doctors);
    }


    @PostMapping("/book-appointment/{doctorId}")
    public ResponseEntity<String> bookAppointment(@AuthenticationPrincipal UserDetails userDetails, 
                                                @PathVariable Long doctorId){
        String patientEmail = userDetails.getUsername();
        patientService.bookAppointment(patientEmail, doctorId);
        return ResponseEntity.ok("Booked successfully");
    }
}
