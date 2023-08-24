package org.springframework.data.ultipa.core.convert;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.geo.Point;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Helper class to register geo structure {@link Converter} implementations for Ultipa.
 *
 * @author Wangwang Tang
 * @since 1.0
 */
abstract class GeoConverters {

    private static final String POINT_FORMAT = "POINT( %s %s )";

    private GeoConverters() {
    }

    /**
     * Returns the converters to be registered.
     */
    static Collection<Object> getConvertersToRegister() {
        List<Object> converters = new ArrayList<>();

        converters.add(PointToStringConverter.INSTANCE);
        converters.add(UltipaPointToGeoPointConverter.INSTANCE);
        return converters;
    }

    @WritingConverter
    enum PointToStringConverter implements Converter<Point, String> {
        INSTANCE;

        @Override
        public String convert(Point source) {
            return String.format(POINT_FORMAT, source.getX(), source.getY());
        }
    }

    @ReadingConverter
    enum UltipaPointToGeoPointConverter implements Converter<com.ultipa.sdk.data.Point, Point> {
        INSTANCE;

        @Override
        public Point convert(com.ultipa.sdk.data.Point source) {
            return new Point(source.getLatitude(), source.getLongitude());
        }
    }
}
