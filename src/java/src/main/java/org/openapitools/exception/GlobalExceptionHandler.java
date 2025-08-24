package org.openapitools.exception;

import jakarta.validation.ConstraintViolationException;
import org.openapitools.model.Error;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * Global exception handler for the Lamp Control API. This handler catches validation and parameter
 * conversion exceptions and converts them to appropriate HTTP responses as documented in the
 * OpenAPI specification.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

  /**
   * Handle validation constraint violations (e.g., @Min, @Max annotations).
   *
   * @param ex the constraint violation exception
   * @return 400 Bad Request with error details
   */
  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<Error> handleConstraintViolationException(
      final ConstraintViolationException ex) {
    final Error error = new Error("INVALID_ARGUMENT");
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
  }

  /**
   * Handle method argument validation errors (e.g., @Valid on request body).
   *
   * @param ex the method argument not valid exception
   * @return 400 Bad Request with error details
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Error> handleMethodArgumentNotValidException(
      final MethodArgumentNotValidException ex) {
    final Error error = new Error("INVALID_ARGUMENT");
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
  }

  /**
   * Handle type conversion errors (e.g., invalid number format).
   *
   * @param ex the method argument type mismatch exception
   * @return 400 Bad Request with error details
   */
  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<Error> handleMethodArgumentTypeMismatchException(
      final MethodArgumentTypeMismatchException ex) {
    final Error error = new Error("INVALID_ARGUMENT");
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
  }
}
