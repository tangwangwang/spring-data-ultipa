package org.springframework.data.ultipa.repository.query;

import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.repository.core.NamedQueries;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.query.QueryLookupStrategy;
import org.springframework.data.repository.query.QueryMethod;
import org.springframework.data.repository.query.QueryMethodEvaluationContextProvider;
import org.springframework.data.repository.query.RepositoryQuery;
import org.springframework.data.ultipa.core.UltipaOperations;
import org.springframework.data.ultipa.core.mapping.UltipaPersistentEntity;
import org.springframework.data.ultipa.core.mapping.UltipaPersistentProperty;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.lang.reflect.Method;

/**
 * Lookup strategy for queries. This is the internal api of the {@code query package}.
 *
 * @author Wangwang Tang
 * @since 1.0
 */
public abstract class UltipaQueryLookupStrategy implements QueryLookupStrategy {

    protected final UltipaOperations operations;
    protected final MappingContext<? extends UltipaPersistentEntity<?>, UltipaPersistentProperty> mappingContext;

    private UltipaQueryLookupStrategy(UltipaOperations operations) {
        this.operations = operations;
        this.mappingContext = operations.getConverter().getMappingContext();
    }

    /* (non-Javadoc)
     * @see org.springframework.data.repository.query.QueryLookupStrategy#resolveQuery(java.lang.reflect.Method, org.springframework.data.repository.core.RepositoryMetadata, org.springframework.data.projection.ProjectionFactory, org.springframework.data.repository.core.NamedQueries)
     */
    @Override
    public final RepositoryQuery resolveQuery(Method method, RepositoryMetadata metadata, ProjectionFactory factory, NamedQueries namedQueries) {
        return resolveQuery(new UltipaQueryMethod(method, metadata, factory), operations, namedQueries);
    }

    protected abstract RepositoryQuery resolveQuery(UltipaQueryMethod method, UltipaOperations operations, NamedQueries namedQueries);

    public static QueryLookupStrategy create(UltipaOperations operations, @Nullable Key key, QueryMethodEvaluationContextProvider evaluationContextProvider) {

        Assert.notNull(operations, "UltipaOperations must not be null!");
        Assert.notNull(evaluationContextProvider, "EvaluationContextProvider must not be null!");

        switch (key != null ? key : Key.CREATE_IF_NOT_FOUND) {
            case CREATE:
                return new CreateQueryLookupStrategy(operations);
            case USE_DECLARED_QUERY:
                return new DeclaredQueryLookupStrategy(operations, evaluationContextProvider);
            case CREATE_IF_NOT_FOUND:
                return new CreateIfNotFoundQueryLookupStrategy(operations,
                        new CreateQueryLookupStrategy(operations),
                        new DeclaredQueryLookupStrategy(operations, evaluationContextProvider));
            default:
                throw new IllegalArgumentException(String.format("Unsupported query lookup strategy %s!", key));
        }
    }

    private enum NoQuery implements RepositoryQuery {
        INSTANCE;

        @Override
        public Object execute(Object[] parameters) {
            throw new IllegalStateException("NoQuery should not be executed!");
        }

        @Override
        public QueryMethod getQueryMethod() {
            throw new IllegalStateException("NoQuery does not have a QueryMethod!");
        }
    }

    private static class CreateIfNotFoundQueryLookupStrategy extends UltipaQueryLookupStrategy {

        private final CreateQueryLookupStrategy createStrategy;
        private final DeclaredQueryLookupStrategy declaredStrategy;

        CreateIfNotFoundQueryLookupStrategy(UltipaOperations operations, CreateQueryLookupStrategy createStrategy, DeclaredQueryLookupStrategy declaredStrategy) {
            super(operations);
            Assert.notNull(createStrategy, "CreateQueryLookupStrategy must not be null!");
            Assert.notNull(declaredStrategy, "DeclaredQueryLookupStrategy must not be null!");
            this.createStrategy = createStrategy;
            this.declaredStrategy = declaredStrategy;
        }

        @Override
        protected RepositoryQuery resolveQuery(UltipaQueryMethod method, UltipaOperations operations, NamedQueries namedQueries) {
            RepositoryQuery lookupQuery = declaredStrategy.resolveQuery(method, operations, namedQueries);

            if (lookupQuery != NoQuery.INSTANCE) {
                return lookupQuery;
            }

            return createStrategy.resolveQuery(method, operations, namedQueries);
        }
    }

    private static class CreateQueryLookupStrategy extends UltipaQueryLookupStrategy {

        CreateQueryLookupStrategy(UltipaOperations operations) {
            super(operations);
        }

        @Override
        protected RepositoryQuery resolveQuery(UltipaQueryMethod method, UltipaOperations operations, NamedQueries namedQueries) {
            // TODO Resolving uql statements based on method names is not yet implemented
            throw new IllegalArgumentException(String.format("Failed to create query for method %s!", method));
        }
    }

    static class DeclaredQueryLookupStrategy extends UltipaQueryLookupStrategy {

        DeclaredQueryLookupStrategy(UltipaOperations operations, QueryMethodEvaluationContextProvider evaluationContextProvider) {
            super(operations);
        }

        @Override
        protected RepositoryQuery resolveQuery(UltipaQueryMethod method, UltipaOperations operations, NamedQueries namedQueries) {
            // TODO resolve declared query

            return NoQuery.INSTANCE;
        }
    }
}
