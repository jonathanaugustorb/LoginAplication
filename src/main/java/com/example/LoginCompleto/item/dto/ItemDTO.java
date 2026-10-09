package com.example.LoginCompleto.item.dto;

import com.example.LoginCompleto.item.entity.enums.ItemCategory;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class ItemDTO {

    public static final int MAX_QUANTITY = 1_000_000;

    private Long version;

    @NotBlank(message = "Informe o código")
    @Pattern(regexp = "^[A-Za-z0-9._-]{2,30}$",
            message = "O código deve ter de 2 a 30 caracteres: letras, números, ponto, hífen ou sublinhado")
    private String code;

    @NotBlank(message = "Informe o nome")
    @Size(max = 120, message = "O nome deve ter no máximo 120 caracteres")
    private String name;

    @Size(max = 1000, message = "A descrição deve ter no máximo 1000 caracteres")
    private String description;

    @NotNull(message = "Selecione a categoria")
    private ItemCategory category;

    @Size(max = 80, message = "A marca deve ter no máximo 80 caracteres")
    private String brand;

    @Size(max = 255, message = "A aplicação deve ter no máximo 255 caracteres")
    private String application;

    @NotNull(message = "Informe a quantidade")
    @PositiveOrZero(message = "A quantidade não pode ser negativa")
    @Max(value = MAX_QUANTITY, message = "A quantidade máxima é 1.000.000")
    private Integer quantity;

    @NotNull(message = "Informe o estoque mínimo")
    @PositiveOrZero(message = "O estoque mínimo não pode ser negativo")
    @Max(value = MAX_QUANTITY, message = "O estoque mínimo máximo é 1.000.000")
    private Integer minimumQuantity;

    @NotNull(message = "Informe o preço")
    @DecimalMin(value = "0.00", message = "O preço não pode ser negativo")
    @Digits(integer = 10, fraction = 2, message = "Informe um preço válido, com até 2 casas decimais")
    private BigDecimal price;

    @Size(max = 50, message = "A localização deve ter no máximo 50 caracteres")
    private String location;
}
