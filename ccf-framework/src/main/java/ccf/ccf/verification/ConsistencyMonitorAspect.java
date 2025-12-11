package ccf.ccf.verification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class ConsistencyMonitorAspect {

    private final ConsistencyVerifier consistencyVerifier;

    @Around("@annotation(ccf.ccf.verification.MonitorConsistency)")
    public Object monitorConsistency(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        log.info("Monitoring consistency for method: {}", methodName);

        long startTime = System.currentTimeMillis();

        try {
            Object result = joinPoint.proceed();

            long duration = System.currentTimeMillis() - startTime;
            log.info("Method {} completed in {}ms", methodName, duration);

            return result;

        } catch (Exception e) {
            log.error("Exception in monitored method {}: {}", methodName, e.getMessage());
            throw e;
        }
    }
}