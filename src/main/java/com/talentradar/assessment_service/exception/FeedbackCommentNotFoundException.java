package com.talentradar.assessment_service.exception;

public class FeedbackCommentNotFoundException extends RuntimeException {
    public FeedbackCommentNotFoundException(String message) {
        super(message);
    }
}