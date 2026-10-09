package com.example.LoginCompleto.item;

import com.example.LoginCompleto.common.web.FlashMessage;
import com.example.LoginCompleto.item.entity.Item;
import com.example.LoginCompleto.item.entity.enums.ItemCategory;
import com.example.LoginCompleto.item.repository.IItemRepository;
import com.example.LoginCompleto.support.AbstractIntegrationTest;
import com.example.LoginCompleto.user.entity.User;
import com.example.LoginCompleto.user.entity.enums.UserRole;
import org.bson.Document;
import org.bson.types.Decimal128;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ItemIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private IItemRepository itemRepository;

    private User estoque;
    private User supervisor;
    private User gerente;
    private Item item;

    @BeforeEach
    void setUp() {
        estoque = createUser("estoque", UserRole.ESTOQUE);
        supervisor = createUser("supervisor", UserRole.SUPERVISOR);
        gerente = createUser("gerente", UserRole.GERENTE);
        item = itemRepository.save(Item.builder()
                .code("FLT-01").name("Filtro de óleo").category(ItemCategory.FILTROS)
                .quantity(10).minimumQuantity(3).price(new BigDecimal("39.90")).build());
    }

    @Test
    void everyRoleCanListAndSeeDetails() throws Exception {
        for (User user : new User[]{estoque, supervisor, gerente}) {
            mockMvc.perform(get("/items").with(user(user)))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("FLT-01")));
            mockMvc.perform(get("/items/" + item.getId()).with(user(user)))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("R$")));
        }
    }

    @Test
    void estoqueCreatesItemWithNormalizedCodeAndAudit() throws Exception {
        mockMvc.perform(itemForm(post("/items"), "pst-001", null).with(user(estoque)))
                .andExpect(redirectedUrlPattern("/items/*"))
                .andExpect(flash().attribute(FlashMessage.SUCCESS, "Item cadastrado com sucesso."));

        Item created = itemRepository.findAll().stream()
                .filter(saved -> saved.getCode().equals("PST-001")).findFirst().orElseThrow();
        assertThat(created.getCreatedBy()).isEqualTo("estoque");
        assertThat(created.getPrice()).isEqualByComparingTo("1234.56");
        assertThat(mongoTemplate.getCollection("items").find(new Document("code", "PST-001")).first().get("price"))
                .isInstanceOf(Decimal128.class);
    }

    @Test
    void createKeepsFormDataOnDuplicatedCode() throws Exception {
        mockMvc.perform(itemForm(post("/items"), "flt-01", null).with(user(estoque)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Já existe um item com este código")))
                .andExpect(content().string(containsString("value=\"Pastilha de freio\"")));
    }

    @Test
    void createValidatesFields() throws Exception {
        mockMvc.perform(post("/items").with(csrf()).with(user(estoque))
                        .param("code", "x").param("name", "").param("category", "")
                        .param("quantity", "-1").param("minimumQuantity", "1").param("price", "-5"))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("form", "code", "name", "category", "quantity", "price"));
    }

    @Test
    void estoqueCannotEditOrDelete() throws Exception {
        mockMvc.perform(get("/items/" + item.getId() + "/edit").with(user(estoque))).andExpect(status().isForbidden());
        mockMvc.perform(itemForm(post("/items/" + item.getId()), "FLT-01", 0L).with(user(estoque)))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/items/" + item.getId() + "/delete").with(csrf()).with(user(estoque)))
                .andExpect(status().isForbidden());

        assertThat(itemRepository.findById(item.getId()).orElseThrow().getName()).isEqualTo("Filtro de óleo");
    }

    @Test
    void supervisorUpdatesButCannotDelete() throws Exception {
        mockMvc.perform(get("/items/" + item.getId() + "/edit").with(user(supervisor))).andExpect(status().isOk());
        mockMvc.perform(itemForm(post("/items/" + item.getId()), "FLT-01", item.getVersion()).with(user(supervisor)))
                .andExpect(redirectedUrl("/items/" + item.getId()));

        Item updated = itemRepository.findById(item.getId()).orElseThrow();
        assertThat(updated.getName()).isEqualTo("Pastilha de freio");
        assertThat(updated.getUpdatedBy()).isEqualTo("supervisor");

        mockMvc.perform(post("/items/" + item.getId() + "/delete").with(csrf()).with(user(supervisor)))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateDetectsConcurrentChange() throws Exception {
        mockMvc.perform(itemForm(post("/items/" + item.getId()), "FLT-01", item.getVersion() + 5).with(user(supervisor)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("alterado por outro usuário")));
    }

    @Test
    void gerenteDeletes() throws Exception {
        mockMvc.perform(post("/items/" + item.getId() + "/delete").with(csrf()).with(user(gerente)))
                .andExpect(redirectedUrl("/items"))
                .andExpect(flash().attribute(FlashMessage.SUCCESS, "Item excluído com sucesso."));

        assertThat(itemRepository.existsById(item.getId())).isFalse();
        mockMvc.perform(get("/items/" + item.getId()).with(user(gerente))).andExpect(status().isNotFound());
    }

    @Test
    void addsStockAndRespectsLimit() throws Exception {
        mockMvc.perform(addStock("5").with(user(estoque)))
                .andExpect(redirectedUrl("/items/" + item.getId()))
                .andExpect(flash().attribute(FlashMessage.SUCCESS, "Entrada de 5 unidade(s) registrada."));

        Item afterEntry = itemRepository.findById(item.getId()).orElseThrow();
        assertThat(afterEntry.getQuantity()).isEqualTo(15);
        assertThat(afterEntry.getVersion()).isEqualTo(item.getVersion() + 1);
        assertThat(afterEntry.getUpdatedBy()).isEqualTo("estoque");

        mockMvc.perform(addStock("0").with(user(estoque)))
                .andExpect(flash().attribute(FlashMessage.ERROR, "A quantidade deve ser maior que zero"));
        mockMvc.perform(addStock("999999").with(user(estoque)))
                .andExpect(flash().attribute(FlashMessage.ERROR, containsString("ultrapassaria o limite")));

        assertThat(itemRepository.findById(item.getId()).orElseThrow().getQuantity()).isEqualTo(15);
    }

    @Test
    void buttonsFollowRoleHierarchy() throws Exception {
        String detail = "/items/" + item.getId();

        mockMvc.perform(get(detail).with(user(estoque)))
                .andExpect(content().string(containsString("Entrada de estoque")))
                .andExpect(content().string(not(containsString(">Editar<"))))
                .andExpect(content().string(not(containsString(">Excluir<"))));
        mockMvc.perform(get(detail).with(user(supervisor)))
                .andExpect(content().string(containsString(">Editar<")))
                .andExpect(content().string(not(containsString(">Excluir<"))));
        mockMvc.perform(get(detail).with(user(gerente)))
                .andExpect(content().string(containsString("Entrada de estoque")))
                .andExpect(content().string(containsString(">Editar<")))
                .andExpect(content().string(containsString(">Excluir<")));
    }

    @Test
    void homeShowsStockIndicators() throws Exception {
        itemRepository.save(Item.builder().code("LOW-1").name("Item baixo").category(ItemCategory.MOTOR)
                .quantity(1).minimumQuantity(5).price(BigDecimal.ONE).build());

        mockMvc.perform(get("/").with(user(estoque)))
                .andExpect(model().attribute("totalItems", 2L))
                .andExpect(model().attribute("lowStockItems", 1L))
                .andExpect(content().string(containsString("Com estoque baixo")));
    }

    private MockHttpServletRequestBuilder itemForm(MockHttpServletRequestBuilder request, String code, Long version) {
        return request.with(csrf())
                .param("version", version == null ? "" : version.toString())
                .param("code", code)
                .param("name", "Pastilha de freio")
                .param("category", "FREIOS")
                .param("brand", "Bosch")
                .param("application", "Gol 1.0")
                .param("description", "Pastilha dianteira")
                .param("quantity", "3")
                .param("minimumQuantity", "5")
                .param("price", "1234.56")
                .param("location", "A-01");
    }

    private MockHttpServletRequestBuilder addStock(String quantity) {
        return post("/items/" + item.getId() + "/stock").with(csrf()).param("quantity", quantity);
    }
}
