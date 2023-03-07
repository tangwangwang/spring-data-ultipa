package org.springframework.data.ultipa.core;

import org.springframework.data.ultipa.core.convert.UltipaConverter;

/**
 * @author Wangwang Tang
 * @since 1.0
 */
public interface UltipaOperations {

    /**
     * Returns the underlying {@link UltipaConverter}.
     *
     * @return never {@literal null}.
     */
    UltipaConverter getConverter();
}
