package com.geko.Booking.Repository.Mysql;

import com.geko.Booking.Entity.Mysql.Booking;
import com.geko.Booking.Entity.Mysql.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    void deleteByBooking(Booking booking);
}
