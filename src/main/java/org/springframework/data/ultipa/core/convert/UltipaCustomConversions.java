package org.springframework.data.ultipa.core.convert;

import org.springframework.data.convert.CustomConversions;
import org.springframework.data.mapping.model.SimpleTypeHolder;
import org.springframework.data.ultipa.core.mapping.model.UltipaSimpleTypeHolder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Value object to capture custom conversion. {@link UltipaCustomConversions} also act as factory for
 * {@link SimpleTypeHolder}.
 *
 * @author Wangwang Tang
 * @since 1.0
 */
public class UltipaCustomConversions extends CustomConversions {

    private static final List<Object> STORE_CONVERTERS;

    private static final StoreConversions STORE_CONVERSIONS;

    static {
        List<Object> converters = new ArrayList<>();

        converters.addAll(UltipaConverters.getConvertersToRegister());
        converters.addAll(Jsr310Converters.getConvertersToRegister());
        converters.addAll(GeoConverters.getConvertersToRegister());

        STORE_CONVERTERS = Collections.unmodifiableList(converters);
        STORE_CONVERSIONS = StoreConversions.of(UltipaSimpleTypeHolder.HOLDER, STORE_CONVERTERS);
    }

    /**
     * Creates an empty {@link UltipaCustomConversions} object.
     */
    public UltipaCustomConversions() {
        this(Collections.emptyList());
    }

    /**
     * Create a new {@link UltipaCustomConversions} instance registering the given converters.
     *
     * @param converters must not be {@literal null}.
     */
    public UltipaCustomConversions(List<?> converters) {
        super(STORE_CONVERSIONS, converters);
    }

}
