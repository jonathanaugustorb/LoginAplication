package com.example.LoginCompleto.item.entity;

import com.example.LoginCompleto.common.entity.BaseEntity;
import com.example.LoginCompleto.item.entity.enums.ItemCategory;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;

@Document(collection = "items")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor

public class Item extends BaseEntity {
    @Indexed(unique = true)
    private String code;

    @Indexed
    private String name;

    private String description;

    @Indexed
    private ItemCategory category;

    private String brand;
    private String application;
    private int quantity;
    private int minimumQuantity;
    private BigDecimal price;
    private String location;

    public boolean isLowStock() {
        return quantity <= minimumQuantity;
    }
}
