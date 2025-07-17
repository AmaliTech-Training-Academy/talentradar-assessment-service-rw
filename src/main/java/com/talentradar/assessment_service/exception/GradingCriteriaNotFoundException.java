package com.talentradar.assessment_service.exception;

public class GradingCriteriaNotFoundException extends RuntimeException {
    public GradingCriteriaNotFoundException(String message) {
        super(message);
    }
}