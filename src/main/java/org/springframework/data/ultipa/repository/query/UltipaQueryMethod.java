package org.springframework.data.ultipa.repository.query;

import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.query.QueryMethod;

import java.lang.reflect.Method;

/**
 * @author Wangwang Tang
 * @since 1.0
 */
public class UltipaQueryMethod extends QueryMethod {
    public UltipaQueryMethod(Method method, RepositoryMetadata metadata, ProjectionFactory factory) {
        super(method, metadata, factory);
    }
}
