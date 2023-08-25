package org.springframework.data.ultipa.repository.query;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.data.ultipa.core.query.Query;
import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;

import java.util.List;

/**
 * @author Wangwang Tang
 * @since 1.0
 */
public interface UltipaQueryExecution {

    @Nullable
    Object execute(AbstractUltipaQuery ultipaQuery, UltipaParametersParameterAccessor accessor);

    static UltipaQueryExecution countExecution() {
        return (ultipaQuery, accessor) -> {
            Query query = ultipaQuery.createCountQuery(accessor);
            return query.count();
        };
    }

    static UltipaQueryExecution existsExecution() {
        return (ultipaQuery, accessor) -> {
            Query query = ultipaQuery.createExistsQuery(accessor);
            return query.exists();
        };
    }

    static UltipaQueryExecution collectionExecution() {
        return (ultipaQuery, accessor) -> {
            Query query = ultipaQuery.createQuery(accessor);
            Class<?> returnedType = ClassUtils.resolvePrimitiveIfNecessary(ultipaQuery.getQueryMethod().getResultProcessor().getReturnedType().getReturnedType());
            return query.findAll(returnedType);
        };
    }

    static UltipaQueryExecution slicedExecution() {
        return (ultipaQuery, accessor) -> {
            Class<?> domainType = ultipaQuery.getQueryMethod().getResultProcessor().getReturnedType().getDomainType();
            Pageable pageable = accessor.getPageable();
            int pageSize = 0;
            if (pageable.isPaged()) {
                pageSize = pageable.getPageSize();
            }
            Query query = ultipaQuery.createQuery(accessor).limit(pageSize + 1);
            List<?> resultList = query.findAll(domainType);

            boolean hasNext = pageable.isPaged() && resultList.size() > pageable.getPageSize();
            if (hasNext) {
                return new SliceImpl<>(resultList.subList(0, pageSize), pageable, true);
            } else {
                return new SliceImpl<>(resultList, pageable, false);
            }
        };
    }

    static UltipaQueryExecution pagedExecution() {
        return (ultipaQuery, accessor) -> {
            Class<?> domainType = ultipaQuery.getQueryMethod().getResultProcessor().getReturnedType().getDomainType();
            Query query = ultipaQuery.createQuery(accessor);
            Query countQuery = ultipaQuery.createCountQuery(accessor);
            return PageableExecutionUtils.getPage(query.findAll(domainType), accessor.getPageable(), countQuery::count);
        };
    }


    static UltipaQueryExecution singleEntityExecution() {
        return (ultipaQuery, accessor) -> {
            Class<?> returnedType = ClassUtils.resolvePrimitiveIfNecessary(ultipaQuery.getQueryMethod().getResultProcessor().getReturnedType().getReturnedType());
            Class<?> domainType = ultipaQuery.getQueryMethod().getResultProcessor().getReturnedType().getDomainType();
            if (domainType == returnedType) {
                return ultipaQuery.createQuery(accessor).findOne(domainType);
            }

            if (returnedType == Boolean.class) {
                return ultipaQuery.createQuery(accessor).exists();
            }

            if (Number.class.isAssignableFrom(returnedType)) {
                return ultipaQuery.createQuery(accessor).count();
            }

            return ultipaQuery.createQuery(accessor).findOne(returnedType);
        };
    }

}
