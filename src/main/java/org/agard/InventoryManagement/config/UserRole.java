package org.agard.InventoryManagement.config;

public enum UserRole {
    USER("USER"),
    EDITOR("EDITOR"),
    ADMIN("ADMIN");

    public final String name;

    public final String authority;

    private UserRole(String name){
        this.name = name;
        this.authority = "ROLE_" + name;
    }


}
