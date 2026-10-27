package com.fu.hotel.dto.request;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public class CustomerUpdateRequest {

    @NotBlank(message = "Customer full name is required")
    @Size(max = 100, message = "Customer full name must be at most 100 characters")
    private String customerFullName;

    @NotBlank(message = "Telephone is required")
    @Pattern(regexp = "^[0-9]{10,12}$", message = "Telephone must be 10-12 digits")
    private String telephone;

    @NotBlank(message = "Email address is required")
    @Email(message = "Email format is invalid")
    @Size(max = 100, message = "Email address must be at most 100 characters")
    private String emailAddress;

    @NotNull(message = "Customer birthday is required")
    @Past(message = "Birthday must be a date in the past")
    private LocalDate customerBirthday;

    @Size(min = 6, message = "Password must be at least 6 characters if provided")
    private String password;

    private String customerStatus;

    public CustomerUpdateRequest() {
    }

    public CustomerUpdateRequest(String customerFullName, String telephone, String emailAddress,
                                 LocalDate customerBirthday, String password, String customerStatus) {
        this.customerFullName = customerFullName;
        this.telephone = telephone;
        this.emailAddress = emailAddress;
        this.customerBirthday = customerBirthday;
        this.password = password;
        this.customerStatus = customerStatus;
    }

    public String getCustomerFullName() {
        return customerFullName;
    }

    public void setCustomerFullName(String customerFullName) {
        this.customerFullName = customerFullName;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public LocalDate getCustomerBirthday() {
        return customerBirthday;
    }

    public void setCustomerBirthday(LocalDate customerBirthday) {
        this.customerBirthday = customerBirthday;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getCustomerStatus() {
        return customerStatus;
    }

    public void setCustomerStatus(String customerStatus) {
        this.customerStatus = customerStatus;
    }
}
