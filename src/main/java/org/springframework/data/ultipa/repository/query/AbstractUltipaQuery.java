package org.springframework.data.ultipa.repository.query;

import org.springframework.data.repository.query.ParameterAccessor;
import org.springframework.data.repository.query.RepositoryQuery;
import org.springframework.data.repository.query.ResultProcessor;
import org.springframework.data.ultipa.core.UltipaOperations;
import org.springframework.data.ultipa.core.query.Query;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * Base class for {@link RepositoryQuery} implementations for Ultipa.
 *
 * @author Wangwang Tang
 * @since 1.0
 */
public abstract class AbstractUltipaQuery implements RepositoryQuery {

    private final UltipaQueryMethod method;
    private final UltipaOperations operations;

    /**
     * Creates a new {@link AbstractUltipaQuery} from the given {@link UltipaQueryMethod} and {@link UltipaOperations}.
     *
     * @param method     must not be {@literal null}.
     * @param operations must not be {@literal null}.
     */
    public AbstractUltipaQuery(UltipaQueryMethod method, UltipaOperations operations) {

        Assert.notNull(method, "UltipaQueryMethod must not be null!");
        Assert.notNull(operations, "UltipaOperations must not be null!");

        this.method = method;
        this.operations = operations;
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.repository.query.RepositoryQuery#execute(java.lang.Object[])
     */
    @Override
    public Object execute(Object[] parameters) {
        UltipaParametersParameterAccessor accessor = new UltipaParametersParameterAccessor(method.getParameters(), parameters);

        ResultProcessor processor = method.getResultProcessor().withDynamicProjection(accessor);
        return processor.processResult(doExecute(getExecution(), accessor));
    }

    @Nullable
    private Object doExecute(UltipaQueryExecution execution, UltipaParametersParameterAccessor accessor) {
        return execution.execute(this, accessor);
    }

    protected UltipaQueryExecution getExecution() {
        if (method.isCollectionQuery()) {
            return UltipaQueryExecution.collectionExecution();
        } else if (method.isSliceQuery()) {
            return UltipaQueryExecution.slicedExecution();
        } else if (method.isPageQuery()) {
            return UltipaQueryExecution.pagedExecution();
        } else if (isCountQuery()) {
            return UltipaQueryExecution.countExecution();
        } else if (isExistsQuery()) {
            return UltipaQueryExecution.existsExecution();
        } else {
            return UltipaQueryExecution.singleEntityExecution();
        }
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.repository.query.RepositoryQuery#getQueryMethod()
     */
    @Override
    public UltipaQueryMethod getQueryMethod() {
        return method;
    }

    /**
     * Returns the {@link UltipaOperations}.
     *
     * @return never {@literal null}.
     */
    public UltipaOperations getOperations() {
        return operations;
    }

    /**
     * Returns whether the query should get a count projection applied.
     *
     * @return The count projection applied return true, otherwise return false
     */
    protected abstract boolean isCountQuery();

    /**
     * Returns whether the query should get an exists projection applied.
     *
     * @return The exists projection applied return true, otherwise return false
     */
    protected abstract boolean isExistsQuery();

    /**
     * Creates a {@link Query} instance using the given {@link ParameterAccessor}
     *
     * @param accessor must not be {@literal null}.
     * @return Created {@link Query}
     */
    protected abstract Query createQuery(UltipaParametersParameterAccessor accessor);

    /**
     * Creates a {@link Query} instance using the given {@link ParameterAccessor} for count projection applied.
     *
     * @param accessor must not be {@literal null}.
     * @return Created {@link Query}
     */
    protected abstract Query createCountQuery(UltipaParametersParameterAccessor accessor);

    /**
     * Creates a {@link Query} instance using the given {@link ParameterAccessor} for exists projection applied.
     *
     * @param accessor must not be {@literal null}.
     * @return Created {@link Query}
     */
    protected abstract Query createExistsQuery(UltipaParametersParameterAccessor accessor);

}