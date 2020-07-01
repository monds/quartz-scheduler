package com.monds.scheduler;

import com.monds.scheduler.config.JasyptConfig;
import com.ulisesbocchio.jasyptspringboot.configuration.EnableEncryptablePropertiesConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Import({
    QuartzJobScheduler.class,
    JasyptConfig.class,
    EnableEncryptablePropertiesConfiguration.class
})
public @interface EnableQuartzScheduler {
}
