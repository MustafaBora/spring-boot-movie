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
    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(MovieAlreadyExistsException.class)
    public Map<String, String> handleMovieAlreadyExists(MovieAlreadyExistsException ex)
    {
        return Map.of("error", ex.getMessage());
    }

    // --- Custom exception handlers ---
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(MovieNotFoundException.class)
    public Map<String, String> handleMovieNotFound(MovieNotFoundException ex)
    {
        return Map.of("error", ex.getMessage());
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(org.hyf.movie.exception.ReviewNotFoundException.class)
    public Map<String, String> handleReviewNotFound(org.hyf.movie.exception.ReviewNotFoundException ex)
    {
        return Map.of("error", ex.getMessage());
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(IllegalArgumentException.class)
    public Map<String, String> handleIllegalArgument(IllegalArgumentException ex)
    {
        return Map.of("error", ex.getMessage());
    }

    //This is the last resort before runtimeexception
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public Map<String, String> handleMovieNotFound(DataIntegrityViolationException ex)
    {
        Map<String, String> errors = new HashMap<>();
        errors.put("error", ex.getMessage());
        errors.put("myMessage", "There is a database error!");
        return errors;
    }

    // Catch-all for any other RuntimeException not handled above.
    // prefer specific exception classes over this
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(RuntimeException.class)
    public Map<String, String> handleRuntime(RuntimeException ex)
    {
        return Map.of("error", ex.getMessage());
    }
}