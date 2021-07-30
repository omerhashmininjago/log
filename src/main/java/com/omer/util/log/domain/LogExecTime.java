package com.omer.util.log.domain;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface LogExecTime {

    LogLevel level() default LogLevel.INFO;

    enum LogLevel {
        TRACE, DEBUG, INFO, WARN, ERROR;
    }

}