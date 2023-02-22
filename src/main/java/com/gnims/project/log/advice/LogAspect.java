package com.gnims.project.log.advice;

import com.gnims.project.log.entity.Log;
import com.gnims.project.log.repository.LogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * DB 접속 시간이 1.5초 이상인 경우 해당 기록을 DB에 저장합니다.
 * 도입해보려고 한 이유
 * 1. 병목지점이 어디인지 판단할 수 있음
 * 2. 클라이언트 요청 추적 가능
 */

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class LogAspect {

    private final LogRepository timeLogRepository;

    // 사용자가 불편함을 느낄만한 시간 - 임시 결정
    private static Long stressTime = 1500l;
    // root 부터 domain 디렉토리까지 문자열 수
    private static Integer declaringTypeNameStartIndex = 25;
    // 도메인 내부 모든 컨트롤러 모든 메소드에 적용
    @Around("execution(* com.gnims.project.domain..*Controller.*(..))")
    public Object doTxTimeLog(ProceedingJoinPoint joinPoint) throws Throwable {
        long txBefore = System.currentTimeMillis();

        Object result = joinPoint.proceed();

        long txAfter = System.currentTimeMillis();
        Long txMillis = txAfter - txBefore;

        if (txMillis <= stressTime) {
            log.info("[API NAME : {}  TRANSACTION TIME : {} ms]", createMethodName(joinPoint), txMillis);
        }

        if (txMillis > stressTime) {
            log.warn("[API NAME : {}  TRANSACTION TIME : {} ms]", txMillis);
            timeLogRepository.save(new Log(txMillis, createMethodName(joinPoint)));
        }

        return result;
    }

    private String createMethodName(ProceedingJoinPoint joinPoint) {
        // 메소드 명
        String methodName = joinPoint.getSignature().getName();
        // 클래스 명
        String declaringTypeName = joinPoint.getSignature().getDeclaringTypeName();
        declaringTypeName = declaringTypeName.substring(declaringTypeNameStartIndex);

        String txSource = declaringTypeName + "." + methodName;
        return txSource;
    }
}

