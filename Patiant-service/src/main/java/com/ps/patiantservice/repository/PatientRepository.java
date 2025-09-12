package com.ps.patiantservice.repository;

import com.ps.patiantservice.model.Patient;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
@Repository
public interface PatientRepository extends JpaRepository<Patient, UUID> {

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(@NotBlank(message = "Email should not be blank") @Email(message = "invalid email") String email, UUID id);
}
