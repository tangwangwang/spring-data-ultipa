package org.springframework.data.ultipa.core.convert;

import org.springframework.core.convert.ConversionFailedException;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.Timestamp;
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

    private static final ZoneOffset UTC_ZONE = ZoneOffset.UTC;
    private final static LocalDate DEFAULT_LOCAL_DATE = LocalDate.of(1970, 1, 1);

    private Jsr310Converters() {
    }

    /**
     * Returns the converters to be registered.
     */
    static Collection<Object> getConvertersToRegister() {
        List<Object> converters = new ArrayList<>();

        // LocalDate
        converters.add(LocalDateToLocalDateTimeConverter.INSTANCE);
        converters.add(LocalDateToDateConverter.INSTANCE);
        converters.add(LocalDateTimeToLocalDateConverter.INSTANCE);
        converters.add(DateToLocalDateConverter.INSTANCE);

        // LocalTime
        converters.add(LocalTimeToLocalDateTimeConverter.INSTANCE);
        converters.add(LocalTimeToDateConverter.INSTANCE);
        converters.add(LocalDateTimeToLocalTimeConverter.INSTANCE);
        converters.add(DateToLocalTimeConverter.INSTANCE);

        // LocalDateTime
        converters.add(LocalDateTimeToStringConverter.INSTANCE);
        converters.add(DateToLocalDateTimeConverter.INSTANCE);

        //  Date
        converters.add(DateToStringConverter.INSTANCE);
        converters.add(LocalDateTimeToDateConverter.INSTANCE);

        // ZonedDateTime
        converters.add(ZonedDateTimeToLocalDateTimeConverter.INSTANCE);
        converters.add(ZonedDateTimeToDateConverter.INSTANCE);
        converters.add(LocalDateTimeToZonedDateTimeConverter.INSTANCE);
        converters.add(DateToZonedDateTimeConverter.INSTANCE);

        // Instant
        converters.add(InstantToLocalDateTimeConverter.INSTANCE);
        converters.add(InstantToDateConverter.INSTANCE);
        converters.add(LocalDateTimeToInstantConverter.INSTANCE);
        converters.add(DateToInstantConverter.INSTANCE);

        // Timestamp
        converters.add(LocalDateTimeToTimestampConverter.INSTANCE);
        converters.add(DateToTimestampConverter.INSTANCE);

        // millis
        converters.add(LocalDateTimeToMillisConverterFactory.INSTANCE);
        converters.add(DateToMillisConverterFactory.INSTANCE);
        converters.add(InstantToMillisConverterFactory.INSTANCE);
        converters.add(ZonedDateTimeToMillisConverterFactory.INSTANCE);
        converters.add(LocalDateToMillisConverterFactory.INSTANCE);
        converters.add(LocalTimeToMillisConverterFactory.INSTANCE);
        converters.add(MillisToLocalDateTimeConverter.INSTANCE);
        converters.add(MillisToDateConverter.INSTANCE);
        converters.add(MillisToInstantConverter.INSTANCE);
        converters.add(MillisToTimestampConverter.INSTANCE);
        converters.add(MillisToZonedDateTimeConverter.INSTANCE);
        converters.add(MillisToLocalDateConverter.INSTANCE);
        converters.add(MillisToLocalTimeConverter.INSTANCE);

        return converters;
    }

    @ReadingConverter
    enum LocalDateToLocalDateTimeConverter implements Converter<LocalDate, LocalDateTime> {
        INSTANCE;

        @Override
        public LocalDateTime convert(LocalDate source) {
            return source.atStartOfDay();
        }
    }

    @ReadingConverter
    enum LocalDateToDateConverter implements Converter<LocalDate, Date> {
        INSTANCE;

        @Override
        public Date convert(LocalDate source) {
            return Date.from(source.atStartOfDay(UTC_ZONE).toInstant());
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
    enum DateToLocalDateConverter implements Converter<Date, LocalDate> {
        INSTANCE;

        @Override
        public LocalDate convert(Date source) {
            return source.toInstant().atZone(UTC_ZONE).toLocalDate();
        }
    }

    @ReadingConverter
    enum LocalTimeToLocalDateTimeConverter implements Converter<LocalTime, LocalDateTime> {
        INSTANCE;

        @Override
        public LocalDateTime convert(LocalTime source) {
            return source.atDate(DEFAULT_LOCAL_DATE);
        }
    }

    @ReadingConverter
    enum LocalTimeToDateConverter implements Converter<LocalTime, Date> {
        INSTANCE;

        @Override
        public Date convert(LocalTime source) {
            return Date.from(source.atDate(DEFAULT_LOCAL_DATE).toInstant(UTC_ZONE));
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
    enum DateToLocalTimeConverter implements Converter<Date, LocalTime> {
        INSTANCE;

        @Override
        public LocalTime convert(Date source) {
            return source.toInstant().atZone(UTC_ZONE).toLocalTime();
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

    @ReadingConverter
    enum DateToLocalDateTimeConverter implements Converter<Date, LocalDateTime> {
        INSTANCE;

        @Override
        public LocalDateTime convert(Date source) {
            return source.toInstant().atZone(UTC_ZONE).toLocalDateTime();
        }
    }

    @ReadingConverter
    enum LocalDateTimeToDateConverter implements Converter<LocalDateTime, Date> {
        INSTANCE;

        @Override
        public Date convert(LocalDateTime source) {
            return Date.from(source.toInstant(UTC_ZONE));
        }
    }

    @ReadingConverter
    enum ZonedDateTimeToLocalDateTimeConverter implements Converter<ZonedDateTime, LocalDateTime> {
        INSTANCE;

        @Override
        public LocalDateTime convert(ZonedDateTime source) {
            return source.withZoneSameInstant(UTC_ZONE).toLocalDateTime();
        }
    }

    @ReadingConverter
    enum ZonedDateTimeToDateConverter implements Converter<ZonedDateTime, Date> {
        INSTANCE;

        @Override
        public Date convert(ZonedDateTime source) {
            return Date.from(source.toInstant());
        }
    }

    @ReadingConverter
    enum LocalDateTimeToZonedDateTimeConverter implements Converter<LocalDateTime, ZonedDateTime> {
        INSTANCE;

        @Override
        public ZonedDateTime convert(LocalDateTime source) {
            return source.atZone(UTC_ZONE);
        }
    }

    @ReadingConverter
    enum DateToZonedDateTimeConverter implements Converter<Date, ZonedDateTime> {
        INSTANCE;

        @Override
        public ZonedDateTime convert(Date source) {
            return source.toInstant().atZone(UTC_ZONE);
        }
    }

    @ReadingConverter
    enum InstantToLocalDateTimeConverter implements Converter<Instant, LocalDateTime> {
        INSTANCE;

        @Override
        public LocalDateTime convert(Instant source) {
            return source.atZone(UTC_ZONE).toLocalDateTime();
        }
    }

    @ReadingConverter
    enum InstantToDateConverter implements Converter<Instant, Date> {
        INSTANCE;

        @Override
        public Date convert(Instant source) {
            return Date.from(source);
        }
    }

    @ReadingConverter
    enum LocalDateTimeToInstantConverter implements Converter<LocalDateTime, Instant> {
        INSTANCE;

        @Override
        public Instant convert(LocalDateTime source) {
            return source.toInstant(UTC_ZONE);
        }
    }

    @ReadingConverter
    enum DateToInstantConverter implements Converter<Date, Instant> {
        INSTANCE;

        @Override
        public Instant convert(Date source) {
            return source.toInstant();
        }
    }

    @WritingConverter
    enum DateToStringConverter implements Converter<Date, String> {
        INSTANCE;

        private static final DateTimeFormatter ZONED_DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        @Override
        public String convert(Date source) {
            return source.toInstant().atZone(UTC_ZONE).format(ZONED_DATE_TIME_FORMAT);
        }
    }

    @ReadingConverter
    enum LocalDateTimeToTimestampConverter implements Converter<LocalDateTime, Timestamp> {
        INSTANCE;

        @Override
        public Timestamp convert(LocalDateTime source) {
            return Timestamp.from(source.toInstant(UTC_ZONE));
        }
    }

    @ReadingConverter
    enum DateToTimestampConverter implements Converter<Date, Timestamp> {
        INSTANCE;

        @Override
        public Timestamp convert(Date source) {
            return Timestamp.from(source.toInstant());
        }
    }

    @ReadingConverter
    enum MillisToLocalDateTimeConverter implements Converter<Number, LocalDateTime> {
        INSTANCE;

        @Override
        public LocalDateTime convert(Number source) {
            return Instant.ofEpochMilli(source.longValue()).atZone(UTC_ZONE).toLocalDateTime();
        }
    }

    @ReadingConverter
    enum MillisToDateConverter implements Converter<Number, Date> {
        INSTANCE;

        @Override
        public Date convert(Number source) {
            return Date.from(Instant.ofEpochMilli(source.longValue()));
        }
    }

    @ReadingConverter
    enum LocalDateTimeToMillisConverterFactory implements ConverterFactory<LocalDateTime, Number> {
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
                return getMilli(source.toInstant(UTC_ZONE), numberType);
            }
        }
    }

    @ReadingConverter
    enum DateToMillisConverterFactory implements ConverterFactory<Date, Number> {
        INSTANCE;

        @Override
        public <T extends Number> Converter<Date, T> getConverter(Class<T> targetType) {
            return new DateToNumberConverter<>(targetType);
        }

        private static class DateToNumberConverter<T extends Number> implements Converter<Date, T> {

            private final Class<T> numberType;

            DateToNumberConverter(Class<T> numberType) {
                this.numberType = numberType;
            }

            @Override
            public T convert(Date source) {
                return getMilli(source.toInstant(), numberType);
            }
        }
    }

    @ReadingConverter
    enum InstantToMillisConverterFactory implements ConverterFactory<Instant, Number> {
        INSTANCE;

        @Override
        public <T extends Number> Converter<Instant, T> getConverter(Class<T> targetType) {
            return new InstantToNumberConverter<>(targetType);
        }

        private static class InstantToNumberConverter<T extends Number> implements Converter<Instant, T> {

            private final Class<T> numberType;

            InstantToNumberConverter(Class<T> numberType) {
                this.numberType = numberType;
            }

            @Override
            public T convert(Instant source) {
                return getMilli(source, numberType);
            }
        }
    }

    @ReadingConverter
    enum ZonedDateTimeToMillisConverterFactory implements ConverterFactory<ZonedDateTime, Number> {
        INSTANCE;

        @Override
        public <T extends Number> Converter<ZonedDateTime, T> getConverter(Class<T> targetType) {
            return new ZonedDateTimeToNumberConverter<>(targetType);
        }

        private static class ZonedDateTimeToNumberConverter<T extends Number> implements Converter<ZonedDateTime, T> {

            private final Class<T> numberType;

            ZonedDateTimeToNumberConverter(Class<T> numberType) {
                this.numberType = numberType;
            }

            @Override
            public T convert(ZonedDateTime source) {
                return getMilli(source.toInstant(), numberType);
            }
        }
    }

    @ReadingConverter
    enum LocalDateToMillisConverterFactory implements ConverterFactory<LocalDate, Number> {
        INSTANCE;

        @Override
        public <T extends Number> Converter<LocalDate, T> getConverter(Class<T> targetType) {
            return new LocalDateToNumberConverter<>(targetType);
        }

        private static class LocalDateToNumberConverter<T extends Number> implements Converter<LocalDate, T> {

            private final Class<T> numberType;

            LocalDateToNumberConverter(Class<T> numberType) {
                this.numberType = numberType;
            }

            @Override
            public T convert(LocalDate source) {
                return getMilli(source.atStartOfDay().toInstant(UTC_ZONE), numberType);
            }
        }
    }

    @ReadingConverter
    enum LocalTimeToMillisConverterFactory implements ConverterFactory<LocalTime, Number> {
        INSTANCE;

        @Override
        public <T extends Number> Converter<LocalTime, T> getConverter(Class<T> targetType) {
            return new LocalTimeToNumberConverter<>(targetType);
        }

        private static class LocalTimeToNumberConverter<T extends Number> implements Converter<LocalTime, T> {

            private final Class<T> numberType;

            LocalTimeToNumberConverter(Class<T> numberType) {
                this.numberType = numberType;
            }

            @Override
            public T convert(LocalTime source) {
                return getMilli(source.atDate(DEFAULT_LOCAL_DATE).toInstant(UTC_ZONE), numberType);
            }
        }
    }


    private static <T extends Number> T getMilli(Instant source, Class<T> numberType) {
        long milli = source.toEpochMilli();
        try {
            Constructor<T> constructor = numberType.getConstructor(String.class);
            return constructor.newInstance(String.valueOf(milli));
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException |
                 IllegalAccessException e) {
            throw new ConversionFailedException(TypeDescriptor.forObject(source), TypeDescriptor.valueOf(numberType), source, e);
        }
    }

    @ReadingConverter
    enum MillisToInstantConverter implements Converter<Number, Instant> {
        INSTANCE;

        @Override
        public Instant convert(Number source) {
            return Instant.ofEpochMilli(source.longValue());
        }
    }

    @ReadingConverter
    enum MillisToTimestampConverter implements Converter<Number, Timestamp> {
        INSTANCE;

        @Override
        public Timestamp convert(Number source) {
            return new Timestamp(source.longValue());
        }
    }

    @ReadingConverter
    enum MillisToZonedDateTimeConverter implements Converter<Number, ZonedDateTime> {
        INSTANCE;

        @Override
        public ZonedDateTime convert(Number source) {
            return Instant.ofEpochMilli(source.longValue()).atZone(UTC_ZONE);
        }
    }

    @ReadingConverter
    enum MillisToLocalDateConverter implements Converter<Number, LocalDate> {
        INSTANCE;

        @Override
        public LocalDate convert(Number source) {
            return Instant.ofEpochMilli(source.longValue()).atZone(UTC_ZONE).toLocalDateTime().toLocalDate();
        }
    }

    @ReadingConverter
    enum MillisToLocalTimeConverter implements Converter<Number, LocalTime> {
        INSTANCE;

        @Override
        public LocalTime convert(Number source) {
            return Instant.ofEpochMilli(source.longValue()).atZone(UTC_ZONE).toLocalDateTime().toLocalTime();
        }
    }

}
