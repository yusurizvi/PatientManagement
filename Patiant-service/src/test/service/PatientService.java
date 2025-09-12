package com.ps.patiantservice.service;

import com.ps.patiantservice.Exception.EmailDuplicationException;
import com.ps.patiantservice.Exception.PatientNotFoundException;
import com.ps.patiantservice.dto.PatientRequest;
import com.ps.patiantservice.dto.PatientResponse;
import com.ps.patiantservice.mapper.PatientMapper;
import com.ps.patiantservice.model.Patient;
import com.ps.patiantservice.repository.PatientRepository;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
public class PatientService {
    private final PatientRepository patientRepository;
    private final PatientMapper mapper;

    public List<PatientResponse> getPatientDetails(){
        return patientRepository.findAll().stream().map(mapper::toPatientResponse).toList();

//     return null;v
    }

    public PatientResponse addPatient(PatientRequest request) {

        if(patientRepository.existsByEmail(request.getEmail())){
            throw new EmailDuplicationException("Patient with this email alredy exists",request.getEmail());
        }
        Patient patient = mapper.RequestMapToPatient(null,request);
        patientRepository.save(patient);
        return mapper.toPatientResponse(patient);

    }

    public PatientResponse updatePatient(UUID id, @Valid PatientRequest request) {
        Optional<Patient> optionalPatient = patientRepository.findById(id);
        if(optionalPatient.isEmpty())
            throw new PatientNotFoundException("This patient doesn't exist");
        Patient patient = optionalPatient.get();
        patientRepository.save(mapper.RequestMapToPatient(patient,request));
        return mapper.toPatientResponse(optionalPatient.get());
    }

    public void deletePatient(UUID id) {
        patientRepository.deleteById(id);
    }
}
