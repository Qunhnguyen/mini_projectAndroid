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
import androidx.appcompat.app.AlertDialog;
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
    private TextView tvMovieInfo;
    private TextView tvSelectedSeat;
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
                    showBookingConfirmation();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seat_selection);

        initDependencies();
        if (showTimeId == AppConstants.INVALID_ID) {
            Toast.makeText(this, R.string.error_invalid_showtime, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initUi();
        loadShowtimeData();
    }

    private void initDependencies() {
        db = AppDatabase.getInstance(this);
        pref = new PrefManager(this);
        showTimeId = getIntent().getIntExtra(AppConstants.EXTRA_SHOWTIME_ID, AppConstants.INVALID_ID);
    }

    private void initUi() {
        gridSeats = findViewById(R.id.grid_seats);
        tvMovieInfo = findViewById(R.id.tv_seat_movie_info);
        tvSelectedSeat = findViewById(R.id.tv_selected_seat);
        btnConfirm = findViewById(R.id.btn_confirm_booking);
        ImageButton btnBack = findViewById(R.id.btn_seat_back);

        btnBack.setOnClickListener(v -> finish());
        btnConfirm.setOnClickListener(v -> handleBookingProcess());
        updateSelectedSeatLabel();
        updateConfirmButtonState();
    }

    private void handleBookingProcess() {
        if (selectedSeat.isEmpty()) {
            Toast.makeText(this, R.string.prompt_select_seat, Toast.LENGTH_SHORT).show();
            return;
        }

        if (!pref.isLoggedIn()) {
            Toast.makeText(this, R.string.prompt_login_before_booking, Toast.LENGTH_SHORT).show();
            loginLauncher.launch(new Intent(this, LoginActivity.class));
            return;
        }

        showBookingConfirmation();
    }

    private void showBookingConfirmation() {
        if (currentShowTime == null) {
            Toast.makeText(this, R.string.error_invalid_showtime, Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle(R.string.booking_dialog_title)
                .setMessage(getString(R.string.booking_dialog_message, selectedSeat))
                .setPositiveButton(R.string.action_confirm, (dialog, which) -> confirmBooking())
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }

    private void loadShowtimeData() {
        executor.execute(() -> {
            currentShowTime = db.showTimesDao().getShowTimeById(showTimeId);
            if (currentShowTime == null) {
                runOnUiThread(() -> {
                    Toast.makeText(this, R.string.error_invalid_showtime, Toast.LENGTH_SHORT).show();
                    finish();
                });
                return;
            }

            Movie movie = db.movieDao().getMovieById(currentShowTime.getMovieId());
            Theater theater = db.theaterDao().getTheaterById(currentShowTime.getTheaterId());
            List<String> bookedSeats = db.ticketsDao().getBookedSeats(showTimeId);

            runOnUiThread(() -> {
                String movieTitle = movie != null ? movie.getTitle() : getString(R.string.label_unknown_movie);
                String theaterName = theater != null ? theater.getName() : getString(R.string.label_unknown_theater);
                String info = movieTitle + "\n" + theaterName + " - " + currentShowTime.getDateTime();
                tvMovieInfo.setText(info);
                generateSeatGrid(bookedSeats);
            });
        });
    }

    private void generateSeatGrid(List<String> bookedSeats) {
        gridSeats.removeAllViews();
        int rows = 4;
        int cols = 5;

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                String seatName = (char) ('A' + row) + String.valueOf(col + 1);
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
                    seatView.setOnClickListener(v -> updateSeatSelection(seatView, seatName));
                }
                gridSeats.addView(seatView);
            }
        }
    }

    private void updateSeatSelection(TextView view, String seatName) {
        for (int index = 0; index < gridSeats.getChildCount(); index++) {
            View child = gridSeats.getChildAt(index);
            if (child.isEnabled()) {
                child.setBackgroundResource(R.drawable.bg_chip_unselected);
            }
        }

        selectedSeat = seatName;
        view.setBackgroundResource(R.drawable.bg_chip_selected);
        updateSelectedSeatLabel();
        updateConfirmButtonState();
    }

    private void updateSelectedSeatLabel() {
        String seatLabel = selectedSeat.isEmpty()
                ? getString(R.string.label_seat_not_selected)
                : selectedSeat;
        tvSelectedSeat.setText(getString(R.string.selected_seat_label, seatLabel));
    }

    private void updateConfirmButtonState() {
        boolean enabled = !selectedSeat.isEmpty();
        btnConfirm.setEnabled(enabled);
        btnConfirm.setAlpha(enabled ? 1f : 0.6f);
    }

    private void confirmBooking() {
        executor.execute(() -> {
            String bookingDate = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date());
            Tickets ticket = new Tickets(
                    showTimeId,
                    pref.getUserId(),
                    selectedSeat,
                    bookingDate,
                    currentShowTime.getPrice()
            );
            db.ticketsDao().insert(ticket);

            runOnUiThread(() -> {
                Toast.makeText(this, R.string.booking_success, Toast.LENGTH_LONG).show();
                startActivity(new Intent(this, MyTicketsActivity.class));
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
