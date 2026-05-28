package com.java.backend.controller;

import com.java.backend.dto.DoctorDTO;
import com.java.backend.dto.PatientMedicalTestsViewDTO;
import com.java.backend.dto.PersonDTO;
import com.java.backend.model.Patient;
import com.java.backend.service.AdminService;
import com.java.backend.service.MedicalTestService;
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

    private final MedicalTestService medicalTestService;
    private final AdminService adminService;

    public AdminController(AdminService adminService, MedicalTestService medicalTestService){
        this.adminService = adminService;
        this.medicalTestService = medicalTestService;
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

    @GetMapping("/medical-tests")
    public  ResponseEntity<List<PatientMedicalTestsViewDTO>> viewAllMedicalTests(){
       List<PatientMedicalTestsViewDTO> medicalTestsViewDTOS =  adminService.getMedicalTestsDTOS();
       return ResponseEntity.ok(medicalTestsViewDTOS);
    }

    @GetMapping("/medical-tests/{testId}/patient")
    public ResponseEntity<PersonDTO> getPatientOfMedicalTest(@PathVariable Long testId){
        Patient patient = medicalTestService.getPatientOfMedicalTest(testId);
        return viewUser(patient.getId());
    }

    @DeleteMapping("/medical-test/{id}")
    public ResponseEntity<String> deleteMedicalTest(@PathVariable Long id){
        medicalTestService.deleteById(id);
        return ResponseEntity.ok("Medical test with id: "+id+" has been deleted.");
    }

    @PostMapping("/doctors")
    public ResponseEntity<?> addDoctor(@RequestBody @Valid DoctorDTO doctorDTO){
        Map<String ,String> result =  adminService.registerNewDoctor(doctorDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }
}
