package com.ps.authservice.controller;

import com.ps.authservice.dto.LoginRequest;
import com.ps.authservice.dto.LoginResponse;
import com.ps.authservice.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.swing.text.html.Option;
import java.util.Optional;

@RestController
@RequestMapping("/login")
public class LoginController {

    private static final Logger log = LoggerFactory.getLogger(LoginController.class);
    private final AuthService  authService;

    public LoginController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "Generate token on user login")
    @PostMapping
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {

        loginRequest.setEmail("testuser@test.com");
        loginRequest.setPassword("password123");

        Optional<String> resp = authService.authenticate(loginRequest);
        if(resp.isEmpty())
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        String token = resp.get();
        return ResponseEntity.ok(new LoginResponse(token));
    }
    @Operation(summary = "token validation endpoint ")
    @GetMapping("/validate")
    public ResponseEntity<Void> validateToken(@RequestHeader("Authorization") String bearerToken){
        log.error("entered auth service");
        if(bearerToken.isEmpty() || !bearerToken.startsWith("Bearer "))
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return authService.validateToken(bearerToken.substring(7))?
                ResponseEntity.ok().build() :
                ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
}
