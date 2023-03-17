package org.springframework.data.ultipa.core.schema;

/**
 * Interface for generating ids for entities.
 *
 * @author Wangwang Tang
 * @since 1.0
 */
@FunctionalInterface
public interface IdGenerator<ID> {

    /**
     * Generates a new id for given entity.
     *
     * @param primary the id property name
     * @param entity  the entity to be saved
     * @return id to be assigned to the entity
     */
    ID generateId(String primary, Object entity);

}
