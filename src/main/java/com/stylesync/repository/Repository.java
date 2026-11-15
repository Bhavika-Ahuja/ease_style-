package com.stylesync.repository;

import com.stylesync.exception.DatabaseException;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.Optional;

/**
 * Generic Repository interface - demonstrates ABSTRACTION
 * Defines common CRUD operations for all repositories
 * @param <T> The entity type
 */
public interface Repository<T> {
    
    /**
     * Save or update an entity
     */
    T save(T entity) throws DatabaseException;
    
    /**
     * Find entity by ID
     */
    Optional<T> findById(ObjectId id) throws DatabaseException;
    
    /**
     * Find entity by string ID
     */
    Optional<T> findById(String id) throws DatabaseException;
    
    /**
     * Find all entities
     */
    List<T> findAll() throws DatabaseException;
    
    /**
     * Delete entity by ID
     */
    boolean deleteById(ObjectId id) throws DatabaseException;
    
    /**
     * Delete entity by string ID
     */
    boolean deleteById(String id) throws DatabaseException;
    
    /**
     * Count total entities
     */
    long count() throws DatabaseException;
    
    /**
     * Check if entity exists by ID
     */
    boolean existsById(ObjectId id) throws DatabaseException;
}
