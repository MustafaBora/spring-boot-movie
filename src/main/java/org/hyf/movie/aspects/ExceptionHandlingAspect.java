package org.hyf.movie.aspects;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * Logs exceptions thrown from any service method as a cross-cutting concern (README30).
 *
 * Note: the actual HTTP error responses are returned by GlobalExceptionHandler
 * (@RestControllerAdvice). This aspect only adds logging — it does not intercept
 * or modify the exception flow.
 */
@Aspect
@Component
public class ExceptionHandlingAspect {

    // Runs after any service method throws an exception
    @AfterThrowing(
            pointcut = "execution(* org.hyf.movie.service.*.*(..))",
            throwing = "ex"
    )
    public void logServiceException(JoinPoint joinPoint, Throwable ex) {
        String method = joinPoint.getSignature().toShortString();
        System.out.println("[EXCEPTION] " + method + " threw: "
                + ex.getClass().getSimpleName() + " — " + ex.getMessage());
    }
}
