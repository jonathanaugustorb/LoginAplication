package com.example.LoginCompleto.item.mapper;

import com.example.LoginCompleto.item.dto.ItemDTO;
import com.example.LoginCompleto.item.entity.Item;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class ItemMapper {

    public Item toEntity(ItemDTO dto) {
        var item = new Item();
        updateEntity(item, dto);
        return item;
    }

    public ItemDTO toDTO(Item item) {
        var dto = new ItemDTO();
        dto.setVersion(item.getVersion());
        dto.setCode(item.getCode());
        dto.setName(item.getName());
        dto.setDescription(item.getDescription());
        dto.setCategory(item.getCategory());
        dto.setBrand(item.getBrand());
        dto.setApplication(item.getApplication());
        dto.setQuantity(item.getQuantity());
        dto.setMinimumQuantity(item.getMinimumQuantity());
        dto.setPrice(item.getPrice());
        dto.setLocation(item.getLocation());
        return dto;
    }

    public void updateEntity(Item item, ItemDTO dto) {
        item.setCode(normalizeCode(dto.getCode()));
        item.setName(dto.getName());
        item.setDescription(emptyToNull(dto.getDescription()));
        item.setCategory(dto.getCategory());
        item.setBrand(emptyToNull(dto.getBrand()));
        item.setApplication(emptyToNull(dto.getApplication()));
        item.setQuantity(dto.getQuantity());
        item.setMinimumQuantity(dto.getMinimumQuantity());
        item.setPrice(dto.getPrice());
        item.setLocation(emptyToNull(dto.getLocation()));
    }

    public String normalizeCode(String code) {
        return code.trim().toUpperCase(Locale.ROOT);
    }

    private String emptyToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
