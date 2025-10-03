package com.ps.authservice.repository;

import com.ps.authservice.model.User;
//import net.bytebuddy.dynamic.DynamicType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RepositoryUser extends JpaRepository<User, UUID> {

    public Optional<User> findByEmail(String email);
}
