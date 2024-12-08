package com.geko.Booking.Entity.Mysql;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;


import java.util.List;

@Entity
@Table(name = "homeowners")
@EqualsAndHashCode(callSuper = true)
@Data
@DynamicUpdate
@DynamicInsert
@AllArgsConstructor
public class Homeowner extends User {
    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Listing> listings;

    @OneToMany(mappedBy = "homeowner", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Booking> bookings;

    public Homeowner() {
        super.setRole(Role.HOMEOWNER);
    }
}