package com.java.backend.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
public class Patient extends Person{


    @OneToMany(mappedBy = "patient", cascade = CascadeType.PERSIST)
    private List<Appointment> appointments = new ArrayList<>();

    @OneToMany(mappedBy = "patient", cascade = CascadeType.PERSIST)
    private List<Prescription> prescriptions = new ArrayList<>();

    @OneToMany(mappedBy = "patient", cascade = CascadeType.PERSIST)
    private List<MedicalTest> medicalTestList = new ArrayList<>();// one to many - pi

}
