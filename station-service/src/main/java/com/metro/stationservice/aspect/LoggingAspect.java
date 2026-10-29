package com.metro.stationservice.aspect;

import java.util.Arrays;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Aspect
@Component
public class LoggingAspect {
	private static final Logger log = LoggerFactory.getLogger(LoggingAspect.class);
	
	@Pointcut("execution(* com.metro.stationservice.service..*(..))")
	public void serviceLayer() {}
	//serviceLayer - empty method itself -named placeholder for this rule so other annotation can use it by name
	
	
    // Pointcut: every method in your controller layer
	@Pointcut("execution(* com.metro.stationservice.controller..*(..))")
	public void controllerLayer() {}
	
    // --- Log method entry with arguments (service layer) ---
	//jp -object represent the intercepted method
	@Before("serviceLayer()")
	public void logServiceEntry(JoinPoint jp) {
	    log.info("→ Entering {}.{}() with args: {}",
	    		//full name where method is defined
	            jp.getSignature().getDeclaringTypeName(),
	            jp.getSignature().getName(),
	            Arrays.toString(jp.getArgs()));
	}

    // --- Log return value (service layer) ---
	@AfterReturning(pointcut = "serviceLayer()", returning = "result")
	public void logServiceExit(JoinPoint jp, Object result) {
	    log.info("← Exiting {}.{}() with result: {}",
	            jp.getSignature().getDeclaringTypeName(),
	            jp.getSignature().getName(),
	            result);
	}
	  // --- Log exceptions thrown from service layer, before GlobalExceptionHandler catches them ---
	@AfterThrowing(pointcut = "serviceLayer()", throwing = "ex")
	public void logServiceException(JoinPoint jp, Throwable ex) {
	    log.error("✕ Exception in {}.{}(): {}",
	            jp.getSignature().getDeclaringTypeName(),
	            jp.getSignature().getName(),
	            ex.getMessage());
	}
    // --- Log execution time for controller endpoints ---
    @Around("controllerLayer()")
    public Object logControllerExecutionTime(ProceedingJoinPoint pjp) throws Throwable {
        long start = System.currentTimeMillis();
        log.info("API call started: {}", pjp.getSignature().toShortString());

        Object result = pjp.proceed(); // actual controller method runs here

        long duration = System.currentTimeMillis() - start;
        log.info("API call finished: {} in {} ms", pjp.getSignature().toShortString(), duration);

        return result;
    }
	
	
}

