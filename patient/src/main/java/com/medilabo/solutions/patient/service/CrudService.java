package com.medilabo.solutions.patient.service;

import java.util.List;

/**
 * Generic service interface for managing entities.
 *
 * @param <T> the type of the entity
 * 
 * @author Fraigneau Lucas
 * @version 1.0
 */
public interface CrudService<T> {

    /**
     * Creates a new entity.
     *
     * @param Object the entity to create
     * @return the created entity
     * @throws Exception if the create operation is not supported
     */
    public default T create(T Object) throws Exception {
        throw new UnsupportedOperationException("Create operation not supported");
    }

    /**
     * Retrieves all entities.
     *
     * @return a list of all entities
     * @throws Exception if the create operation is not supported
     */
    public default List<T> findAll() {
        throw new UnsupportedOperationException("Find all operation not supported");
    }

    /**
     * Retrieves an entity by its identifier.
     *
     * @param id the identifier of the entity to retrieve
     * @return the entity with the specified identifier
     * @throws Exception if the create operation is not supported
     */
    public default T findById(int id) {
        throw new UnsupportedOperationException("Find by ID operation not supported");
    }

    /**
     * Creates a new entity.
     *
     * @param Object the entity to create
     * @return the created entity
     * @throws Exception if the create operation is not supported
     */
    public default T update(T Object) {
        throw new UnsupportedOperationException("Update operation not supported");
    }

    /**
     * Deletes an entity.
     *
     * @param Object the entity to delete
     * @throws Exception if the create operation is not supported
     */
    public default void delete(T Object) {
        throw new UnsupportedOperationException("Delete operation not supported");
    }

}
