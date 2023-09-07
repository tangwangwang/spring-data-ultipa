package com.tangwangwang.spring.data.ultipa.repository.support;

import com.tangwangwang.spring.data.ultipa.core.UltipaOperations;
import com.tangwangwang.spring.data.ultipa.repository.UltipaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.util.StreamUtils;
import org.springframework.util.Assert;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Wangwang Tang
 * @since 1.0
 */
@NoRepositoryBean
public class SimpleUltipaRepository<T, ID> implements UltipaRepository<T, ID> {

    private static final String REMOVE_NODES_UQL = "delete().nodes({ %s })";
    private static final String REMOVE_EDGES_UQL = "delete().edges({ %s })";
    private static final String FIND_NODES_UQL = "find().nodes({ %s }) as nodes return nodes{*}";
    private static final String FIND_EDGES_UQL = "find().edges({ %s }) as edges return edges{*}";
    private static final String COUNT_NODES_UQL = "find().nodes({ %s }) as nodes return count(nodes)";
    private static final String COUNT_EDGES_UQL = "find().edges({ %s }) as edges return count(edges)";

    private final UltipaEntityInformation<T, ID> information;
    private final UltipaOperations operations;

    public SimpleUltipaRepository(UltipaEntityInformation<T, ID> information, UltipaOperations operations) {
        Assert.notNull(information, "MappingUltipaEntityInformation must not be null!");
        Assert.notNull(operations, "UltipaOperations must not be null!");
        this.information = information;
        this.operations = operations;
    }

    @Override
    public <S extends T> S save(S entity) {
        Assert.notNull(entity, "Entity must not be null!");

        if (information.isNew(entity)) {
            return operations.save(entity);
        } else {
            return operations.update(entity);
        }
    }

    @Override
    public <S extends T> List<S> saveAll(Iterable<S> entities) {
        Assert.notNull(entities, "Entities must not be null!");

        List<S> result = new ArrayList<>();

        for (S entity : entities) {
            result.add(save(entity));
        }

        return result;
    }

    @Override
    public Optional<T> findById(ID id) {
        return Optional.ofNullable(operations.getById(id, information.getJavaType()));
    }

    @Override
    public boolean existsById(ID id) {
        return findById(id).isPresent();
    }

    @Override
    public List<T> findAll() {
        return operations.getAll(information.getJavaType());
    }

    @Override
    public List<T> findAllById(Iterable<ID> ids) {
        String schemaFilter = "@" + information.getSchemaName();
        String idFilter = generateIdFilter(schemaFilter, true);

        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("ids", ids);

        if (information.isNode()) {
            return operations.createQuery(String.format(FIND_NODES_UQL, schemaFilter + " && " + idFilter), paramMap)
                    .findAll(information.getJavaType());
        } else {
            return operations.createQuery(String.format(FIND_EDGES_UQL, schemaFilter + " && " + idFilter), paramMap)
                    .findAll(information.getJavaType());
        }
    }

    @Override
    public long count() {
        String schemaFilter = "@" + information.getSchemaName();

        if (information.isNode()) {
            return operations.createQuery(String.format(COUNT_NODES_UQL, schemaFilter)).count();
        } else {
            return operations.createQuery(String.format(COUNT_EDGES_UQL, schemaFilter)).count();
        }
    }

    @Override
    public void deleteById(ID id) {
        String schemaFilter = "@" + information.getSchemaName();
        String idFilter = generateIdFilter(schemaFilter, false);

        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("id", id);

        if (information.isNode()) {
            operations.createQuery(String.format(REMOVE_NODES_UQL, schemaFilter + " && " + idFilter), paramMap).execute();
        } else {
            operations.createQuery(String.format(REMOVE_EDGES_UQL, schemaFilter + " && " + idFilter), paramMap).execute();
        }
    }

    @Override
    public void delete(T entity) {
        Assert.notNull(entity, "Entity must not be null!");
        ID id = information.getId(entity);
        if (id != null) {
            deleteById(id);
        }
    }

    @Override
    public void deleteAllById(Iterable<? extends ID> ids) {
        String schemaFilter = "@" + information.getSchemaName();
        String idFilter = generateIdFilter(schemaFilter, true);

        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("ids", ids);

        if (information.isNode()) {
            operations.createQuery(String.format(REMOVE_NODES_UQL, schemaFilter + " && " + idFilter), paramMap).execute();
        } else {
            operations.createQuery(String.format(REMOVE_EDGES_UQL, schemaFilter + " && " + idFilter), paramMap).execute();
        }
    }

    @Override
    public void deleteAll(Iterable<? extends T> entities) {
        List<ID> ids = StreamUtils.createStreamFromIterator(entities.iterator())
                .map(information::getId)
                .collect(Collectors.toList());
        deleteAllById(ids);
    }

    @Override
    public void deleteAll() {
        String schemaFilter = "@" + information.getSchemaName();
        if (information.isNode()) {
            operations.createQuery(String.format(REMOVE_NODES_UQL, schemaFilter)).execute();
        } else {
            operations.createQuery(String.format(REMOVE_EDGES_UQL, schemaFilter)).execute();
        }
    }

    @Override
    public List<T> findAll(Sort sort) {
        String schemaFilter = "@" + information.getSchemaName();
        if (information.isNode()) {
            return operations.createQuery(String.format(FIND_NODES_UQL, schemaFilter), sort, "nodes")
                    .findAll(information.getJavaType());
        } else {
            return operations.createQuery(String.format(FIND_EDGES_UQL, schemaFilter), sort, "edges")
                    .findAll(information.getJavaType());
        }
    }

    @Override
    public Page<T> findAll(Pageable pageable) {
        String schemaFilter = "@" + information.getSchemaName();
        List<T> result;
        if (information.isNode()) {
            result = operations.createQuery(String.format(FIND_NODES_UQL, schemaFilter), pageable, "nodes")
                    .findAll(information.getJavaType());
        } else {
            result = operations.createQuery(String.format(FIND_EDGES_UQL, schemaFilter), pageable, "edges")
                    .findAll(information.getJavaType());
        }
        return new PageImpl<>(result, pageable, count());
    }

    private String generateIdFilter(String schemaFilter, boolean batch) {
        String symbols = batch ? " in #{ids} " : " == #{id} ";
        String idFilter;
        if (information.isSystemId()) {
            idFilter = information.getIdPropertyName() + symbols;
        } else {
            idFilter = schemaFilter + "." + information.getIdPropertyName() + symbols;
        }
        return idFilter;
    }
}
