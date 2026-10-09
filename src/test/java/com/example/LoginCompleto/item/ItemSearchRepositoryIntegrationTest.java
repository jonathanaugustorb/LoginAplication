package com.example.LoginCompleto.item;

import com.example.LoginCompleto.item.dto.ItemFilterDTO;
import com.example.LoginCompleto.item.entity.Item;
import com.example.LoginCompleto.item.entity.enums.ItemCategory;
import com.example.LoginCompleto.item.repository.IItemRepository;
import com.example.LoginCompleto.support.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

class ItemSearchRepositoryIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private IItemRepository itemRepository;

    @BeforeEach
    void setUp() {
        for (int i = 1; i <= 12; i++) {
            itemRepository.save(item("VEL-%02d".formatted(i), "Vela de ignição " + i, ItemCategory.MOTOR, i, 5));
        }
        itemRepository.save(item("SUS-01", "Amortecedor (dianteiro)", ItemCategory.SUSPENSAO, 10, 2));
    }

    @Test
    void searchesTextIgnoringCaseAcrossFields() {
        assertThat(search(filter -> filter.setSearch("vel-0")).getTotalElements()).isEqualTo(9);
        assertThat(search(filter -> filter.setSearch("IGNIÇÃO")).getTotalElements()).isEqualTo(12);
        assertThat(search(filter -> filter.setSearch("bosch")).getTotalElements()).isEqualTo(13);
    }

    @Test
    void treatsSearchTextLiterallyNotAsRegex() {
        assertThat(search(filter -> filter.setSearch("(dianteiro")).getContent())
                .extracting(Item::getCode).containsExactly("SUS-01");
        assertThat(search(filter -> filter.setSearch(".*")).getTotalElements()).isZero();
    }

    @Test
    void combinesCategoryAndLowStockFilters() {
        assertThat(search(filter -> filter.setCategory(ItemCategory.SUSPENSAO)).getTotalElements()).isEqualTo(1);
        assertThat(search(filter -> filter.setLowStock(true)).getTotalElements()).isEqualTo(5);
        assertThat(search(filter -> {
            filter.setCategory(ItemCategory.SUSPENSAO);
            filter.setLowStock(true);
        }).getTotalElements()).isZero();
        assertThat(itemRepository.countLowStock()).isEqualTo(5);
    }

    @Test
    void paginatesAndSorts() {
        var firstPage = itemRepository.search(new ItemFilterDTO(), PageRequest.of(0, 10, Sort.by("quantity")));
        var secondPage = itemRepository.search(new ItemFilterDTO(), PageRequest.of(1, 10, Sort.by("quantity")));

        assertThat(firstPage.getTotalElements()).isEqualTo(13);
        assertThat(firstPage.getTotalPages()).isEqualTo(2);
        assertThat(firstPage.getContent()).hasSize(10).first().extracting(Item::getQuantity).isEqualTo(1);
        assertThat(secondPage.getContent()).hasSize(3);
    }

    @Test
    void addQuantityIsAtomicAndBounded() {
        Item item = itemRepository.findAll().get(0);
        int quantity = item.getQuantity();

        assertThat(itemRepository.addQuantity(item.getId(), 4, 100, "tester")).isTrue();
        assertThat(itemRepository.addQuantity(item.getId(), 1_000, 100, "tester")).isFalse();

        Item updated = itemRepository.findById(item.getId()).orElseThrow();
        assertThat(updated.getQuantity()).isEqualTo(quantity + 4);
        assertThat(updated.getUpdatedBy()).isEqualTo("tester");
    }

    private Page<Item> search(Consumer<ItemFilterDTO> configure) {
        var filter = new ItemFilterDTO();
        configure.accept(filter);
        return itemRepository.search(filter, PageRequest.of(0, 50));
    }

    private Item item(String code, String name, ItemCategory category, int quantity, int minimumQuantity) {
        return Item.builder()
                .code(code).name(name).category(category).brand("Bosch")
                .quantity(quantity).minimumQuantity(minimumQuantity)
                .price(new BigDecimal("10.00"))
                .build();
    }
}
