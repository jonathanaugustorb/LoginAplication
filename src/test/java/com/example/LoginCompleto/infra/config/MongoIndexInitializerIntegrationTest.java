package com.example.LoginCompleto.infra.config;

import com.example.LoginCompleto.auth.entity.PasswordResetToken;
import com.example.LoginCompleto.item.entity.Item;
import com.example.LoginCompleto.support.AbstractIntegrationTest;
import com.example.LoginCompleto.user.entity.User;
import com.example.LoginCompleto.user.entity.enums.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.index.IndexInfo;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MongoIndexInitializerIntegrationTest extends AbstractIntegrationTest {

    @Test
    void createsUniqueIndexesFromAnnotations() {
        assertThat(uniqueIndexedFields(User.class)).contains("username", "email");
        assertThat(uniqueIndexedFields(Item.class)).contains("code");
        assertThat(uniqueIndexedFields(PasswordResetToken.class)).contains("tokenHash");
    }

    @Test
    void createsTtlIndexForResetTokens() {
        assertThat(mongoTemplate.indexOps(PasswordResetToken.class).getIndexInfo())
                .anyMatch(index -> index.getExpireAfter().map(Duration::isZero).orElse(false));
    }

    @Test
    void uniqueIndexBlocksDuplicatesEvenOutsideTheService() {
        createUser("joao", UserRole.ESTOQUE);

        assertThatThrownBy(() -> mongoTemplate.insert(User.builder()
                .username("joao").email("outro@mail.com").role(UserRole.ESTOQUE).build()))
                .isInstanceOf(DuplicateKeyException.class);
    }

    private List<String> uniqueIndexedFields(Class<?> entityType) {
        return mongoTemplate.indexOps(entityType).getIndexInfo().stream()
                .filter(IndexInfo::isUnique)
                .flatMap(index -> index.getIndexFields().stream())
                .map(field -> field.getKey())
                .toList();
    }
}
