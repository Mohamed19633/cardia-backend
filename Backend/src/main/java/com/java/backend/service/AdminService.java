package com.java.backend.service;

import com.java.backend.dto.DoctorDTO;
import com.java.backend.dto.PersonDTO;
import com.java.backend.dto.PredictionResultDTO;
import com.java.backend.exception.EmailAlreadyUsedException;
import com.java.backend.exception.UserNotFoundException;
import com.java.backend.mapper.DoctorMapper;
import com.java.backend.mapper.PersonMapper;
import com.java.backend.mapper.PredictionMapper;
import com.java.backend.model.Doctor;
import com.java.backend.model.Person;
import com.java.backend.model.Prediction;
import com.java.backend.repository.DoctorRepository;
import com.java.backend.repository.PatientRepository;
import com.java.backend.repository.PersonRepository;
import com.java.backend.repository.PredictionRepository;
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
    private final PredictionRepository predictionRepository;
    private final DoctorMapper doctorMapper;
    private final PersonMapper personMapper;
    private final PredictionMapper predictionMapper;

    public AdminService(PersonRepository personRepository, PredictionRepository predictionRepository, DoctorRepository doctorRepository, PatientRepository patientRepository, DoctorMapper doctorMapper, PersonMapper personMapper, PredictionMapper predictionMapper) {
        this.personRepository = personRepository;
        this.predictionRepository = predictionRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.doctorMapper = doctorMapper;
        this.personMapper = personMapper;
        this.predictionMapper = predictionMapper;
    }

    public List<PersonDTO> getAllUsersExceptAdmins(String email) {
        //get all users from person repository
        List<Person> users = personRepository.findAll();
        // loop and except admins
        return  users.stream().filter(user -> !user.getRole().getName().equals("ADMIN"))
                .map(personMapper::toDto).toList();
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
            patientRepository.removeDoctorFromPatients(id);
            doctorRepository.deleteById(id);
        }else
            patientRepository.deleteById(id);
        personRepository.deleteById(id);
    }

    public List<PredictionResultDTO> getAllPredictions() {
        List<Prediction> predictionList =  predictionRepository.findAll();
        return predictionList.stream().map(predictionMapper::toDTO).toList();
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
