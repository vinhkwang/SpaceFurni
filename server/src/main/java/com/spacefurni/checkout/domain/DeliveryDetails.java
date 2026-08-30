package com.spacefurni.checkout.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class DeliveryDetails {

    @Column(name = "delivery_full_name", nullable = false)
    private String fullName;

    @Column(name = "delivery_phone", nullable = false)
    private String phone;

    @Column(name = "delivery_street", nullable = false)
    private String street;

    @Column(name = "delivery_district", nullable = false)
    private String district;

    @Column(name = "delivery_city", nullable = false)
    private String city;

    @Column(name = "delivery_note")
    private String note;

    protected DeliveryDetails() {
    }

    public DeliveryDetails(String fullName, String phone, String street, String district, String city, String note) {
        this.fullName = fullName;
        this.phone = phone;
        this.street = street;
        this.district = district;
        this.city = city;
        this.note = note;
    }

    public String getFullName() {
        return fullName;
    }

    public String getPhone() {
        return phone;
    }

    public String getStreet() {
        return street;
    }

    public String getDistrict() {
        return district;
    }

    public String getCity() {
        return city;
    }

    public String getNote() {
        return note;
    }
}
