package com.example.LoginCompleto.infra.security;

public final class Permissions {

    public static final String ESTOQUE = "hasRole('ESTOQUE')";
    public static final String SUPERVISOR = "hasRole('SUPERVISOR')";
    public static final String GERENTE = "hasRole('GERENTE')";

    private Permissions() {
    }
}
