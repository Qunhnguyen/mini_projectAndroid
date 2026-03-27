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

/**
 * Activity xử lý việc lựa chọn chỗ ngồi và xác nhận đặt vé phim.
 * Luồng: Chọn ghế -> Kiểm tra đăng nhập -> Xác nhận thanh toán -> Lưu vé.
 */
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

    // Launcher xử lý kết quả trả về từ LoginActivity
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

        initDatabase();
        initUI();
        loadShowtimeData();
    }

    private void initDatabase() {
        db = AppDatabase.getInstance(this);
        pref = new PrefManager(this);
        showTimeId = getIntent().getIntExtra("SHOWTIME_ID", -1);
    }

    private void initUI() {
        gridSeats = findViewById(R.id.grid_seats);
        tvMovieInfo = findViewById(R.id.tv_seat_movie_info);
        tvSelectedSeat = findViewById(R.id.tv_selected_seat);
        btnConfirm = findViewById(R.id.btn_confirm_booking);
        ImageButton btnBack = findViewById(R.id.btn_seat_back);

        btnBack.setOnClickListener(v -> finish());
        btnConfirm.setOnClickListener(v -> handleBookingProcess());
    }

    private void handleBookingProcess() {
        if (selectedSeat.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn một vị trí ghế!", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (!pref.isLoggedIn()) {
            Toast.makeText(this, "Bạn cần đăng nhập để thực hiện đặt vé!", Toast.LENGTH_SHORT).show();
            loginLauncher.launch(new Intent(this, LoginActivity.class));
        } else {
            showBookingConfirmation();
        }
    }

    /**
     * Hiển thị hộp thoại xác nhận trước khi lưu vào database (Tăng tính trải nghiệm người dùng)
     */
    private void showBookingConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận đặt vé")
                .setMessage("Bạn có chắc chắn muốn đặt ghế " + selectedSeat + " cho suất chiếu này không?")
                .setPositiveButton("Xác nhận", (dialog, which) -> confirmBooking())
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void loadShowtimeData() {
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
                    seatView.setOnClickListener(v -> updateSeatSelection(seatView, seatName));
                }
                gridSeats.addView(seatView);
            }
        }
    }

    private void updateSeatSelection(TextView view, String seatName) {
        // Reset trạng thái các ghế khác
        for (int k = 0; k < gridSeats.getChildCount(); k++) {
            View child = gridSeats.getChildAt(k);
            if (child.isEnabled()) {
                child.setBackgroundResource(R.drawable.bg_chip_unselected);
            }
        }
        // Cập nhật ghế mới chọn
        selectedSeat = seatName;
        view.setBackgroundResource(R.drawable.bg_chip_selected);
        tvSelectedSeat.setText("Ghế đã chọn: " + selectedSeat);
    }

    private void confirmBooking() {
        executor.execute(() -> {
            String date = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date());
            Tickets ticket = new Tickets(showTimeId, pref.getUserId(), selectedSeat, date, currentShowTime.getPrice());
            db.ticketsDao().insert(ticket);
            
            runOnUiThread(() -> {
                Toast.makeText(this, "Đặt vé thành công!", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(this, MyTicketsActivity.class);
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