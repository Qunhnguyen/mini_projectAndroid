package com.example.shopping_app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.shopping_app.data.AppDatabase;
import com.example.shopping_app.data.entity.Movie;
import com.example.shopping_app.data.entity.Theater;
import com.example.shopping_app.data.entity.ShowTimes;
import com.example.shopping_app.data.entity.User;
import com.google.android.material.button.MaterialButton;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Màn hình chính của ứng dụng Movie Ticket.
 * Hiển thị danh sách phim đang chiếu và quản lý trạng thái người dùng.
 */
public class MainActivity extends AppCompatActivity {

    private AppDatabase db;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private MovieAdapter movieAdapter;
    private List<Movie> movieList = new ArrayList<>();
    private PrefManager pref;

    private TextView tvHomeGreeting, tvHomeSummary, tvHomeCartSummary, tvProductsHint;
    private MaterialButton btnHomePrimary, btnHomeSecondary;
    private LinearLayout layoutCategories;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupDependencies();
        initViews();
        setupRecyclerView();
        attachListeners();
        executeDataLoading();
    }

    /**
     * Khởi tạo các thành phần cốt lõi của ứng dụng
     */
    private void setupDependencies() {
        db = AppDatabase.getInstance(this);
        pref = new PrefManager(this);
    }

    /**
     * Ánh xạ các thành phần giao diện từ XML
     */
    private void initViews() {
        tvHomeGreeting = findViewById(R.id.tv_home_greeting);
        tvHomeSummary = findViewById(R.id.tv_home_summary);
        tvHomeCartSummary = findViewById(R.id.tv_home_cart_summary);
        tvProductsHint = findViewById(R.id.tv_products_hint);
        btnHomePrimary = findViewById(R.id.btn_home_primary);
        btnHomeSecondary = findViewById(R.id.btn_home_secondary);
        layoutCategories = findViewById(R.id.layout_categories);
        
        if (tvProductsHint != null) {
            tvProductsHint.setText("Đang tải danh sách phim...");
        }
    }

    /**
     * Thiết lập danh sách hiển thị phim (RecyclerView)
     */
    private void setupRecyclerView() {
        RecyclerView rvMovies = findViewById(R.id.rv_products);
        if (rvMovies != null) {
            rvMovies.setLayoutManager(new GridLayoutManager(this, 2));
            movieAdapter = new MovieAdapter(movieList, movie -> {
                navigateToShowtimes(movie.getMovieId());
            });
            rvMovies.setAdapter(movieAdapter);
        }
    }

    /**
     * Gán các sự kiện click cho các nút bấm
     */
    private void attachListeners() {
        // Nút trên Toolbar
        ImageButton btnCart = findViewById(R.id.btn_toolbar_cart);
        if (btnCart != null) {
            btnCart.setOnClickListener(v -> navigateToMyTickets());
        }

        // Nút trên thẻ chào mừng
        btnHomePrimary.setOnClickListener(v -> handlePrimaryAction());
        btnHomeSecondary.setOnClickListener(v -> navigateToMyTickets());
    }

    /**
     * Bắt đầu tiến trình tải dữ liệu từ database
     */
    private void executeDataLoading() {
        executorService.execute(() -> {
            try {
                seedData();
                loadMovies();
                updateUserInterface();
            } catch (Exception e) {
                Log.e("MainActivity", "Lỗi tải dữ liệu: " + e.getMessage());
            }
        });
    }

    private void handlePrimaryAction() {
        if (pref.isLoggedIn()) {
            pref.logout();
            showToast("Đã đăng xuất thành công!");
            updateUserInterface();
        } else {
            startActivity(new Intent(this, LoginActivity.class));
        }
    }

    private void navigateToShowtimes(int movieId) {
        Intent intent = new Intent(MainActivity.this, ShowtimesActivity.class);
        intent.putExtra("MOVIE_ID", movieId);
        startActivity(intent);
    }

    private void navigateToMyTickets() {
        if (pref.isLoggedIn()) {
            startActivity(new Intent(this, MyTicketsActivity.class));
        } else {
            showToast("Vui lòng đăng nhập để xem vé!");
            startActivity(new Intent(this, LoginActivity.class));
        }
    }

    private void loadMovies() {
        List<Movie> movies = db.movieDao().getAllMovies();
        runOnUiThread(() -> {
            if (movieAdapter != null) {
                movieAdapter.updateData(movies);
            }
            if (tvProductsHint != null) {
                tvProductsHint.setText(movies.size() + " bộ phim đang chiếu");
            }
        });
    }

    private void updateUserInterface() {
        runOnUiThread(() -> {
            if (pref.isLoggedIn()) {
                tvHomeGreeting.setText("Xin chào!");
                tvHomeSummary.setText("Chào mừng bạn quay trở lại. Hãy chọn phim và đặt vé ngay.");
                btnHomePrimary.setText("Đăng xuất");
                btnHomeSecondary.setText("Vé của tôi");
                tvHomeCartSummary.setVisibility(View.GONE);
            } else {
                tvHomeGreeting.setText("Movie Ticket App");
                tvHomeSummary.setText("Khám phá hàng ngàn bộ phim bom tấn và đặt vé dễ dàng.");
                tvHomeCartSummary.setVisibility(View.VISIBLE);
                tvHomeCartSummary.setText("Tài khoản trải nghiệm: admin / 123456");
                btnHomePrimary.setText("Đăng nhập");
                btnHomeSecondary.setText("Bắt đầu ngay");
            }
        });
    }

    private void seedData() {
        if (db.movieDao().getAllMovies().isEmpty()) {
            // Khởi tạo dữ liệu người dùng mặc định
            db.userDao().insert(new User("admin", "123456", "Quản trị viên", "admin@movieticket.com"));

            // Danh sách phim bom tấn
            db.movieDao().insert(new Movie("Oppenheimer", "Kiệt tác về cha đẻ bom nguyên tử của Christopher Nolan", 180, "Tiểu sử / Chính kịch", "https://images.unsplash.com/photo-1485846234645-a62644f84728?q=80&w=2059&auto=format&fit=crop"));
            db.movieDao().insert(new Movie("Interstellar", "Hành trình vĩ đại xuyên không gian để cứu lấy nhân loại", 169, "Khoa học viễn tưởng", "https://images.unsplash.com/photo-1446776811953-b23d57bd21aa?q=80&w=2072&auto=format&fit=crop"));
            db.movieDao().insert(new Movie("Joker: Folie à Deux", "Sự điên rồ nhân đôi trong phần tiếp theo đầy ám ảnh", 138, "Tội phạm / Nhạc kịch", "https://images.unsplash.com/photo-1531259683007-016a7b628fc3?q=80&w=1974&auto=format&fit=crop"));
            db.movieDao().insert(new Movie("Avatar: The Way of Water", "Trở lại hành tinh Pandora với những khung hình đại dương choáng ngợp", 192, "Hành động / Phiêu lưu", "https://images.unsplash.com/photo-1598327105666-5b89351aff97?q=80&w=2070&auto=format&fit=crop"));

            // Hệ thống rạp chiếu
            db.theaterDao().insert(new Theater("CGV Vincom Center", "72 Lê Thánh Tôn, Quận 1"));
            db.theaterDao().insert(new Theater("Lotte Cinema Cantavil", "Tầng 7 Cantavil Premier, Quận 2"));

            List<Movie> movies = db.movieDao().getAllMovies();
            List<Theater> theaters = db.theaterDao().getAllTheaters();

            if (!movies.isEmpty() && !theaters.isEmpty()) {
                // Thiết lập lịch chiếu mẫu cho phim đầu tiên
                db.showTimesDao().insert(new ShowTimes(movies.get(0).getMovieId(), theaters.get(0).getTheaterId(), "19:00 - Hôm nay", 120000));
                db.showTimesDao().insert(new ShowTimes(movies.get(0).getMovieId(), theaters.get(1).getTheaterId(), "21:30 - Ngày mai", 95000));
                
                // Lịch chiếu cho phim thứ hai
                db.showTimesDao().insert(new ShowTimes(movies.get(1).getMovieId(), theaters.get(1).getTheaterId(), "18:00 - Cuối tuần", 110000));
            }
            Log.d("Database", "Khởi tạo dữ liệu mẫu hoàn tất.");
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        executeDataLoading();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}
