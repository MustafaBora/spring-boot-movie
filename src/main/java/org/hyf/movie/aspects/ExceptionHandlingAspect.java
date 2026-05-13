package org.hyf.movie.aspects;


import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class ExceptionHandlingAspect {



    @AfterThrowing(
            pointcut = "execution(* org.hyf.movie.service.*.*(..))",
            throwing = "exception"
    )
    public void logServiceExceptions(JoinPoint joinPoint, Throwable exception) {
        String methodName = joinPoint.getSignature().toShortString();
        log.warn("Exception occured on " + methodName + ", it threw: "
            + exception.getClass().getSimpleName() + " - " + exception.getMessage());
    }
}
