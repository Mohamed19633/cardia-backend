package com.java.backend.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data

public class DoctorListItemDTO {

    private Long id;

    @Size(min = 3, message = "at least 3 characters required in the name")
    private String name;

// TODO:   Add photo attribute;

    @NotBlank(message = "Phone number is required")
    @Pattern(
            regexp = "^(?:\\+20|20|0)?1(0|1|2|5)[0-9]{8}$",
            message = "Invalid Egyptian mobile number!"
    )
    private String contactNumber;

    @NotBlank(message = "Street is required")
    @Size(min = 3, max = 100)
    private String streetAddress;

    @NotBlank(message = "City is required")
    @Size(min = 2, max = 50)
    private String city;

    @NotBlank(message = "State is required")
    @Size(min = 2, max = 50)
    private String state;

    @NotBlank(message = "Country is required")
    private String country;

    @NotNull(message = "Specialization can not be null")
    @Size(min = 3, message = "At least 3 characters in Specialization")
    private String specialization;

    @NotNull(message = "Specialization can not be null")
    private String workTime;

    public DoctorListItemDTO(){

    }

    public DoctorListItemDTO(String name, String contactNumber, String streetAddress, String city, String state, String country, String specialization, String workTime){
        this.name = name;
        this.contactNumber = contactNumber;
        this.streetAddress = streetAddress;
        this.city = city;
        this.state = state;
        this.country = country;
        this.specialization = specialization;
        this.workTime = workTime;
    }
}
