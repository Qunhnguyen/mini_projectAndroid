package com.example.shopping_app;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.example.shopping_app.data.AppDatabase;
import com.example.shopping_app.data.entity.Movie;
import com.example.shopping_app.data.entity.ShowTimes;
import com.example.shopping_app.data.entity.Theater;
import com.example.shopping_app.data.entity.Tickets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SeatSelectionActivity extends AppCompatActivity {
    private GridLayout gridSeats;
    private TextView tvMovieInfo, tvSelectedSeat;
    private Button btnConfirm;
    private AppDatabase db;
    private PrefManager pref;
    private int showTimeId;
    private String selectedSeat = "";
    private ShowTimes currentShowTime;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private final ActivityResultLauncher<Intent> loginLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (pref.isLoggedIn()) {
                    confirmBooking();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seat_selection);

        db = AppDatabase.getInstance(this);
        pref = new PrefManager(this);
        showTimeId = getIntent().getIntExtra("SHOWTIME_ID", -1);

        gridSeats = findViewById(R.id.grid_seats);
        tvMovieInfo = findViewById(R.id.tv_seat_movie_info);
        tvSelectedSeat = findViewById(R.id.tv_selected_seat);
        btnConfirm = findViewById(R.id.btn_confirm_booking);
        ImageButton btnBack = findViewById(R.id.btn_seat_back);

        btnBack.setOnClickListener(v -> finish());
        btnConfirm.setOnClickListener(v -> {
            if (selectedSeat.isEmpty()) {
                Toast.makeText(this, "Vui lòng chọn ghế!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!pref.isLoggedIn()) {
                loginLauncher.launch(new Intent(this, LoginActivity.class));
            } else {
                confirmBooking();
            }
        });

        loadData();
    }

    private void loadData() {
        executor.execute(() -> {
            currentShowTime = db.showTimesDao().getShowTimeById(showTimeId);
            if (currentShowTime != null) {
                Movie movie = db.movieDao().getMovieById(currentShowTime.getMovieId());
                Theater theater = db.theaterDao().getTheaterById(currentShowTime.getTheaterId());
                List<String> bookedSeats = db.ticketsDao().getBookedSeats(showTimeId);

                runOnUiThread(() -> {
                    String info = movie.getTitle() + "\n" + theater.getName() + " - " + currentShowTime.getDateTime();
                    tvMovieInfo.setText(info);
                    generateSeatGrid(bookedSeats);
                });
            }
        });
    }

    private void generateSeatGrid(List<String> bookedSeats) {
        gridSeats.removeAllViews();
        int rows = 4;
        int cols = 5;

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                String seatName = (char) ('A' + i) + String.valueOf(j + 1);
                TextView seatView = new TextView(this);
                seatView.setText(seatName);
                seatView.setPadding(20, 20, 20, 20);
                seatView.setGravity(Gravity.CENTER);
                
                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.setMargins(10, 10, 10, 10);
                seatView.setLayoutParams(params);

                if (bookedSeats.contains(seatName)) {
                    seatView.setBackgroundColor(Color.LTGRAY);
                    seatView.setEnabled(false);
                } else {
                    seatView.setBackgroundResource(R.drawable.bg_chip_unselected);
                    seatView.setOnClickListener(v -> {
                        // Reset previous selection
                        for (int k = 0; k < gridSeats.getChildCount(); k++) {
                            View child = gridSeats.getChildAt(k);
                            if (child.isEnabled()) {
                                child.setBackgroundResource(R.drawable.bg_chip_unselected);
                            }
                        }
                        // Select current
                        selectedSeat = seatName;
                        seatView.setBackgroundResource(R.drawable.bg_chip_selected);
                        tvSelectedSeat.setText("Ghế đã chọn: " + selectedSeat);
                    });
                }
                gridSeats.addView(seatView);
            }
        }
    }

    private void confirmBooking() {
        executor.execute(() -> {
            String date = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date());
            Tickets ticket = new Tickets(showTimeId, pref.getUserId(), selectedSeat, date, currentShowTime.getPrice());
            long id = db.ticketsDao().insert(ticket);
            
            runOnUiThread(() -> {
                Toast.makeText(this, "Đặt vé thành công!", Toast.LENGTH_LONG).show();
                // Luồng: Hiển thị vé đã đặt
                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}