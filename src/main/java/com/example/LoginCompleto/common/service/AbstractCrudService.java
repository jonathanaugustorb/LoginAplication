package com.example.LoginCompleto.common.service;

import com.example.LoginCompleto.common.entity.BaseEntity;
import com.example.LoginCompleto.common.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public abstract class AbstractCrudService<T extends BaseEntity, R extends MongoRepository<T, String>>
        implements ICrudService<T> {

    protected final R repository;
    private final String resourceName;

    protected AbstractCrudService(R repository, String resourceName) {
        this.repository = repository;
        this.resourceName = resourceName;
    }

    @Override
    public Page<T> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    @Override
    public T findById(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(resourceName, id));
    }

    @Override
    public T save(T entity) {
        return repository.save(entity);
    }

    @Override
    public void deleteById(String id) {
        repository.delete(findById(id));
    }

    @Override
    public long count() {
        return repository.count();
    }
}
