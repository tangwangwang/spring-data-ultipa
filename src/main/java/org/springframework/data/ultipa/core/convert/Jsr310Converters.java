package org.springframework.data.ultipa.core.convert;

import org.springframework.core.convert.ConversionFailedException;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * Helper class to register JSR-310 specific {@link Converter} implementations for Ultipa.
 *
 * @author Wangwang Tang
 * @since 1.0
 */
abstract class Jsr310Converters {

    private static final ZoneId ZONE = ZoneOffset.UTC;

    private Jsr310Converters() {
    }

    /**
     * Returns the converters to be registered.
     */
    static Collection<Object> getConvertersToRegister() {
        List<Object> converters = new ArrayList<>();

        converters.add(LocalDateTimeToInstantConverter.INSTANCE);
        converters.add(LocalDateTimeToLocalDateConverter.INSTANCE);
        converters.add(LocalDateTimeToLocalTimeConverter.INSTANCE);
        converters.add(LocalDateTimeToDateConverter.INSTANCE);
        converters.add(LocalDateTimeToNumberConverterFactory.INSTANCE);

        converters.add(InstantToLocalDateTimeConverter.INSTANCE);
        converters.add(LocalDateToLocalDateTimeConverter.INSTANCE);
        converters.add(LocalTimeToLocalDateTimeConverter.INSTANCE);
        converters.add(DateToLocalDateTimeConverter.INSTANCE);
        converters.add(MillisToLocalDateTimeConverter.INSTANCE);

        converters.add(LocalDateTimeToStringConverter.INSTANCE);
        converters.add(LocalDateToStringConverter.INSTANCE);
        converters.add(LocalTimeToStringConverter.INSTANCE);
        return converters;
    }

    @ReadingConverter
    enum LocalDateTimeToInstantConverter implements Converter<LocalDateTime, Instant> {
        INSTANCE;

        @Override
        public Instant convert(LocalDateTime source) {
            return source.atZone(ZONE).toInstant();
        }
    }

    @ReadingConverter
    enum LocalDateTimeToLocalDateConverter implements Converter<LocalDateTime, LocalDate> {
        INSTANCE;

        @Override
        public LocalDate convert(LocalDateTime source) {
            return source.toLocalDate();
        }
    }

    @ReadingConverter
    enum LocalDateTimeToLocalTimeConverter implements Converter<LocalDateTime, LocalTime> {
        INSTANCE;

        @Override
        public LocalTime convert(LocalDateTime source) {
            return source.toLocalTime();
        }
    }

    @ReadingConverter
    enum LocalDateTimeToDateConverter implements Converter<LocalDateTime, Date> {
        INSTANCE;

        @Override
        public Date convert(LocalDateTime source) {
            return Date.from(source.atZone(ZONE).toInstant());
        }
    }

    @ReadingConverter
    enum LocalDateTimeToNumberConverterFactory implements ConverterFactory<LocalDateTime, Number> {
        INSTANCE;

        @Override
        public <T extends Number> Converter<LocalDateTime, T> getConverter(Class<T> targetType) {
            return new LocalDateTimeToNumberConverter<>(targetType);
        }

        private static class LocalDateTimeToNumberConverter<T extends Number> implements Converter<LocalDateTime, T> {

            private final Class<T> numberType;

            LocalDateTimeToNumberConverter(Class<T> numberType) {
                this.numberType = numberType;
            }

            @Override
            public T convert(LocalDateTime source) {
                long milli = source.atZone(ZONE).toInstant().toEpochMilli();
                try {
                    Constructor<T> constructor = numberType.getConstructor(String.class);
                    return constructor.newInstance(String.valueOf(milli));
                } catch (NoSuchMethodException | InvocationTargetException | InstantiationException |
                         IllegalAccessException e) {
                    throw new ConversionFailedException(TypeDescriptor.forObject(source), TypeDescriptor.valueOf(numberType), source, e);
                }
            }
        }
    }

    @WritingConverter
    enum InstantToLocalDateTimeConverter implements Converter<Instant, LocalDateTime> {
        INSTANCE;

        @Override
        public LocalDateTime convert(Instant source) {
            return source.atZone(ZONE).toLocalDateTime();
        }
    }

    @WritingConverter
    enum LocalDateToLocalDateTimeConverter implements Converter<LocalDate, LocalDateTime> {
        INSTANCE;

        @Override
        public LocalDateTime convert(LocalDate source) {
            return source.atStartOfDay();
        }
    }

    @WritingConverter
    enum LocalTimeToLocalDateTimeConverter implements Converter<LocalTime, LocalDateTime> {
        INSTANCE;

        @Override
        public LocalDateTime convert(LocalTime source) {
            return source.atDate(LocalDate.now());
        }
    }

    @WritingConverter
    enum DateToLocalDateTimeConverter implements Converter<Date, LocalDateTime> {
        INSTANCE;

        @Override
        public LocalDateTime convert(Date source) {
            return source.toInstant().atZone(ZONE).toLocalDateTime();
        }
    }

    @WritingConverter
    enum MillisToLocalDateTimeConverter implements Converter<Long, LocalDateTime> {
        INSTANCE;

        @Override
        public LocalDateTime convert(Long source) {
            return Instant.ofEpochMilli(source).atZone(ZONE).toLocalDateTime();
        }
    }

    @WritingConverter
    enum LocalDateTimeToStringConverter implements Converter<LocalDateTime, String> {
        INSTANCE;

        private final static DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS");

        @Override
        public String convert(LocalDateTime source) {
            return source.format(DATE_FORMAT);
        }
    }

    @WritingConverter
    enum LocalDateToStringConverter implements Converter<LocalDate, String> {
        INSTANCE;

        private final static DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd 00:00:00");

        @Override
        public String convert(LocalDate source) {
            return source.format(DATE_FORMAT);
        }
    }

    @WritingConverter
    enum LocalTimeToStringConverter implements Converter<LocalTime, String> {
        INSTANCE;

        private final static DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("00-00-00 HH:mm:ss.SSSSSS");

        @Override
        public String convert(LocalTime source) {
            return source.format(TIME_FORMAT);
        }
    }
}
