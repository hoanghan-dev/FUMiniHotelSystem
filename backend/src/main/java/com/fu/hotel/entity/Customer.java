package com.fu.hotel.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Customer")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CustomerID")
    private Integer customerID;

    @Column(name = "CustomerFullName", nullable = false, length = 100)
    private String customerFullName;

    @Column(name = "Telephone", length = 12)
    private String telephone;

    @Column(name = "EmailAddress", nullable = false, unique = true, length = 100)
    private String emailAddress;

    @Column(name = "CustomerBirthday")
    private LocalDate customerBirthday;

    @Column(name = "CustomerStatus", length = 20)
    private String customerStatus; // "1" for Active, "0" for Inactive

    @Column(name = "Password", nullable = false, length = 100)
    private String password;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<BookingReservation> bookingReservations = new ArrayList<>();

    public Customer() {
    }

    public Customer(Integer customerID, String customerFullName, String telephone,
                    String emailAddress, LocalDate customerBirthday, String customerStatus,
                    String password) {
        this.customerID = customerID;
        this.customerFullName = customerFullName;
        this.telephone = telephone;
        this.emailAddress = emailAddress;
        this.customerBirthday = customerBirthday;
        this.customerStatus = customerStatus;
        this.password = password;
    }

    public Integer getCustomerID() {
        return customerID;
    }

    public void setCustomerID(Integer customerID) {
        this.customerID = customerID;
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

    public String getCustomerStatus() {
        return customerStatus;
    }

    public void setCustomerStatus(String customerStatus) {
        this.customerStatus = customerStatus;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<BookingReservation> getBookingReservations() {
        return bookingReservations;
    }

    public void setBookingReservations(List<BookingReservation> bookingReservations) {
        this.bookingReservations = bookingReservations;
    }
}
