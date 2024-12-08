package com.geko.Booking.Entity.Mysql;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;

@Entity
@Table(name = "listings")
@Data
@Builder
@DynamicUpdate
@DynamicInsert
@AllArgsConstructor
@NoArgsConstructor
public class Listing implements Serializable {
    @Id
    private String id;
    @ManyToOne
    @JoinColumn(name = "owner_id", nullable = false)
    private Homeowner owner;
    // {
    //  "id": 12,
    //  "owner": {
    //    "id": 1
    //  }
    //}
}
