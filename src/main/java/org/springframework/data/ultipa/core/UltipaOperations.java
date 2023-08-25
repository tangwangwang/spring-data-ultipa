package org.springframework.data.ultipa.core;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.ultipa.core.convert.UltipaConverter;
import org.springframework.data.ultipa.core.query.Query;
import org.springframework.lang.Nullable;

import java.util.List;
import java.util.Map;

/**
 * Interface that specifies a basic set of Ultipa operations. Implemented by {@link UltipaTemplate}. Not often used but
 * a useful option for extensibility and testability (as it can be easily mocked, stubbed, or be the target of a JDK
 * proxy).
 *
 * @author Wangwang Tang
 * @since 1.0
 */
public interface UltipaOperations {

    Query createQuery(String uql);

    Query createQuery(String uql, Map<String, Object> paramMap);

    Query createQuery(String uql, Sort sort);

    Query createQuery(String uql, Pageable pageable);

    Query createQuery(String uql, Sort sort, @Nullable String sortPrefix);

    Query createQuery(String uql, Pageable pageable, @Nullable String sortPrefix);

    Query createQuery(String uql, Map<String, Object> paramMap, Sort sort);

    Query createQuery(String uql, Map<String, Object> paramMap, Pageable pageable);

    Query createQuery(String uql, Map<String, Object> paramMap, Sort sort, @Nullable String sortPrefix);

    Query createQuery(String uql, Map<String, Object> paramMap, Pageable pageable, @Nullable String sortPrefix);

    <T> T save(T entity);

    <T> T update(T entity);

    <T> void remove(T entity);

    @Nullable
    <T> T getById(Object id, Class<T> entityClass);

    <T> boolean existsById(Object id, Class<T> entityClass);

    <T> List<T> getAll(Class<T> entityClass);

    void execute(String uql);

    @Nullable
    <T> T findOne(String uql, Class<T> entityClass);

    <T> List<T> findAll(String uql, Class<T> entityClass);

    @Nullable
    Map<String, Object> findOne(String uql);

    List<Map<String, Object>> findAll(String uql);

    @Nullable
    List<Object> findArray(String uql);

    List<List<Object>> findArrays(String uql);

    /**
     * Returns the underlying {@link UltipaConverter}.
     *
     * @return never {@literal null}.
     */
    UltipaConverter getConverter();
}
