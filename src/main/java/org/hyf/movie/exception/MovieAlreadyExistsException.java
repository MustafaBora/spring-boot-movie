package org.hyf.movie.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class MovieAlreadyExistsException extends RuntimeException {
    public MovieAlreadyExistsException(String title) {
        super("Movie with name '" + title + "' already exists!");
    }
}
