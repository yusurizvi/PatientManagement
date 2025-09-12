package com.ps.patiantservice.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PatientResponse {
    private String id;
    private String name;
    private String email;
    private String address;
    private String dateOfBirth;
}
