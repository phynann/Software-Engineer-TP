package edu.itc.se.shop.service;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class TimingAspect {
    private static final Logger log = LoggerFactory.getLogger(TimingAspect.class);

    @Around("execution(public * edu.itc.se.shop.service..*Service.*(..))")
    public Object measureTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.nanoTime();
        try {
            return joinPoint.proceed();
        } finally {
            long durationMicros = (System.nanoTime() - start) / 1000;
            String signature = joinPoint.getSignature().getDeclaringType().getSimpleName()
                    + "." + joinPoint.getSignature().getName() + "(..)";
            log.debug("{} took {} µs", signature, durationMicros);
        }
    }
}
