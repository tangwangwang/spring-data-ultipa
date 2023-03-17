package org.springframework.data.ultipa.core.mapping.model;

import org.springframework.data.mapping.model.SimpleTypeHolder;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.*;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * @author Wangwang Tang
 * @since 1.0
 */
public class UltipaSimpleTypeHolder extends SimpleTypeHolder {

    private static final Set<Class<?>> ULTIPA_SIMPLE_TYPES;

    static {
        Set<Class<?>> simpleTypes = new HashSet<>();
        simpleTypes.add(UUID.class);

        // date type support
        simpleTypes.add(LocalDate.class);
        simpleTypes.add(LocalTime.class);
        simpleTypes.add(LocalDateTime.class);
        simpleTypes.add(Instant.class);
        simpleTypes.add(Period.class);
        simpleTypes.add(Duration.class);

        simpleTypes.add(BigDecimal.class);
        simpleTypes.add(BigInteger.class);

        ULTIPA_SIMPLE_TYPES = Collections.unmodifiableSet(simpleTypes);
    }

    public static final SimpleTypeHolder HOLDER = new UltipaSimpleTypeHolder();

    private UltipaSimpleTypeHolder() {
        super(ULTIPA_SIMPLE_TYPES, true);
    }

}
