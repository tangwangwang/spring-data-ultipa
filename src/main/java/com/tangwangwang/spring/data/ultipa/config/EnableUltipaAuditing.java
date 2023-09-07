package com.tangwangwang.spring.data.ultipa.config;

import org.springframework.context.annotation.Import;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.domain.AuditorAware;

import java.lang.annotation.*;

/**
 * @author Wangwang Tang
 * @since 1.0
 */
@Inherited
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(UltipaAuditingRegistrar.class)
public @interface EnableUltipaAuditing {

    /**
     * Configures the {@link AuditorAware} bean to be used to look up the current principal.
     *
     * @return The name of the {@link AuditorAware} bean to be used to look up the current principal.
     */
    String auditorAwareRef() default "";

    /**
     * Configures whether the creation and modification dates are set. Defaults to {@literal true}.
     *
     * @return whether to set the creation and modification dates.
     */
    boolean setDates() default true;

    /**
     * Configures whether the entity shall be marked as modified on creation. Defaults to {@literal true}.
     *
     * @return whether to mark the entity as modified on creation.
     */
    boolean modifyOnCreate() default true;

    /**
     * Configures a {@link DateTimeProvider} bean name that allows customizing the {@link java.time.temporal.TemporalAccessor} to be
     * used for setting creation and modification dates.
     *
     * @return The name of the {@link DateTimeProvider} bean to provide the current date time for creation and modification dates.
     */
    String dateTimeProviderRef() default "";

}
