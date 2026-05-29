package com.java.backend.service;

import com.java.backend.dto.DoctorDTO;
import com.java.backend.dto.PatientMedicalTestsViewDTO;
import com.java.backend.dto.PersonDTO;
import com.java.backend.exception.EmailAlreadyUsedException;
import com.java.backend.exception.UserNotFoundException;
import com.java.backend.mapper.DoctorMapper;
import com.java.backend.mapper.MedicalTestsMapper;
import com.java.backend.mapper.PersonMapper;
import com.java.backend.model.Doctor;
import com.java.backend.model.MedicalTest;
import com.java.backend.model.Person;
import com.java.backend.repository.DoctorRepository;
import com.java.backend.repository.PatientRepository;
import com.java.backend.repository.PersonRepository;
import com.java.backend.repository.MedicalTestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
 @Transactional
public class AdminService {
    private final PersonRepository personRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final MedicalTestRepository medicalTestRepository;
    private final DoctorMapper doctorMapper;
    private final PersonMapper personMapper;
    private MedicalTestsMapper medicalTestsMapper;

    public AdminService(PersonRepository personRepository,MedicalTestsMapper medicalTestsMapper, MedicalTestRepository medicalTestRepository, DoctorRepository doctorRepository, PatientRepository patientRepository, DoctorMapper doctorMapper, PersonMapper personMapper) {
        this.personRepository = personRepository;
        this.medicalTestRepository = medicalTestRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.doctorMapper = doctorMapper;
        this.personMapper = personMapper;
        this.medicalTestsMapper = medicalTestsMapper;
    }

    public List<PersonDTO> getAllUsersExceptAdmins(String email) {
        //get all users except admins from person repository
        List<Person> users = personRepository.findAllExceptAdmins();
        return  users.stream().map(personMapper::toDto).toList();
    }

    public PersonDTO viewUser(Long id) {
        return personRepository.findById(id)
                .filter(person -> !person.getRole().getName().equals("ADMIN"))
                .map(personMapper::toDto)
                .orElseThrow(() -> new UserNotFoundException("No users with id: " + id));
    }

    @Transactional
    public void deleteUser(Long id) {
        Optional<Person> person = personRepository.findById(id);

        if(person.isEmpty() || person.get().getRole().getName().equals("ADMIN"))
            throw new UserNotFoundException("No users with id: "+id);


        if(person.get().getRole().getName().equals("DOCTOR")) {
            doctorRepository.deleteById(id);
        }else
            patientRepository.deleteById(id);
        personRepository.deleteById(id);
    }

    public List<PatientMedicalTestsViewDTO> getMedicalTestsDTOS() {
        List<MedicalTest> medicalTestList =  medicalTestRepository.findAll();
        return medicalTestList.stream().map(medicalTestsMapper::toDTO).toList();
    }

    public Map<String, String> registerNewDoctor(DoctorDTO doctorDTO) {
        if(doctorRepository.existsByEmail(doctorDTO.getEmail())){
            throw new EmailAlreadyUsedException("Email already in use!");
        }
        Doctor doctor = doctorRepository.save(doctorMapper.toDoctorEntity(doctorDTO));

        Map<String, String> map = new HashMap<>();
        map.put("message","Registered Successfully");
        map.put("role",doctor.getRole().getName());
        return map;
    }
}
