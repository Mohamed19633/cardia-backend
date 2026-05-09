package com.java.backend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Entity
public class Doctor extends Person{

    @NotNull(message = "Specialization can not be null")
    @Size(min = 3, message = "At least 3 characters in Specialization")
    private String specialization;

    @NotNull(message = "Work Time can not be null")
    private String workTime;

    @OneToMany(mappedBy = "doctor")
    private List<Patient> patientList = new ArrayList<>();

}
