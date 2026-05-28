package com.java.backend.mapper;

import com.java.backend.dto.*;
import com.java.backend.model.*;
import com.java.backend.repository.DoctorRepository;
import com.java.backend.repository.RoleRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class PatientMapper {

   private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final DoctorRepository doctorRepository;
    private final MedicalTestsMapper medicalTestsMapper;
    private final AppointmentMapper appointmentMapper;
    private PrescriptionMapper prescriptionMapper;

    public  PatientMapper(RoleRepository roleRepository, PrescriptionMapper prescriptionMapper , PasswordEncoder passwordEncoder, DoctorRepository doctorRepository, MedicalTestsMapper medicalTestsMapper, AppointmentMapper appointmentMapper){
        this.roleRepository= roleRepository;
        this.passwordEncoder = passwordEncoder;
        this. doctorRepository =doctorRepository;
        this.prescriptionMapper = prescriptionMapper;
        this.medicalTestsMapper = medicalTestsMapper;
        this.appointmentMapper = appointmentMapper;
    }

    public  Patient toPatientEntity(PatientDTO patientDTO,String requestType,Patient existingPatient) {
        if(patientDTO == null)
            return null;

        Patient patient = existingPatient;
        Address address;

        if(requestType.equals("SAVE")){
            patient = new Patient();
            patient.setEmail(patientDTO.getEmail());
            patient.setRole(roleRepository.findById(2L).get());
            patient.setPassword(passwordEncoder.encode(patientDTO.getPassword()));
            address = new Address();
        }else{
            address = existingPatient.getAddress();
        }

        patient.setName(patientDTO.getName());
        patient.setUserName(patientDTO.getUserName());
        patient.setContactNumber(patientDTO.getContactNumber());

        address.setStreetAddress(patientDTO.getStreetAddress());
        address.setCity(patientDTO.getCity());
        address.setState(patientDTO.getState());
        address.setCountry(patientDTO.getCountry());
        patient.setAddress(address);

        patient.setAge(patientDTO.getAge());

        return patient;
    }


    public PatientDTO toPatientDTO(Patient patient) {
        if (patient == null) {
            return null;
        }

        PatientDTO dto = new PatientDTO();

        dto.setId(patient.getId());
        dto.setName(patient.getName());
        dto.setUserName(patient.getUserName());
        dto.setEmail(patient.getEmail());
        dto.setPassword(patient.getPassword());
        dto.setContactNumber(patient.getContactNumber());
        dto.setAge(patient.getAge());

        if (patient.getAddress() != null) {
            dto.setStreetAddress(patient.getAddress().getStreetAddress());
            dto.setCity(patient.getAddress().getCity());
            dto.setState(patient.getAddress().getState());
            dto.setCountry(patient.getAddress().getCountry());
        }

        if(patient.getMedicalTestList() != null)
            dto.setMedicalTests(patient.getMedicalTestList().stream().map(medicalTestsMapper::toDTO).toList());


        if (patient.getPrescriptions() != null) {
            dto.setPrescriptions(patient.getPrescriptions().stream()
                    .map(prescriptionMapper::toDTO)
                    .toList());
        }


        if(patient.getAppointments() != null){
            dto.setAppointments(patient.getAppointments().stream().map(appointmentMapper::toAppointmentListPatientViewDTO).toList());
        }

        return dto;
    }
}
