package com.fu.hotel.dto.response;

public class AuthResponse {

    private String token;
    private String type = "Bearer";
    private String role; // "STAFF" or "CUSTOMER"
    private Integer customerId; // null if staff
    private String fullName;
    private String email;

    public AuthResponse() {
    }

    public AuthResponse(String token, String role, Integer customerId, String fullName, String email) {
        this.token = token;
        this.role = role;
        this.customerId = customerId;
        this.fullName = fullName;
        this.email = email;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Integer getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Integer customerId) {
        this.customerId = customerId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
