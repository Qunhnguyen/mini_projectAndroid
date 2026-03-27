package com.example.shopping_app.data.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "show_times",
        foreignKeys = {
                @ForeignKey(entity = Movie.class,
                        parentColumns = "movieId",
                        childColumns = "movieId",
                        onDelete = ForeignKey.CASCADE),
                @ForeignKey(entity = Theater.class,
                        parentColumns = "theaterId",
                        childColumns = "theaterId",
                        onDelete = ForeignKey.CASCADE)
        },
        indices = {@Index("movieId"), @Index("theaterId")})
public class ShowTimes {
    @PrimaryKey(autoGenerate = true)
    private int showTimeId;
    private int movieId;
    private int theaterId;
    private String dateTime;
    private double price;

    public ShowTimes(int movieId, int theaterId, String dateTime, double price) {
        this.movieId = movieId;
        this.theaterId = theaterId;
        this.dateTime = dateTime;
        this.price = price;
    }

    public int getShowTimeId() { return showTimeId; }
    public void setShowTimeId(int showTimeId) { this.showTimeId = showTimeId; }
    public int getMovieId() { return movieId; }
    public void setMovieId(int movieId) { this.movieId = movieId; }
    public int getTheaterId() { return theaterId; }
    public void setTheaterId(int theaterId) { this.theaterId = theaterId; }
    public String getDateTime() { return dateTime; }
    public void setDateTime(String dateTime) { this.dateTime = dateTime; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
}