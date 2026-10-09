package com.example.LoginCompleto.item.entity.enums;

import com.example.LoginCompleto.common.enums.ILabeledEnum;

public enum ItemCategory implements ILabeledEnum {
    MOTOR("Motor"),
    FREIOS("Freios"),
    SUSPENSAO("Suspensão"),
    ELETRICA("Elétrica"),
    FILTROS("Filtros"),
    OLEOS_FLUIDOS("Óleos e fluidos"),
    TRANSMISSAO("Transmissão"),
    ARREFECIMENTO("Arrefecimento"),
    FERRAMENTAS("Ferramentas");

    private String category;

    ItemCategory(String category) {
        this.category = category;
    }

    @Override
    public String getLabel() {
        return category;
    }
}
