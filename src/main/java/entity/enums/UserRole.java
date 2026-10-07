package entity.enums;

public enum UserRole {
    VENDEDOR("vendedor"),
    GERENTE("gerente"),
    DONO("dono");

    private String role;

     UserRole(String role) {
        this.role = role;
    }

    public String getRole() {
        return role;
    }
}
