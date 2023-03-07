package org.springframework.data.ultipa.repository;

import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.Repository;

/**
 * Ultipa specific {@link Repository} interface.
 *
 * @param <T>  type of the domain class to map
 * @param <ID> identifier type in the domain class
 * @author Wangwang Tang
 * @since 1.0
 */
@NoRepositoryBean
public interface UltipaRepository<T, ID> extends PagingAndSortingRepository<T, ID> {
}
