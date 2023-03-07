package org.springframework.data.ultipa.repository.support;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.core.support.RepositoryFactoryBeanSupport;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;
import org.springframework.data.ultipa.core.UltipaOperations;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.io.Serializable;

/**
 * Adapter for Springs {@link FactoryBean} interface to allow easy setup of {@link UltipaRepositoryFactory} via Spring
 * configuration.
 *
 * @param <T>  the type of the repository
 * @param <S>  type of the domain class to map
 * @param <ID> identifier type in the domain class
 * @author Wangwang Tang
 * @since 1.0
 */
public class UltipaRepositoryFactoryBean<T extends Repository<S, ID>, S, ID extends Serializable>
        extends RepositoryFactoryBeanSupport<T, S, ID> {

    private @Nullable UltipaOperations operations;

    /**
     * Creates a new {@link UltipaRepositoryFactoryBean} for the given repository interface.
     *
     * @param repositoryInterface must not be {@literal null}.
     */
    protected UltipaRepositoryFactoryBean(Class<? extends T> repositoryInterface) {
        super(repositoryInterface);
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.repository.core.support.RepositoryFactoryBeanSupport#createRepositoryFactory()
     */
    @Override
    protected RepositoryFactorySupport createRepositoryFactory() {
        Assert.notNull(operations, "operations are not initialized");

        return new UltipaRepositoryFactory(operations);
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.repository.core.support.RepositoryFactoryBeanSupport#afterPropertiesSet()
     */
    @Override
    public void afterPropertiesSet() {
        super.afterPropertiesSet();
        Assert.notNull(operations, "UltipaOperations must not be configured!");
    }

    /**
     * Configures the {@link UltipaOperations} to be used to create Ultipa repositories.
     *
     * @param operations the operations to set
     */
    public void setUltipaOperations(UltipaOperations operations) {
        Assert.notNull(operations, "UltipaOperations must not be null!");

        setMappingContext(operations.getConverter().getMappingContext());
        this.operations = operations;
    }
}
