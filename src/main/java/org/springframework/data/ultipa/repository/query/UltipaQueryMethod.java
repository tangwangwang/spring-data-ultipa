package org.springframework.data.ultipa.repository.query;

import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.query.QueryMethod;
import org.springframework.data.ultipa.annotation.Query;
import org.springframework.data.util.Lazy;

import java.lang.reflect.Method;

/**
 * @author Wangwang Tang
 * @since 1.0
 */
public class UltipaQueryMethod extends QueryMethod {

    private final Lazy<Query> query;

    public UltipaQueryMethod(Method method, RepositoryMetadata metadata, ProjectionFactory factory) {
        super(method, metadata, factory);
        this.query = Lazy.of(() -> AnnotatedElementUtils.findMergedAnnotation(method, Query.class));
    }

    String getQuery() {
        return query.map(Query::value).or(String.valueOf(getAttributeValue("value"))).get();
    }

    String getCountQuery() {
        return query.map(Query::countQuery).or(String.valueOf(getAttributeValue("countQuery"))).get();
    }

    String getSortPrefix() {
        return query.map(Query::sortPrefix).or(String.valueOf(getAttributeValue("sortPrefix"))).get();
    }

    boolean isCount() {
        return query.map(Query::count).or(false).get();
    }

    boolean isExists() {
        return query.map(Query::exists).or(false).get();
    }

    private Object getAttributeValue(String attributeName) {
        Object value = AnnotationUtils.getDefaultValue(Query.class, attributeName);
        if (value == null) {
            throw new IllegalArgumentException(String.format("%s has no attribute of %s!", Query.class, attributeName));
        }
        return value;
    }
}
