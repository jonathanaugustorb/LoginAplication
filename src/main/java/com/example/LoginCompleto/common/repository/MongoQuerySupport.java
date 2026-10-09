package com.example.LoginCompleto.common.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.support.PageableExecutionUtils;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

public final class MongoQuerySupport {

    private MongoQuerySupport() {
    }

    public static Optional<Criteria> containsIgnoreCase(String term, Collection<String> fields) {
        if (term == null || term.isBlank()) return Optional.empty();

        Pattern pattern = Pattern.compile(Pattern.quote(term.trim()), Pattern.CASE_INSENSITIVE);
        return Optional.of(new Criteria().orOperator(fields.stream()
                .map(field -> Criteria.where(field).regex(pattern))
                .toList()));
    }

    public static <T> Page<T> findPage(MongoTemplate mongoTemplate, List<Criteria> criteria,
                                       Pageable pageable, Class<T> entityType) {
        Query query = new Query();
        if (!criteria.isEmpty()) query.addCriteria(new Criteria().andOperator(criteria));

        List<T> content = mongoTemplate.find(Query.of(query).with(pageable), entityType);
        return PageableExecutionUtils.getPage(content, pageable, () -> mongoTemplate.count(query, entityType));
    }
}
