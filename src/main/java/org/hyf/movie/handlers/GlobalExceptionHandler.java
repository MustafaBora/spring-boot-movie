package org.hyf.movie.handlers;

import jakarta.validation.ConstraintViolationException;
import org.hyf.movie.exception.MovieAlreadyExistsException;
import org.hyf.movie.exception.MovieNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler
{

    // Handles @Valid failures on @RequestBody fields
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Map<String, String> handleValidationErrors(MethodArgumentNotValidException ex)
    {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(err ->
                errors.put(err.getField(), err.getDefaultMessage()));
        return errors;
    }

    // Handles @Validated failures on @PathVariable / @RequestParam
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ConstraintViolationException.class)
    public Map<String, String> handleConstraintViolation(ConstraintViolationException ex)
    {
        Map<String, String> errors = new HashMap<>();
        ex.getConstraintViolations().forEach(v ->
                errors.put(v.getPropertyPath().toString(), v.getMessage()));
        return errors;
    }

    // --- Custom exception handlers ---

    // Technique 1: Add a timestamp so the client knows exactly when the error occurred.
    // Useful for correlating client-side logs with server logs.
    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(MovieAlreadyExistsException.class)
    public Map<String, String> handleMovieAlreadyExists(MovieAlreadyExistsException ex)
    {
        return Map.of(
                "error", ex.getMessage(),
                "timestamp", Instant.now().toString()  // e.g. "2026-05-10T12:34:56.789Z"
        );
    }

    // Technique 2: Include the numeric HTTP status code in the body.
    // Clients can read this without inspecting the HTTP header separately.
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(MovieNotFoundException.class)
    public Map<String, String> handleMovieNotFound(MovieNotFoundException ex)
    {
        return Map.of(
                "error", ex.getMessage(),
                "status", String.valueOf(HttpStatus.NOT_FOUND.value())  // "404"
        );
    }

    // Technique 3: Include the exception class name.
    // Helpful during development — the client can see which exception was thrown.
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(org.hyf.movie.exception.ReviewNotFoundException.class)
    public Map<String, String> handleReviewNotFound(org.hyf.movie.exception.ReviewNotFoundException ex)
    {
        return Map.of(
                "error", ex.getMessage(),
                "type", ex.getClass().getSimpleName()  // e.g. "ReviewNotFoundException"
        );
    }

    // Technique 4: Include the request path so the client knows which endpoint triggered the error.
    // Requires HttpServletRequest as an extra parameter — Spring injects it automatically.
    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(IllegalArgumentException.class)
    public Map<String, String> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request)
    {
        return Map.of(
                "error", ex.getMessage(),
                "path", request.getRequestURI()  // e.g. "/api/users/register"
        );
    }

    //This is the last resort before runtimeexception
    // Technique 5: Combine multiple extra fields for a richer, structured response.
    // This is the step towards a dedicated ErrorResponse record/class.
    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public Map<String, String> handleDataIntegrity(DataIntegrityViolationException ex, HttpServletRequest request)
    {
        Map<String, String> errors = new HashMap<>(error(ex));
        errors.put("timestamp", Instant.now().toString());
        errors.put("path", request.getRequestURI());
        errors.put("hint", "A database constraint was violated (e.g. duplicate key or null field)");
        return errors;
    }

    // Catch-all for any other RuntimeException not handled above.
    // prefer specific exception classes over this
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(RuntimeException.class)
    public Map<String, String> handleRuntime(RuntimeException ex)
    {
        return error(ex);
    }

    private Map<String, String> error(Exception ex)
    {
        return Map.of("error", ex.getMessage());
    }
}