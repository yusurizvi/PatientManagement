package com.ps.patiantservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PatientRequest {

    @NotBlank(message = "Name acnnot be blank")
    @Size(max = 100,message = "The name should be less than 100 characters")
    private String name;
    @NotBlank(message = "Email should not be blank")
    @Email(message = "invalid email")
    private String email;
    @NotBlank(message = "Address should not be blank")
    private String address;
    @NotBlank(message = "Date of birth should be blank")
    private String dateOfBirth;
    @NotBlank(message ="Registration should not be blank")
    private String registration;
}
