package com.java.backend.controller;

import com.java.backend.dto.DoctorDTO;
import com.java.backend.dto.PersonDTO;
import com.java.backend.dto.PredictionResultDTO;
import com.java.backend.model.Patient;
import com.java.backend.service.AdminService;
import com.java.backend.service.PredictionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api/admin")
public class AdminController {

    private final PredictionService predictionService;
    private final AdminService adminService;

    public AdminController(AdminService adminService, PredictionService predictionService){
        this.adminService = adminService;
        this.predictionService = predictionService;
    }

    @GetMapping("/users")
    public ResponseEntity<List<PersonDTO>> viewAllUsers(@AuthenticationPrincipal UserDetails userDetails){
        // get the authenticated person from persistence layer
        String email = userDetails.getUsername();
        
        List<PersonDTO> users =  adminService.getAllUsersExceptAdmins(email);
        return  ResponseEntity.ok(users);
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<PersonDTO> viewUser(@PathVariable Long id){
        PersonDTO personDTO = adminService.viewUser(id);
        return ResponseEntity.ok(personDTO);
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id){
        adminService.deleteUser(id);
        return ResponseEntity.ok("User Id: "+id+" Deleted successfully");
    }

    @GetMapping("/predictions")
    public  ResponseEntity<List<PredictionResultDTO>> viewAllPredictions(){
       List<PredictionResultDTO> predictionList =  adminService.getAllPredictions();
       return ResponseEntity.ok(predictionList);
    }

    @GetMapping("/predictions/{id}/patient")
    public ResponseEntity<PersonDTO> getPatientOfPrediction(@PathVariable Long id){
        Patient patient = predictionService.getPatientOfPrediction(id);
        return viewUser(patient.getId());
    }

    @DeleteMapping("/predictions/{id}")
    public ResponseEntity<String> deletePrediction(@PathVariable Long id){
        predictionService.deleteById(id);
        return ResponseEntity.ok("Prediction with id: "+id+" has been deleted.");
    }

    @PostMapping("/doctors")
    public ResponseEntity<?> addDoctor(@RequestBody @Valid DoctorDTO doctorDTO){
        Map<String ,String> result =  adminService.registerNewDoctor(doctorDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }
}
