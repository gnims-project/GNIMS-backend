package com.gnims.project.log.advice;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Slf4j
@Aspect
@Component
public class ReadOnlyTransactionCheckAspect {

    @Around("execution(* com.gnims.project.domain..*Service.read*(..))")
    public Object checkReadOnly(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        Object result = joinPoint.proceed();

        if (TransactionSynchronizationManager.isCurrentTransactionReadOnly()) {
            log.info("[{} Method is readOnly]", methodName);
            return result;
        }

        log.info("[{} Method is Not readOnly]", methodName);
        return result;
    }
}
