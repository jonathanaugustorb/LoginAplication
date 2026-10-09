package com.example.LoginCompleto.user.entity.enums;

import com.example.LoginCompleto.common.enums.ILabeledEnum;

// Ordem crescente de permissão: cada perfil herda as permissões dos anteriores.
public enum UserRole implements ILabeledEnum {
    ESTOQUE("Estoque"),
    SUPERVISOR("Supervisor"),
    GERENTE("Gerente");

    private static final String AUTHORITY_PREFIX = "ROLE_";

    private String role;

     UserRole(String role) {
        this.role = role;
    }

    public String getRole() {
        return role;
    }

    @Override
    public String getLabel() {
        return role;
    }

    public String getAuthority() {
        return AUTHORITY_PREFIX + name();
    }
}
