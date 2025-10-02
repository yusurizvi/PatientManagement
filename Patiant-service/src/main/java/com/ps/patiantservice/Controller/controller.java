package com.ps.patiantservice.Controller;

import com.ps.patiantservice.dto.PatientRequest;
import com.ps.patiantservice.dto.PatientResponse;
import com.ps.patiantservice.service.PatientService;
import jakarta.validation.groups.Default;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/patients")
@AllArgsConstructor
public class controller {

    private final PatientService  patientService;

    @GetMapping
    public ResponseEntity<List<PatientResponse>> getPatients(){

        List<PatientResponse> resp = patientService.getPatientDetails();
        return new ResponseEntity<>(resp, HttpStatus.OK);
    }
    @PostMapping
    public ResponseEntity<PatientResponse> addPatient(@Validated({Default.class}) @RequestBody PatientRequest request){
        return new ResponseEntity<>(patientService.addPatient(request),HttpStatus.OK);
    }
    @PutMapping("/{id}")
    public ResponseEntity<PatientResponse> updatePatient(@Validated({Default.class}) @RequestBody PatientRequest request, @PathVariable UUID id){
       return new ResponseEntity<>(patientService.updatePatient(id,request),HttpStatus.OK);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> DeletePatient(@PathVariable UUID id){
        patientService.deletePatient(id);
        return ResponseEntity.noContent().build();
    }
}
