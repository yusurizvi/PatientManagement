package com.ps.patiantservice.Exception;

public class EmailDuplicationException extends RuntimeException {
    public EmailDuplicationException(String message, String email) {
        super(message);
    }
}
