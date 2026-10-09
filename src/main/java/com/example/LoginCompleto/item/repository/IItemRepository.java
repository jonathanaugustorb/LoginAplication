package com.example.LoginCompleto.item.repository;

import com.example.LoginCompleto.item.entity.Item;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface IItemRepository extends MongoRepository<Item, String>, IItemSearchRepository {
    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, String id);
}
