package ru.roborox.crawler.anotation;

import ru.roborox.crawler.domain.ReloadType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Reload {
    ReloadType value() default ReloadType.NEVER;
    Duration rate() default @Duration;
}
