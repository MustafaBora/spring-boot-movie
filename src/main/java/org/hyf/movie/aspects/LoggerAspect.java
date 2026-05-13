package org.hyf.movie.aspects;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class LoggerAspect {

    @Around(value = "execution(* org.hyf.movie.controller.*.*(..))" )
    public Object logControllerMethodCalls(ProceedingJoinPoint joinPoint) throws Throwable {
        //Logger logger = LoggerFactory.getLogger(Logger.class);
        String methodName = joinPoint.getSignature().toShortString();
        long start = System.currentTimeMillis();
        log.warn("Calling method: " + methodName);
        Object result = joinPoint.proceed();
        long finish = System.currentTimeMillis();
        long duration = finish - start;
        log.warn(methodName + " finished in " + duration + " milliseconds");
        return result;
    }

}
