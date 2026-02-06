package com.example.demo.exception;

public class WeeklyLimitExceededException extends RuntimeException {
    public WeeklyLimitExceededException(String message) {
        super(message);
    }
}
