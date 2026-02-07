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

  private static final String INVALID_ARG_ERROR = "INVALID_ARGUMENT";

  /**
   * Handle validation constraint violations (e.g., @Min, @Max annotations).
   *
   * @param ex the constraint violation exception
   * @return 400 Bad Request with error details
   */
  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<Error> handleConstraintViolationException(
      final ConstraintViolationException ex) {
    final Error error = new Error(INVALID_ARG_ERROR);
    return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
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
    final Error error = new Error(INVALID_ARG_ERROR);
    return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
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
    final Error error = new Error(INVALID_ARG_ERROR);
    return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
  }

  /**
   * Handle null pointer exceptions that may occur during request processing.
   *
   * @param ex the null pointer exception
   * @return 400 Bad Request with error details
   */
  @ExceptionHandler(NullPointerException.class)
  public ResponseEntity<Error> handleNullPointerException(final NullPointerException ex) {
    final Error error = new Error(INVALID_ARG_ERROR);
    return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
  }

  /**
   * Handle lamp not found exceptions.
   *
   * @param ex the lamp not found exception
   * @return 404 Not Found
   */
  @ExceptionHandler(LampNotFoundException.class)
  public ResponseEntity<Void> handleLampNotFoundException(final LampNotFoundException ex) {
    return ResponseEntity.notFound().build();
  }

  /**
   * Handle illegal argument exceptions (e.g., invalid UUID format).
   *
   * @param ex the illegal argument exception
   * @return 400 Bad Request with error details
   */
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<Error> handleIllegalArgumentException(final IllegalArgumentException ex) {
    final Error error = new Error(INVALID_ARG_ERROR);
    return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
  }
}
