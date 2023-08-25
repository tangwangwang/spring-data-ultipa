package org.springframework.data.ultipa.repository.query;

import org.springframework.data.ultipa.core.UltipaOperations;
import org.springframework.data.ultipa.core.query.Query;
import org.springframework.util.StringUtils;

/**
 * Query to use a plain uql String to create the {@link Query} to actually execute.
 *
 * @author Wangwang Tang
 * @since 1.0
 */
public class StringBasedUltipaQuery extends AbstractUltipaQuery {

    /**
     * Creates a new {@link StringBasedUltipaQuery} for the given {@link UltipaQueryMethod}, {@link UltipaOperations}.
     *
     * @param method     must not be {@literal null}.
     * @param operations must not be {@literal null}.
     */
    public StringBasedUltipaQuery(UltipaQueryMethod method, UltipaOperations operations) {
        super(method, operations);
    }

    @Override
    protected boolean isCountQuery() {
        return getQueryMethod().isCount();
    }

    @Override
    protected boolean isExistsQuery() {
        return getQueryMethod().isExists();
    }

    @Override
    protected Query createQuery(UltipaParametersParameterAccessor accessor) {
        String query = getQueryMethod().getAnnotatedQuery();
        if (!StringUtils.hasText(query)) {
            throw new IllegalArgumentException(String.format("Not found uql string for query on method for '%s'.", getQueryMethod()));
        }

        if (accessor.getPageable().isPaged()) {
            return getOperations().createQuery(query, accessor.getParamMap(), accessor.getPageable(), getQueryMethod().getAnnotatedSortPrefix());
        }
        if (accessor.getSort().isSorted()) {
            return getOperations().createQuery(query, accessor.getParamMap(), accessor.getSort(), getQueryMethod().getAnnotatedSortPrefix());
        }
        return getOperations().createQuery(query, accessor.getParamMap());
    }

    @Override
    protected Query createCountQuery(UltipaParametersParameterAccessor accessor) {
        String query = getQueryMethod().getAnnotatedCountQuery();
        if (!StringUtils.hasText(query)) {
            throw new IllegalArgumentException(String.format("Not found uql string for count query on method for '%s'.", getQueryMethod()));
        }
        return getOperations().createQuery(query, accessor.getParamMap());
    }

    @Override
    protected Query createExistsQuery(UltipaParametersParameterAccessor accessor) {
        String query = getQueryMethod().getAnnotatedExistsQuery();
        if (!StringUtils.hasText(query)) {
            throw new IllegalArgumentException(String.format("Not found uql string for exists query on method for '%s'.", getQueryMethod()));
        }
        return getOperations().createQuery(query, accessor.getParamMap());
    }
}