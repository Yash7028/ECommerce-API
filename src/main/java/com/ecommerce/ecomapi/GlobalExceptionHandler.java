package com.ecommerce.ecomapi;

import com.ecommerce.ecomapi.exception.*;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;

import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private ApiErrorResponse buildError(HttpStatus status, String message) {
        return new ApiErrorResponse(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                message
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<?> handleConstraintViolationException(ConstraintViolationException ex) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("status", HttpStatus.BAD_REQUEST.value());
        errorResponse.put("error", "Bad Request");

        // Pick first violation message or join all
        String message = ex.getConstraintViolations().iterator().next().getMessage();
        errorResponse.put("message", message);

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> orderNotFoundException(OrderNotFoundException ex){
        log.error("Order not found with given id ");
        return new ResponseEntity<>(buildError(HttpStatus.BAD_REQUEST,ex.getMessage()),HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AddressNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> addressNotFoundException(AddressNotFoundException ex){
        log.error("Address not found exception.");
        return new ResponseEntity<>(buildError(HttpStatus.BAD_REQUEST,ex.getMessage()),HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(OrderIsAlreadyPlacedException.class)
    public ResponseEntity<ApiErrorResponse> orderIsAlreadyPlace(OrderIsAlreadyPlacedException ex){
        return new ResponseEntity<>(buildError(HttpStatus.OK,ex.getMessage()),HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(RatingNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> ratingNotFoundException(RatingNotFoundException ex){
        return new ResponseEntity<>(buildError(HttpStatus.NOT_FOUND,ex.getMessage()),HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({ AccessDeniedException.class, AuthorizationDeniedException.class })
    public ResponseEntity<Object> handleAccessDenied(Exception ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Access Denied: You do not have permission to access this endpoint.");
        return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );
        log.error("validator error throw.");
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }


    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ApiErrorResponse> handleEmailAlreadyExists(EmailAlreadyExistsException ex) {
        log.warn("Email already exists", ex.getMessage());
        return new ResponseEntity<>(buildError(HttpStatus.BAD_REQUEST, ex.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        log.error("Data integrity violation", ex);
        return new ResponseEntity<>(buildError(HttpStatus.CONFLICT, "Email already exists!"), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleUserNotFound(UserNotFoundException ex) {
        log.error("User not found", ex);
        return new ResponseEntity<>(buildError(HttpStatus.NOT_FOUND, ex.getMessage()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(FileUploadException.class)
    public ResponseEntity<?> handleFileUploadException(FileUploadException ex){
        log.error("Something went to wrong while handling file", ex);
        return new ResponseEntity<>(Map.of(
                "error", "File Upload Error",
                "message", ex.getMessage()
        ), HttpStatus.BAD_REQUEST);
//        return  new ResponseEntity<>(buildError(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<?> handleMaxSizeException(MaxUploadSizeExceededException ex) {
        return new ResponseEntity<>(Map.of(
                "error", "File Too Large",
                "message", "The uploaded file exceeds the maximum allowed size of 10MB."
        ), HttpStatus.PAYLOAD_TOO_LARGE);
    }

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<?> productNotFound(ProductNotFoundException ex){
        log.error("Product Not Found with this id " , ex);
        return new ResponseEntity<>(buildError(HttpStatus.NOT_FOUND,"Product Not Found with this id "), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiErrorResponse> handleGeneric(RuntimeException ex) {
        log.error("Unexpected error", ex);
        return new ResponseEntity<>(buildError(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred."), HttpStatus.INTERNAL_SERVER_ERROR);
    }




}
