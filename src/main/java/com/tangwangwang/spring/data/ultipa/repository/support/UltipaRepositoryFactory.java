package com.tangwangwang.spring.data.ultipa.repository.support;

import com.tangwangwang.spring.data.ultipa.core.UltipaOperations;
import com.tangwangwang.spring.data.ultipa.core.mapping.UltipaPersistentEntity;
import com.tangwangwang.spring.data.ultipa.core.mapping.UltipaPersistentProperty;
import com.tangwangwang.spring.data.ultipa.repository.UltipaRepository;
import com.tangwangwang.spring.data.ultipa.repository.query.UltipaQueryLookupStrategy;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.repository.core.EntityInformation;
import org.springframework.data.repository.core.RepositoryInformation;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;
import org.springframework.data.repository.query.QueryLookupStrategy;
import org.springframework.data.repository.query.QueryLookupStrategy.Key;
import org.springframework.data.repository.query.QueryMethodEvaluationContextProvider;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.util.Optional;

/**
 * Factory to create {@link UltipaRepository} instances.
 *
 * @author Wangwang Tang
 * @since 1.0
 */
public class UltipaRepositoryFactory extends RepositoryFactorySupport {

    private final UltipaOperations operations;

    private final MappingContext<? extends UltipaPersistentEntity<?>, UltipaPersistentProperty> mappingContext;

    /**
     * Creates a new {@link UltipaRepositoryFactory} with the given {@link UltipaOperations}.
     *
     * @param ultipaOperations must not be {@literal null}.
     */
    public UltipaRepositoryFactory(UltipaOperations ultipaOperations) {
        Assert.notNull(ultipaOperations, "UltipaOperations must not be null!");
        this.operations = ultipaOperations;
        this.mappingContext = operations.getConverter().getMappingContext();
    }

    @Override
    public <T, ID> EntityInformation<T, ID> getEntityInformation(Class<T> domainClass) {
        UltipaPersistentEntity<?> persistentEntity = mappingContext.getRequiredPersistentEntity(domainClass);

        return UltipaEntityInformationSupport.getEntityInformation(persistentEntity);
    }

    @Override
    protected final UltipaRepository<?, ?> getTargetRepository(RepositoryInformation information) {
        return getTargetRepository(information, operations);
    }

    protected UltipaRepository<?, ?> getTargetRepository(RepositoryInformation information, UltipaOperations operations) {
        EntityInformation<?, Object> entityInformation = getEntityInformation(information.getDomainType());
        UltipaRepository<?, ?> repository = getTargetRepositoryViaReflection(information, entityInformation, operations);

        Assert.isInstanceOf(UltipaRepository.class, repository);

        return repository;
    }

    @Override
    protected Class<?> getRepositoryBaseClass(RepositoryMetadata metadata) {
        return SimpleUltipaRepository.class;
    }

    @Override
    protected Optional<QueryLookupStrategy> getQueryLookupStrategy(@Nullable Key key, QueryMethodEvaluationContextProvider evaluationContextProvider) {
        return Optional.of(UltipaQueryLookupStrategy.create(operations, key, evaluationContextProvider));
    }
}
