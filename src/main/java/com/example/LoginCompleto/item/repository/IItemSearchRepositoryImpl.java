package com.example.LoginCompleto.item.repository;

import com.example.LoginCompleto.common.repository.MongoQuerySupport;
import com.example.LoginCompleto.item.dto.ItemFilterDTO;
import com.example.LoginCompleto.item.entity.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.ComparisonOperators;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class IItemSearchRepositoryImpl implements IItemSearchRepository {

    private static final List<String> SEARCHABLE_FIELDS = List.of("name", "code", "brand", "application");

    private final MongoTemplate mongoTemplate;

    @Override
    public Page<Item> search(ItemFilterDTO filter, Pageable pageable) {
        List<Criteria> criteria = new ArrayList<>();

        MongoQuerySupport.containsIgnoreCase(filter.getSearch(), SEARCHABLE_FIELDS).ifPresent(criteria::add);
        if (filter.getCategory() != null) criteria.add(Criteria.where("category").is(filter.getCategory()));
        if (filter.isLowStock()) criteria.add(lowStockCriteria());

        return MongoQuerySupport.findPage(mongoTemplate, criteria, pageable, Item.class);
    }

    @Override
    public long countLowStock() {
        return mongoTemplate.count(new Query(lowStockCriteria()), Item.class);
    }

    @Override
    public boolean addQuantity(String id, int amount, int maxQuantity, String updatedBy) {
        Query query = new Query(Criteria.where("id").is(id).and("quantity").lte(maxQuantity - amount));
        Update update = new Update()
                .inc("quantity", amount)
                .inc("version", 1)
                .set("updatedAt", Instant.now())
                .set("updatedBy", updatedBy);

        return mongoTemplate.updateFirst(query, update, Item.class).getModifiedCount() > 0;
    }

    private Criteria lowStockCriteria() {
        return Criteria.expr(ComparisonOperators.valueOf("quantity").lessThanEqualTo("minimumQuantity"));
    }
}
