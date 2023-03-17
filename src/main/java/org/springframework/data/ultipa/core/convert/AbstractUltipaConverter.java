package org.springframework.data.ultipa.core.convert;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.data.convert.CustomConversions;
import org.springframework.data.mapping.model.EntityInstantiators;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * Base class for {@link UltipaConverter} implementations. Sets up a {@link GenericConversionService} and populates basic
 * converters. Allows registering {@link CustomConversions}.
 *
 * @author Wangwang Tang
 * @since 1.0
 */
public abstract class AbstractUltipaConverter implements UltipaConverter, InitializingBean {

    protected final GenericConversionService conversionService;
    protected CustomConversions conversions = new UltipaCustomConversions();
    protected EntityInstantiators instantiators = new EntityInstantiators();

    /**
     * Creates a new {@link AbstractUltipaConverter} using the given {@link GenericConversionService}.
     *
     * @param conversionService can be {@literal null} and defaults to {@link DefaultConversionService}.
     */
    protected AbstractUltipaConverter(@Nullable GenericConversionService conversionService) {
        this.conversionService = conversionService == null ? new DefaultConversionService() : conversionService;
    }

    /**
     * Registers the given custom conversions with the converter.
     *
     * @param conversions must not be {@literal null}.
     */
    public void setCustomConversions(CustomConversions conversions) {

        Assert.notNull(conversions, "Conversions must not be null!");
        this.conversions = conversions;
    }

    /**
     * Registers {@link EntityInstantiators} to customize entity instantiation.
     *
     * @param instantiators can be {@literal null}. Uses default {@link EntityInstantiators} if so.
     */
    public void setInstantiators(@Nullable EntityInstantiators instantiators) {
        this.instantiators = instantiators == null ? new EntityInstantiators() : instantiators;
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.mongodb.core.core.convert.UltipaConverter#getConversionService()
     */
    @Override
    public ConversionService getConversionService() {
        return conversionService;
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    public void afterPropertiesSet() {
        initializeConverters();
    }

    /**
     * Registers additional converters that will be available when using the {@link ConversionService} directly.
     * These converters are not custom conversions as they'd introduce unwanted conversions.
     */
    private void initializeConverters() {
        ConversionService conversionService = getConversionService();

        if (conversionService instanceof GenericConversionService) {
            this.conversions.registerConvertersIn((GenericConversionService) conversionService);
        }
    }

}
