package org.springframework.data.ultipa.core.convert;

import org.springframework.core.convert.ConversionFailedException;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.springframework.data.convert.ConverterBuilder.reading;

/**
 * Wrapper class to contain useful converters for the usage with Ultipa.
 *
 * @author Wangwang Tang
 * @since 1.0
 */
abstract class UltipaConverters {

    private UltipaConverters() {
    }

    /**
     * Returns the converters to be registered.
     */
    static Collection<Object> getConvertersToRegister() {

        List<Object> converters = new ArrayList<>();

        converters.add(NumberToBooleanConverter.INSTANCE);
        converters.add(StringToBooleanConverter.INSTANCE);
        converters.add(StringToNumberConverterFactory.INSTANCE);

        converters.add(BooleanToNumberConverter.INSTANCE);
        converters.add(BooleanToStringConverter.INSTANCE);
        converters.add(NumberToStringConverter.INSTANCE);

        converters.add(reading(String.class, URI.class, URI::create).andWriting(URI::toString));
        return converters;
    }

    @ReadingConverter
    enum NumberToBooleanConverter implements Converter<Number, Boolean> {
        INSTANCE;

        @Override
        public Boolean convert(Number source) {
            return source.intValue() == 0 ? Boolean.FALSE : Boolean.TRUE;
        }
    }

    @ReadingConverter
    enum StringToBooleanConverter implements Converter<String, Boolean> {
        INSTANCE;

        @Override
        public Boolean convert(String source) {
            return Boolean.parseBoolean(source);
        }
    }

    @ReadingConverter
    enum StringToNumberConverterFactory implements ConverterFactory<String, Number> {
        INSTANCE;

        @Override
        public <T extends Number> Converter<String, T> getConverter(Class<T> targetType) {
            return new StringToNumberConverter<>(targetType);
        }

        private static class StringToNumberConverter<T extends Number> implements Converter<String, T> {

            private final Class<T> numberType;

            StringToNumberConverter(Class<T> numberType) {
                this.numberType = numberType;
            }

            @Override
            public T convert(String source) {
                try {
                    Constructor<T> constructor = numberType.getConstructor(String.class);
                    return constructor.newInstance(source);
                } catch (NoSuchMethodException | InvocationTargetException | InstantiationException |
                         IllegalAccessException e) {
                    throw new ConversionFailedException(TypeDescriptor.forObject(source), TypeDescriptor.valueOf(numberType), source, e);
                }
            }
        }
    }

    @WritingConverter
    enum BooleanToNumberConverter implements Converter<Boolean, Number> {
        INSTANCE;

        @Override
        public Number convert(Boolean source) {
            return source ? 1 : 0;
        }
    }

    @WritingConverter
    enum BooleanToStringConverter implements Converter<Boolean, String> {
        INSTANCE;

        @Override
        public String convert(Boolean source) {
            return source.toString();
        }
    }

    @WritingConverter
    enum NumberToStringConverter implements Converter<Number, String> {
        INSTANCE;

        @Override
        public String convert(Number source) {
            return source instanceof BigDecimal ? ((BigDecimal) source).toPlainString() : source.toString();
        }
    }

}
