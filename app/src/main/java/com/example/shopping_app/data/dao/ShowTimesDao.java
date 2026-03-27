package com.example.shopping_app.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import com.example.shopping_app.data.entity.ShowTimes;
import java.util.List;

@Dao
public interface ShowTimesDao {
    @Insert
    void insert(ShowTimes showTime);

    @Query("SELECT * FROM show_times WHERE movieId = :movieId")
    List<ShowTimes> getShowTimesByMovie(int movieId);

    @Query("SELECT * FROM show_times WHERE showTimeId = :id")
    ShowTimes getShowTimeById(int id);
}