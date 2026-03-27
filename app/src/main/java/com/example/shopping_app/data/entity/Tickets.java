package com.example.shopping_app.data.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "tickets",
        foreignKeys = {
                @ForeignKey(entity = ShowTimes.class,
                        parentColumns = "showTimeId",
                        childColumns = "showTimeId",
                        onDelete = ForeignKey.CASCADE),
                @ForeignKey(entity = User.class,
                        parentColumns = "userId",
                        childColumns = "userId",
                        onDelete = ForeignKey.CASCADE)
        },
        indices = {@Index("showTimeId"), @Index("userId")})
public class Tickets {
    @PrimaryKey(autoGenerate = true)
    private int ticketId;
    private int showTimeId;
    private int userId;
    private String seatNumber;
    private String bookingDate;
    private double totalPrice;

    public Tickets(int showTimeId, int userId, String seatNumber, String bookingDate, double totalPrice) {
        this.showTimeId = showTimeId;
        this.userId = userId;
        this.seatNumber = seatNumber;
        this.bookingDate = bookingDate;
        this.totalPrice = totalPrice;
    }

    public int getTicketId() { return ticketId; }
    public void setTicketId(int ticketId) { this.ticketId = ticketId; }
    public int getShowTimeId() { return showTimeId; }
    public void setShowTimeId(int showTimeId) { this.showTimeId = showTimeId; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getSeatNumber() { return seatNumber; }
    public void setSeatNumber(String seatNumber) { this.seatNumber = seatNumber; }
    public String getBookingDate() { return bookingDate; }
    public void setBookingDate(String bookingDate) { this.bookingDate = bookingDate; }
    public double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }
}