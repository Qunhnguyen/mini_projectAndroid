package com.example.shopping_app.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import com.example.shopping_app.data.entity.Theater;
import java.util.List;

@Dao
public interface TheaterDao {
    @Insert
    void insert(Theater theater);

    @Query("SELECT * FROM theaters")
    List<Theater> getAllTheaters();

    @Query("SELECT * FROM theaters WHERE theaterId = :id")
    Theater getTheaterById(int id);
}