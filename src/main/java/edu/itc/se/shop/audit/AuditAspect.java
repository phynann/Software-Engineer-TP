package edu.itc.se.shop.audit;

import java.lang.reflect.Method;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class AuditAspect {
    private static final Logger log = LoggerFactory.getLogger(AuditAspect.class);

    private final ObjectProvider<CurrentRequest> currentRequestProvider;

    public AuditAspect(ObjectProvider<CurrentRequest> currentRequestProvider) {
        this.currentRequestProvider = currentRequestProvider;
    }

    @Around("@annotation(edu.itc.se.shop.audit.Audited)")
    public Object audit(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        Object result = null;
        try {
            result = joinPoint.proceed();
            return result;
        } finally {
            long durationMs = System.currentTimeMillis() - start;
            MethodSignature sig = (MethodSignature) joinPoint.getSignature();
            String className = sig.getDeclaringType().getSimpleName();
            String methodName = sig.getName();

            String customerInfo = extractCustomer(joinPoint.getArgs());
            String resultInfo = extractResult(result, joinPoint.getArgs());

            CurrentRequest currentRequest = currentRequestProvider.getIfAvailable();
            String reqId = currentRequest != null && currentRequest.getRequestId() != null
                    ? currentRequest.getRequestId()
                    : "system";

            log.info("AUDIT {}.{} {}{} ({} ms)",
                    className, methodName, customerInfo, resultInfo, durationMs);
        }
    }

    private String extractCustomer(Object[] args) {
        if (args != null && args.length > 0 && args[0] != null) {
            Object first = args[0];
            try {
                Method m = first.getClass().getMethod("customerId");
                Object val = m.invoke(first);
                if (val != null) {
                    return "customer=" + val + " ";
                }
            } catch (Exception ignored) {
            }
        }
        return "";
    }

    private String extractResult(Object result, Object[] args) {
        if (result != null) {
            try {
                Method m = result.getClass().getMethod("id");
                Object val = m.invoke(result);
                if (val != null) {
                    return "result=order " + val;
                }
            } catch (Exception ignored) {
            }
            return "result=" + result.getClass().getSimpleName();
        }
        if (args != null && args.length > 0 && args[0] != null) {
            return "target=" + args[0];
        }
        return "result=done";
    }
}
