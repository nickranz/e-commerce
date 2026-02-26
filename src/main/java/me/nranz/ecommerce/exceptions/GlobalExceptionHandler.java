package me.nranz.ecommerce.exceptions;

import me.nranz.ecommerce.dto.response.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ApiErrorResponse> handleUsernameExists(
            UserAlreadyExistsException ex,
            HttpServletRequest req
    ) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), req.getRequestURI());
    }

    // Fallback for DB unique-constraint violations (race conditions, etc.)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleDataIntegrity(
            DataIntegrityViolationException ex,
            HttpServletRequest req
    ) {
        // You can inspect ex.getMostSpecificCause().getMessage() to tailor messaging if you want
        return build(HttpStatus.CONFLICT, "Username or email already exists.", req.getRequestURI());
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidCredentials(
            InvalidCredentialsException ex,
            HttpServletRequest req
    ) {
        return build(HttpStatus.UNAUTHORIZED, ex.getMessage(), req.getRequestURI());
    }

    // Catch-all (keeps your API from leaking stack traces / details)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneric(
            Exception ex,
            HttpServletRequest req
    ) {
        ex.printStackTrace(); // replace with logger in real app
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong.", req.getRequestURI());
    }

    private ResponseEntity<ApiErrorResponse> build(HttpStatus status, String message, String path) {
        ApiErrorResponse body = new ApiErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                path,
                message
        );
        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(
            MethodArgumentNotValidException ex,
            HttpServletRequest req
    ) {

        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .findFirst()
                .orElse("Validation failed");

        return build(HttpStatus.BAD_REQUEST, message, req.getRequestURI());
    }
}