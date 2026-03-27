package com.example.shopping_app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

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
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private RecyclerView rvShowtimes;
    private TextView tvTitle;
    private TextView tvEmptyState;
    private ShowtimeAdapter adapter;
    private AppDatabase db;
    private int movieId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_showtimes);

        db = AppDatabase.getInstance(this);
        movieId = getIntent().getIntExtra(AppConstants.EXTRA_MOVIE_ID, AppConstants.INVALID_ID);

        if (movieId == AppConstants.INVALID_ID) {
            Toast.makeText(this, R.string.error_invalid_movie, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        tvTitle = findViewById(R.id.tv_movie_title_detail);
        tvEmptyState = findViewById(R.id.tv_showtimes_empty);
        ImageButton btnBack = findViewById(R.id.btn_showtimes_back);
        rvShowtimes = findViewById(R.id.rv_showtimes);

        btnBack.setOnClickListener(v -> finish());

        rvShowtimes.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ShowtimeAdapter(new ArrayList<>(), db, showTime -> {
            Intent intent = new Intent(this, SeatSelectionActivity.class);
            intent.putExtra(AppConstants.EXTRA_SHOWTIME_ID, showTime.getShowTimeId());
            startActivity(intent);
        });
        rvShowtimes.setAdapter(adapter);

        loadShowtimes();
    }

    private void loadShowtimes() {
        executor.execute(() -> {
            Movie movie = db.movieDao().getMovieById(movieId);
            List<ShowTimes> showtimes = db.showTimesDao().getShowTimesByMovie(movieId);

            runOnUiThread(() -> {
                if (movie == null) {
                    Toast.makeText(this, R.string.error_invalid_movie, Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                tvTitle.setText(movie.getTitle());
                adapter.updateData(showtimes);

                boolean hasData = !showtimes.isEmpty();
                rvShowtimes.setVisibility(hasData ? RecyclerView.VISIBLE : RecyclerView.GONE);
                tvEmptyState.setVisibility(hasData ? TextView.GONE : TextView.VISIBLE);
            });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
