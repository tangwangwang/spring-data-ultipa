package org.springframework.data.ultipa.repository.support;

import org.springframework.data.repository.core.EntityInformation;

/**
 * @author Wangwang Tang
 * @since 1.0
 */
public interface UltipaEntityInformation<T, ID> extends EntityInformation<T, ID> {

    String getSchemaName();

    String getIdPropertyName();

    boolean isNode();

    boolean isEdge();
}
