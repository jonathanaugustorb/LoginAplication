package com.example.LoginCompleto.item.dto;

import com.example.LoginCompleto.item.entity.enums.ItemCategory;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
public class ItemFilterDTO {

    private String search;
    private ItemCategory category;
    private boolean lowStock;

    public Map<String, Object> toQueryParams() {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("search", search);
        params.put("category", category);
        params.put("lowStock", lowStock);
        return params;
    }
}
