package org.springframework.data.ultipa.repository.config;

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
     * Configures the {@link AuditorAware} bean to be used to lookup the current principal.
     */
    String auditorAwareRef() default "";

    /**
     * Configures whether the creation and modification dates are set. Defaults to {@literal true}.
     */
    boolean setDates() default true;

    /**
     * Configures whether the entity shall be marked as modified on creation. Defaults to {@literal true}.
     */
    boolean modifyOnCreate() default true;

    /**
     * Configures a {@link DateTimeProvider} bean name that allows customizing the {@link java.time.temporal.TemporalAccessor} to be
     * used for setting creation and modification dates.
     */
    String dateTimeProviderRef() default "";

}
