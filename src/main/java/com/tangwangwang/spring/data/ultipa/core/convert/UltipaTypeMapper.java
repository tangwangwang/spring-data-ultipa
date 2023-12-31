package com.tangwangwang.spring.data.ultipa.core.convert;

import com.tangwangwang.spring.data.ultipa.core.mapping.UltipaPersistentEntity;
import com.tangwangwang.spring.data.ultipa.core.mapping.UltipaPersistentProperty;
import com.tangwangwang.spring.data.ultipa.core.schema.Schema;
import org.springframework.data.convert.TypeMapper;
import org.springframework.data.mapping.context.MappingContext;

/**
 * Ultipa specific {@link TypeMapper} definition.
 *
 * @author Wangwang Tang
 * @since 1.0
 */
public interface UltipaTypeMapper extends TypeMapper<Schema> {

    String DEFAULT_TYPE_KEY = "_class";

    /**
     * Returns whether the given key is the type key.
     *
     * @return {@literal true} if given {@literal key} is used as type hint key.
     */
    boolean isTypeKey(String key);

    /**
     * Creates a new default {@link UltipaTypeMapper}.
     *
     * @param mappingContext the mapping context.
     * @return a new default {@link UltipaTypeMapper}.
     * @see DefaultUltipaTypeMapper
     */
    static DefaultUltipaTypeMapper create(
            MappingContext<? extends UltipaPersistentEntity<?>, UltipaPersistentProperty> mappingContext) {
        return new DefaultUltipaTypeMapper(DEFAULT_TYPE_KEY, mappingContext);
    }
}
