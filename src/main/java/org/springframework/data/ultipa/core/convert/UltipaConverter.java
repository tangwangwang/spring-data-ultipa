package org.springframework.data.ultipa.core.convert;

import org.springframework.data.convert.EntityConverter;
import org.springframework.data.ultipa.core.mapping.UltipaPersistentEntity;
import org.springframework.data.ultipa.core.mapping.UltipaPersistentProperty;

import java.util.Map;

/**
 * @author Wangwang Tang
 * @since 1.0
 */
public interface UltipaConverter extends EntityConverter<UltipaPersistentEntity<?>, UltipaPersistentProperty, Object, Map<String, Object>> {
}
