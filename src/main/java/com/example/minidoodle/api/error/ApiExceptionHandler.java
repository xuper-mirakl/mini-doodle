package com.example.minidoodle.api.error;

import com.example.minidoodle.application.Exceptions;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class ApiExceptionHandler {

  @ExceptionHandler(Exceptions.NotFound.class)
  public ResponseEntity<ApiError> notFound(Exceptions.NotFound ex, HttpServletRequest req) {
    return build(HttpStatus.NOT_FOUND, ex.getMessage(), req);
  }

  @ExceptionHandler(Exceptions.Conflict.class)
  public ResponseEntity<ApiError> conflict(Exceptions.Conflict ex, HttpServletRequest req) {
    return build(HttpStatus.CONFLICT, ex.getMessage(), req);
  }

  @ExceptionHandler(Exceptions.BadRequest.class)
  public ResponseEntity<ApiError> badRequest(Exceptions.BadRequest ex, HttpServletRequest req) {
    return build(HttpStatus.BAD_REQUEST, ex.getMessage(), req);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiError> validation(MethodArgumentNotValidException ex, HttpServletRequest req) {
    String msg = ex.getBindingResult().getFieldErrors().stream()
        .findFirst()
        .map(fe -> fe.getField() + " " + fe.getDefaultMessage())
        .orElse("validation error");
    return build(HttpStatus.BAD_REQUEST, msg, req);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiError> generic(Exception ex, HttpServletRequest req) {
    return build(HttpStatus.INTERNAL_SERVER_ERROR, "unexpected error", req);
  }

  private ResponseEntity<ApiError> build(HttpStatus status, String message, HttpServletRequest req) {
    ApiError body = new ApiError(
        Instant.now(),
        status.value(),
        status.getReasonPhrase(),
        message,
        req.getRequestURI()
    );
    return ResponseEntity.status(status).body(body);
  }
}
