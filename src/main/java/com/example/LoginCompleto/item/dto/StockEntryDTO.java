package com.example.LoginCompleto.item.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class StockEntryDTO {

    @NotNull(message = "Informe a quantidade")
    @Positive(message = "A quantidade deve ser maior que zero")
    @Max(value = ItemDTO.MAX_QUANTITY, message = "A quantidade máxima por entrada é 1.000.000")
    private Integer quantity;
}
