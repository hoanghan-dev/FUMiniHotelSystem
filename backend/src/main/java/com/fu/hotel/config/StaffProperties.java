package com.fu.hotel.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class StaffProperties {

    @Value("${app.staff.email:staff@fuminihotel.com}")
    private String email;

    @Value("${app.staff.password:Staff@123}")
    private String password;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
