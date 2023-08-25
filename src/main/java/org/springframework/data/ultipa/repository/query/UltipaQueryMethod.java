package org.springframework.data.ultipa.repository.query;

import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.query.QueryMethod;
import org.springframework.data.ultipa.annotation.CountQuery;
import org.springframework.data.ultipa.annotation.ExistsQuery;
import org.springframework.data.ultipa.annotation.Query;
import org.springframework.data.util.Lazy;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;

/**
 * Ultipa specific implementation of {@link QueryMethod}.
 *
 * @author Wangwang Tang
 * @since 1.0
 */
public class UltipaQueryMethod extends QueryMethod {

    private final Lazy<Query> query;
    private final Lazy<CountQuery> countQuery;
    private final Lazy<ExistsQuery> existsQuery;

    /**
     * Creates a new {@link UltipaQueryMethod} from the given {@link Method}.
     *
     * @param method   must not be {@literal null}.
     * @param metadata must not be {@literal null}.
     * @param factory  must not be {@literal null}.
     */
    public UltipaQueryMethod(Method method, RepositoryMetadata metadata, ProjectionFactory factory) {
        super(method, metadata, factory);
        this.query = Lazy.of(() -> AnnotatedElementUtils.findMergedAnnotation(method, Query.class));
        this.countQuery = Lazy.of(() -> AnnotatedElementUtils.findMergedAnnotation(method, CountQuery.class));
        this.existsQuery = Lazy.of(() -> AnnotatedElementUtils.findMergedAnnotation(method, ExistsQuery.class));
    }

    /**
     * Returns the query uql string declared in a {@link Query} annotation with {@link Query#count()} of false and
     * {@link Query#exists()} of also false or {@literal null} if neither the annotation found nor the attribute was specified.
     *
     * @return The query uql string
     */
    @Nullable
    String getAnnotatedQuery() {
        Query annotatedQuery = query.getNullable();
        if (annotatedQuery == null || annotatedQuery.count() || annotatedQuery.exists()) {
            return null;
        }
        return annotatedQuery.value();
    }

    /**
     * Returns the query uql string declared in a {@link Query} annotation with {@link Query#count()} of true or
     * the count query uql string declared in a {@link CountQuery} annotation or {@literal null} if neither
     * the annotation found nor the attribute was specified.
     *
     * @return The count query uql string
     */
    @Nullable
    String getAnnotatedCountQuery() {
        String countQueryString = null;
        if (query.getOptional().isPresent() && query.get().count()) {
            countQueryString = query.get().value();
        }
        if (!StringUtils.hasText(countQueryString)) {
            return countQuery.getOptional().map(CountQuery::value).orElse(null);
        }
        return countQueryString;
    }

    /**
     * Returns the query uql string declared in a {@link Query} annotation with {@link Query#exists()} of true or
     * the exists query uql string declared in a {@link ExistsQuery} annotation or {@literal null} if neither
     * the annotation found nor the attribute was specified.
     *
     * @return The exists query uql string
     */
    @Nullable
    String getAnnotatedExistsQuery() {
        String existsQueryString = null;
        if (query.getOptional().isPresent() && query.get().exists()) {
            existsQueryString = query.get().value();
        }
        if (!StringUtils.hasText(existsQueryString)) {
            return existsQuery.getOptional().map(ExistsQuery::value).orElse(null);
        }
        return existsQueryString;
    }

    /**
     * Returns the sort prefix string declared in a {@link Query} annotation or {@literal null} if neither the annotation found
     * nor the attribute was specified.
     *
     * @return The ort prefix string
     */
    @Nullable
    String getAnnotatedSortPrefix() {
        return query.getOptional().map(Query::sortPrefix).orElse(null);
    }

    /**
     * Returns whether the method has an annotated query.
     *
     * @return Presence annotations return true, otherwise return false
     */
    boolean hasAnnotatedQuery() {
        return query.getOptional().isPresent()
                || countQuery.getOptional().isPresent()
                || existsQuery.getOptional().isPresent();
    }

    /**
     * Returns whether the method has a count query.
     *
     * @return The count query return true, otherwise return false
     */
    boolean isCount() {
        return query.getOptional().map(Query::count).orElse(false)
                || countQuery.getOptional().isPresent();
    }

    /**
     * Returns whether the method has an exists query.
     *
     * @return The exists query return true, otherwise return false
     */
    boolean isExists() {
        return query.getOptional().map(Query::exists).orElse(false)
                || existsQuery.getOptional().isPresent();
    }
}
