package com.tangwangwang.spring.data.ultipa.annotation;

import com.tangwangwang.spring.data.ultipa.core.schema.IdGenerator;
import org.springframework.core.annotation.AliasFor;
import org.springframework.data.annotation.Id;

import java.lang.annotation.*;
import java.util.UUID;

/**
 * Indicates a generated id. Ids can be generated internally. by the database itself or by an external generator. This
 * annotation defaults to the internally generated ids.
 *
 * @author Wangwang Tang
 * @since 1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Documented
@Inherited
public @interface GeneratedValue {

    /**
     * @return The generator to use.
     * @see #generatorClass()
     */
    @AliasFor("generatorClass")
    Class<? extends IdGenerator<?>> value() default GeneratedValue.InternalIdGenerator.class;

    /**
     * @return The generator to use. Defaults to {@link InternalIdGenerator}, which indicates database generated values.
     */
    @AliasFor("value")
    Class<? extends IdGenerator<?>> generatorClass() default GeneratedValue.InternalIdGenerator.class;

    /**
     * @return An optional reference to a bean to be used as ID generator.
     */
    String generatorRef() default "";

    /**
     * This {@link IdGenerator} does nothing. It is used for relying on the internal, database-side created id.
     */
    enum InternalIdGenerator implements IdGenerator<Void> {

        INSTANCE;

        @Override
        public Void generateId(String primaryLabel, Object entity) {
            return null;
        }
    }

    /**
     * This generator is automatically applied when a field of type {@link java.util.UUID} is annotated with
     * {@link Id @Id} and {@link GeneratedValue @GeneratedValue}.
     */
    enum UUIDGenerator implements IdGenerator<UUID> {

        INSTANCE;

        @Override
        public UUID generateId(String primaryLabel, Object entity) {
            return UUID.randomUUID();
        }
    }

}
