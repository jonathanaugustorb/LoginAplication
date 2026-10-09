package com.example.LoginCompleto.infra.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.IndexOperations;
import org.springframework.data.mongodb.core.index.MongoPersistentEntityIndexResolver;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.data.mongodb.core.mapping.MongoPersistentEntity;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MongoIndexInitializer {

    private final MongoTemplate mongoTemplate;
    private final MongoMappingContext mappingContext;

    @EventListener(ApplicationReadyEvent.class)
    public void createIndexes() {
        var indexResolver = new MongoPersistentEntityIndexResolver(mappingContext);

        mappingContext.getPersistentEntities().stream()
                .filter(entity -> entity.isAnnotationPresent(Document.class))
                .forEach(entity -> createIndexesFor(entity, indexResolver));
    }

    private void createIndexesFor(MongoPersistentEntity<?> entity, MongoPersistentEntityIndexResolver indexResolver) {
        IndexOperations indexOperations = mongoTemplate.indexOps(entity.getType());
        indexResolver.resolveIndexForEntity(entity).forEach(indexOperations::createIndex);
        log.info("Índices verificados para a coleção '{}'", entity.getCollection());
    }
}
