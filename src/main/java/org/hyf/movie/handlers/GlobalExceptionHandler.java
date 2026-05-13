package org.hyf.movie.handlers;

import jakarta.servlet.http.HttpServletRequest;
import org.hyf.movie.exception.MovieAlreadyExistsException;
import org.hyf.movie.exception.MovieNotFoundException;
import org.hyf.movie.exception.ReviewNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Map<String, String> handleValidationOnBody(MethodArgumentNotValidException exception) {
        Map<String, String> errorsToReturnMap = new HashMap<>();
        exception.getBindingResult().getFieldErrors().forEach( error ->
                errorsToReturnMap.put(error.getField(), error.getDefaultMessage() + ", it was " + error.getRejectedValue())
        );
        return errorsToReturnMap;
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(MovieNotFoundException.class)
    public Map<String, String> handleMovieNotFound(MovieNotFoundException movieNotFoundException) {
        return Map.of(
                "error", movieNotFoundException.getMessage(),
                "timestamp", Instant.now().toString(),
                "status", String.valueOf(HttpStatus.NOT_FOUND.value()),
                "status-explanation", HttpStatus.NOT_FOUND.getReasonPhrase().toString()
        );
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(ReviewNotFoundException.class)
    public Map<String, String> handleMovieNotFound(ReviewNotFoundException exception, HttpServletRequest request) {
        return Map.of(
            "error", exception.getMessage(),
            "path", request.getRequestURI(),
            "status", String.valueOf(HttpStatus.NOT_FOUND.value()),
            "hint", "With the given id, the review was not found"
        );
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public Map<String, String> handleDataIntegrityViolationException(DataIntegrityViolationException dive, HttpServletRequest request) {
        return Map.of(
                "error", dive.getMessage(),
                "path", request.getRequestURI(),
                "status", String.valueOf(HttpStatus.CONFLICT.value()),
                "hint", "Validation on backend side was OK but a constraint in database does not let us persist!",
                "timestamp", Instant.now().toString(),
                "type", dive.getClass().getSimpleName()
        );
    }

    //Catch all for any other RuntimeExceptions not handled on above handlers
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(RuntimeException.class)
    public Map<String, String> handleRuntime(RuntimeException runtimeException) {
        return Map.of(
                "error", runtimeException.getMessage(),
                "timestamp", Instant.now().toString(),
                "status", String.valueOf(HttpStatus.NOT_FOUND.value()),
                "status-explanation", HttpStatus.NOT_FOUND.getReasonPhrase().toString()
        );
    }

}
