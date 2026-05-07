package org.hyf.movie.aspects;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggerAspect {

    // @Around must return Object so the actual controller return value is not lost
    @Around(value = "execution(* org.hyf.movie.controller.*.*(..))")
    public Object logControllerMethodCalls(ProceedingJoinPoint joinPoint) throws Throwable {
        String method = joinPoint.getSignature().toShortString();
        long start = System.currentTimeMillis();
        System.out.println("[LOG] Calling: " + method);

        Object result = joinPoint.proceed(); // execute the actual method

        long duration = System.currentTimeMillis() - start;
        System.out.println("[LOG] Finished: " + method + " (" + duration + "ms)");
        return result;
    }
}
