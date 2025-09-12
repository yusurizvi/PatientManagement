package com.ps.patiantservice.mapper;

import com.ps.patiantservice.dto.PatientRequest;
import com.ps.patiantservice.dto.PatientResponse;
import com.ps.patiantservice.model.Patient;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class PatientMapper {

    public PatientResponse toPatientResponse(Patient patient){
       return PatientResponse.builder().id(patient.getId().toString())
                .name(patient.getName())
                .dateOfBirth(patient.getDateOfBirth().toString())
                .email(patient.getEmail())
                .address(patient.getAddress())
                .build();
    }

    public Patient RequestMapToPatient(Patient patient,PatientRequest request) {

        if(patient != null){
            patient.setName(request.getName());
            patient.setEmail(request.getEmail());
            patient.setAddress(request.getAddress());
            patient.setDateOfBirth(LocalDate.parse(request.getDateOfBirth()));
            patient.setRegistrationDate(LocalDate.parse(request.getRegistration()));
            return patient;
        }
        return Patient.builder().name(request.getName())
                .address(request.getAddress())
                .dateOfBirth(LocalDate.parse(request.getDateOfBirth()))
                .email(request.getEmail())
                .registrationDate(LocalDate.parse(request.getRegistration())).build();
    }
}
