package com.omer.util.log.interceptor;

import com.google.common.collect.ImmutableMap;
import com.omer.util.log.domain.LogExecTime;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import com.google.common.base.Stopwatch;

import java.util.concurrent.TimeUnit;

import static java.lang.String.format;

/**
 * <p>
 * The {@link LoggingInterceptor} would need to be applied
 * on methods or classes, where the execution time needs to
 * be captured
 * </p>
 * <p>
 * This will be done by annotating the candidate class
 * or method using {@link LogExecTime}
 * </p>
 * <p>
 * {@link LogExecTime} would take in a variable which would
 * determine at what level the log needs to be captured
 * </p>
 * <p>
 * By default the execution time would be logged at INFO level
 * </p>
 */
@Component
public class LoggingInterceptor {

    private static final Logger LOG = LoggerFactory.getLogger(LoggingInterceptor.class);
    private static final String WARNING_MESSAGE = "Exception occured while reading the message signature..";
    private static final String PRE_MESSAGE = "Entering method - %s of %s";
    private static final String POST_MESSAGE = "Exiting method - %s of $s: Total time taken - %s %s";

    private static final LoggingFunction TRACE_LOGGING = message -> LOG.trace((String) message);
    private static final LoggingFunction DEBUG_LOGGING = message -> LOG.debug((String) message);
    private static final LoggingFunction INFO_LOGGING = message -> LOG.info((String) message);
    private static final LoggingFunction WARN_LOGGING = message -> LOG.warn((String) message);
    private static final LoggingFunction ERROR_LOGGING = message -> LOG.error((String) message);

    private static final ImmutableMap<LogExecTime.LogLevel, LoggingFunction> logLevelMapping
            = new ImmutableMap.Builder<LogExecTime.LogLevel, LoggingFunction>()
            .put(LogExecTime.LogLevel.TRACE, TRACE_LOGGING)
            .put(LogExecTime.LogLevel.DEBUG, DEBUG_LOGGING)
            .put(LogExecTime.LogLevel.INFO, INFO_LOGGING)
            .put(LogExecTime.LogLevel.WARN, WARN_LOGGING)
            .put(LogExecTime.LogLevel.ERROR, ERROR_LOGGING)
            .build();


    @Around("classLevelInspection() && @within(logExecTime))")
    public Object classesAnnotatedWithLog(ProceedingJoinPoint proceedingJoinPoint, LogExecTime.LogLevel logLevel) {
        return methodProfilingForLogging(proceedingJoinPoint, logLevel);
    }

    @Around("methodLevelInspection() && @within(logExecTime))")
    public Object publicMethodAnnotatedWith(ProceedingJoinPoint proceedingJoinPoint, LogExecTime.LogLevel logLevel) {
        return methodProfilingForLogging(proceedingJoinPoint, logLevel);
    }

    @Pointcut("within(com..*)")
    public void classLevelInspection() {
    }

    @Pointcut("@annotation(com.omer.util.log.domain.LogExecTime)")
    public void methodLevelInspection() {
    }

    private Object methodProfilingForLogging(ProceedingJoinPoint proceedingJoinPoint, LogExecTime.LogLevel logLevel) {
        Stopwatch stopWatch = null;
        Object returnValue = null;

        try {
            String message;
            LoggingFunction loggingFunction = logLevelMapping.get(logLevel);
            MethodSignature methodSignature = (MethodSignature) proceedingJoinPoint.getSignature();
            String methodName = methodSignature.getMethod().getName();
            Class<?> className = methodSignature.getMethod().getDeclaringClass();
            message = format(PRE_MESSAGE, methodName, className);
            loggingFunction.log(message);
            stopWatch = Stopwatch.createStarted();
            returnValue = proceedingJoinPoint.proceed();
            message = format(POST_MESSAGE, message, className, stopWatch.elapsed(TimeUnit.MILLISECONDS), TimeUnit.MILLISECONDS.name());
            loggingFunction.log(message);
            return returnValue;
        } catch (Throwable e) {
            LOG.warn(WARNING_MESSAGE, e);
        } finally {
            if (null != stopWatch && stopWatch.isRunning()) {
                stopWatch.stop();
            }
        }
        return returnValue;
    }

    @FunctionalInterface
    protected interface LoggingFunction<String> {

        void log(String message);
    }

}
