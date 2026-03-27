package com.example.shopping_app.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import com.example.shopping_app.data.entity.Tickets;
import java.util.List;

@Dao
public interface TicketsDao {
    @Insert
    long insert(Tickets ticket);

    @Query("SELECT * FROM tickets WHERE userId = :userId")
    List<Tickets> getTicketsByUser(int userId);

    @Query("SELECT * FROM tickets WHERE ticketId = :id")
    Tickets getTicketById(int id);

    @Query("SELECT seatNumber FROM tickets WHERE showTimeId = :showTimeId")
    List<String> getBookedSeats(int showTimeId);
}