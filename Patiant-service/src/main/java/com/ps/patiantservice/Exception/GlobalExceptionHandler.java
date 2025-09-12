package com.ps.patiantservice.Exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
//import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String,String>> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex){
        Map<String,String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach((error)-> errors.put(error.getField(),error.getDefaultMessage()));
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(EmailDuplicationException.class)
    public ResponseEntity<Map<String,String>> handleEmailDuplicationException(EmailDuplicationException ex){

        log.warn("EmailDuplicationException "+ex.getMessage());
        Map<String,String> errors = new HashMap<>();
        errors.put("Meessage","Email already exists -");
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(PatientNotFoundException.class)
    public ResponseEntity<Map<String,String>> handlePatientNotFoundException(PatientNotFoundException ex){
        log.warn("PatientNotFoundException "+ex.getMessage());
        Map<String,String> errors = new HashMap<>();
        errors.put("Meessage","Patient doesn't exist");
        return new ResponseEntity<>(errors, HttpStatus.NOT_FOUND);
    }
}
