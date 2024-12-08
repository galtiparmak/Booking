package com.geko.Booking.DTO;

import com.geko.Booking.Entity.Mysql.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SearcherDTO implements Serializable {
    private String username;
    private String email;
    private String phone;
    private Role role;
    private List<BookingDTO> bookings;
}

