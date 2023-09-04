package com.tangwangwang.spring.data.ultipa.repository;

import org.springframework.data.domain.Sort;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.Repository;

import java.util.List;

/**
 * Ultipa specific {@link Repository} interface.
 *
 * @param <T>  type of the domain class to map
 * @param <ID> identifier type in the domain class
 * @author Wangwang Tang
 * @since 1.0
 */
@NoRepositoryBean
public interface UltipaRepository<T, ID> extends CrudRepository<T, ID>, PagingAndSortingRepository<T, ID> {

    /*
     * (non-Javadoc)
     * @see org.springframework.data.repository.CrudRepository#saveAll(java.lang.Iterable)
     */
    @Override
    <S extends T> List<S> saveAll(Iterable<S> entities);

    /*
     * (non-Javadoc)
     * @see org.springframework.data.repository.CrudRepository#findAll()
     */
    @Override
    List<T> findAll();

    /*
     * (non-Javadoc)
     * @see org.springframework.data.repository.CrudRepository#findAllById(java.lang.Iterable)
     */
    @Override
    List<T> findAllById(Iterable<ID> iterable);

    /*
     * (non-Javadoc)
     * @see org.springframework.data.repository.PagingAndSortingRepository#findAll(org.springframework.data.domain.Sort)
     */
    @Override
    List<T> findAll(Sort sort);

}
