package org.springframework.data.ultipa.repository.query;

import org.springframework.data.repository.query.QueryMethodEvaluationContextProvider;
import org.springframework.data.repository.query.RepositoryQuery;
import org.springframework.data.repository.query.ResultProcessor;
import org.springframework.data.ultipa.core.UltipaOperations;
import org.springframework.data.ultipa.core.query.Query;
import org.springframework.data.util.Lazy;
import org.springframework.util.Assert;

import java.util.Map;

/**
 * @author Wangwang Tang
 * @since 1.0
 */
public class SimpleUltipaQuery implements RepositoryQuery {

    private final UltipaQueryMethod method;
    private final UltipaOperations operations;
    private final String queryString;
    private final String countQueryString;
    private final Lazy<UltipaQueryExecution> execution;

    public SimpleUltipaQuery(UltipaQueryMethod method, UltipaOperations operations,
                             QueryMethodEvaluationContextProvider evaluationContextProvider) {

        Assert.notNull(method, "UltipaQueryMethod must not be null!");
        Assert.notNull(operations, "UltipaOperations must not be null!");
        Assert.notNull(evaluationContextProvider, "ExpressionEvaluationContextProvider must not be null!");

        this.method = method;
        this.operations = operations;
        this.queryString = method.getQuery();
        this.countQueryString = method.getCountQuery();

        this.execution = Lazy.of(() -> {
            if (method.isCollectionQuery()) {
                return UltipaQueryExecution.collectionExecution();
            } else if (method.isSliceQuery()) {
                return UltipaQueryExecution.slicedExecution();
            } else if (method.isPageQuery()) {
                return UltipaQueryExecution.pagedExecution();
            } else if (method.isCount()) {
                return UltipaQueryExecution.countExecution();
            } else if (method.isExists()) {
                return UltipaQueryExecution.existsExecution();
            } else {
                return UltipaQueryExecution.singleEntityExecution();
            }
        });
    }

    @Override
    public Object execute(Object[] parameters) {
        UltipaQueryExecution execution = this.execution.get();

        UltipaParametersParameterAccessor accessor = new UltipaParametersParameterAccessor(method.getParameters(), parameters);
        Object result = execution.execute(this, accessor);

        ResultProcessor withDynamicProjection = method.getResultProcessor().withDynamicProjection(accessor);
        return withDynamicProjection.processResult(result);
    }

    @Override
    public UltipaQueryMethod getQueryMethod() {
        return method;
    }

    protected Query createQuery(UltipaParametersParameterAccessor accessor) {
        Map<String, Object> paramMap = accessor.getParamMap();
        if (accessor.getPageable().isPaged()) {
            return operations.createQuery(queryString, paramMap, accessor.getPageable(), method.getSortPrefix());
        }
        if (accessor.getSort().isSorted()) {
            return operations.createQuery(queryString, paramMap, accessor.getSort(), method.getSortPrefix());
        }
        return operations.createQuery(queryString, paramMap);
    }

    protected Query createCountQuery(UltipaParametersParameterAccessor accessor) {
        return operations.createQuery(countQueryString, accessor.getParamMap());
    }

}