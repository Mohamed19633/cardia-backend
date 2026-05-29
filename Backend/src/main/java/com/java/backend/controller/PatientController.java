package com.java.backend.controller;

import com.java.backend.dto.*;
import com.java.backend.mapper.PatientMapper;
import com.java.backend.model.Appointment;
import com.java.backend.model.Patient;
import com.java.backend.service.DoctorService;
import com.java.backend.service.PatientService;
import com.java.backend.utilities.ConnectivityType;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
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


    @GetMapping("/me/appointments")
    public ResponseEntity<List<AppointmentListPatientViewDTO>> viewAppointments(@AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        List<AppointmentListPatientViewDTO> appointmentListPatientViewDTOS = new ArrayList<>();
        appointmentListPatientViewDTOS.addAll(patientService.getPatientAppointment(email));
        return  ResponseEntity.ok(appointmentListPatientViewDTOS);
    }

    @GetMapping("/me/medical-tests")
    public ResponseEntity<List<PatientMedicalTestsViewDTO>> viewPatientMedicalTests(@AuthenticationPrincipal UserDetails userDetails){
        String email = userDetails.getUsername();
        List<PatientMedicalTestsViewDTO> patientMedicalTestsViewDTOS = patientService.getPatientMedicalTests(email);
        return ResponseEntity.ok(patientMedicalTestsViewDTOS);
    }

    @GetMapping("/me/prescriptions")
    public ResponseEntity<List<PrescriptionDTO>> viewPatientPrescriptions(@AuthenticationPrincipal UserDetails userDetails){
        String email = userDetails.getUsername();
        List<PrescriptionDTO> patientPrescriptionDTOS = patientService.getPatientPrescriptions(email);
        return ResponseEntity.ok(patientPrescriptionDTOS);
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
                                                  @PathVariable Long doctorId, @RequestParam String connectivityType, @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime time){
        String patientEmail = userDetails.getUsername();
        ConnectivityType connectivity = connectivityType.toUpperCase().equals("ONLINE")? ConnectivityType.ONLINE : ConnectivityType.OFFLINE;
        Appointment appointment = patientService.bookAppointment(patientEmail, doctorId,connectivity, time);
        return ResponseEntity.ok("Appointment confirmed successfully\n"+appointment.getMeetingLink());
    }

    @PatchMapping("/cancel-appointment/{appointmentId}")
    public ResponseEntity<String> cancelAppointment(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Long appointmentId) {
        patientService.cancelAppointment(appointmentId);
        return ResponseEntity.ok("Appointment Cancelled Successfully");
    }
}
