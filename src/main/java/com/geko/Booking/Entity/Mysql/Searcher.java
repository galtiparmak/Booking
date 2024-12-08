package com.geko.Booking.Entity.Mysql;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.util.List;

@Entity
@Table(name = "searchers")
@EqualsAndHashCode(callSuper = true)
@Data
@DynamicInsert
@DynamicUpdate
@AllArgsConstructor
public class Searcher extends User {
    @OneToMany(mappedBy = "searcher", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Booking> bookings;

    public Searcher() {
        super.setRole(Role.SEARCHER);
    }
}
