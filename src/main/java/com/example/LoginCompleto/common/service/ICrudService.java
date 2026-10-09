package com.example.LoginCompleto.common.service;

import com.example.LoginCompleto.common.entity.BaseEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ICrudService<T extends BaseEntity> {

    Page<T> findAll(Pageable pageable);

    T findById(String id);

    T save(T entity);

    void deleteById(String id);

    long count();
}
