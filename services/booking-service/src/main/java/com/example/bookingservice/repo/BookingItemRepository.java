package com.example.bookingservice.repo;

import com.example.bookingservice.model.BookingItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface BookingItemRepository extends JpaRepository<BookingItem, UUID> {

    @Query("""
       select bi.seatNo
       from BookingItem bi
       join bi.booking b
       where b.eventId = :eventId
         and b.status = 'CONFIRMED'
    """)
    List<Integer> findConfirmedSeatNos(@Param("eventId") UUID eventId);
}
