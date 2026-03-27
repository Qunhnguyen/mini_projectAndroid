package com.example.shopping_app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.shopping_app.data.AppDatabase;
import com.example.shopping_app.data.entity.Movie;
import com.example.shopping_app.data.entity.ShowTimes;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ShowtimesActivity extends AppCompatActivity {
    private RecyclerView rvShowtimes;
    private ShowtimeAdapter adapter;
    private AppDatabase db;
    private int movieId;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_showtimes);

        db = AppDatabase.getInstance(this);
        movieId = getIntent().getIntExtra("MOVIE_ID", -1);

        TextView tvTitle = findViewById(R.id.tv_movie_title_detail);
        ImageButton btnBack = findViewById(R.id.btn_showtimes_back);
        rvShowtimes = findViewById(R.id.rv_showtimes);

        btnBack.setOnClickListener(v -> finish());

        rvShowtimes.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ShowtimeAdapter(new ArrayList<>(), db, showTime -> {
            Intent intent = new Intent(this, SeatSelectionActivity.class);
            intent.putExtra("SHOWTIME_ID", showTime.getShowTimeId());
            startActivity(intent);
        });
        rvShowtimes.setAdapter(adapter);

        executor.execute(() -> {
            Movie movie = db.movieDao().getMovieById(movieId);
            List<ShowTimes> list = db.showTimesDao().getShowTimesByMovie(movieId);
            runOnUiThread(() -> {
                if (movie != null) tvTitle.setText(movie.getTitle());
                adapter = new ShowtimeAdapter(list, db, showTime -> {
                    Intent intent = new Intent(this, SeatSelectionActivity.class);
                    intent.putExtra("SHOWTIME_ID", showTime.getShowTimeId());
                    startActivity(intent);
                });
                rvShowtimes.setAdapter(adapter);
            });
        });
    }
}