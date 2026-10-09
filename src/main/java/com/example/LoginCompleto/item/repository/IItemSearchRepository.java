package com.example.LoginCompleto.item.repository;

import com.example.LoginCompleto.item.dto.ItemFilterDTO;
import com.example.LoginCompleto.item.entity.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IItemSearchRepository {

    Page<Item> search(ItemFilterDTO filter, Pageable pageable);

    long countLowStock();

    boolean addQuantity(String id, int amount, int maxQuantity, String updatedBy);
}
