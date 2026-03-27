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

        try {
            db = AppDatabase.getInstance(this);
            pref = new PrefManager(this);

            initViews();
            setupRecyclerView();
            setupToolbarButtons();
            setupHomeCardButtons();

            executorService.execute(() -> {
                try {
                    seedData();
                    loadMovies();
                    updateHomeCard();
                } catch (Exception e) {
                    Log.e("DB_ERROR", "Lỗi luồng nền: " + e.getMessage());
                }
            });
        } catch (Exception e) {
            Log.e("CRASH", "Lỗi onCreate: " + e.getMessage());
            Toast.makeText(this, "Có lỗi xảy ra khi khởi động!", Toast.LENGTH_LONG).show();
        }
    }

    private void initViews() {
        tvHomeGreeting = findViewById(R.id.tv_home_greeting);
        tvHomeSummary = findViewById(R.id.tv_home_summary);
        tvHomeCartSummary = findViewById(R.id.tv_home_cart_summary);
        tvProductsHint = findViewById(R.id.tv_products_hint);
        btnHomePrimary = findViewById(R.id.btn_home_primary);
        btnHomeSecondary = findViewById(R.id.btn_home_secondary);
        layoutCategories = findViewById(R.id.layout_categories);
        
        // Cập nhật tiêu đề text
        if (tvProductsHint != null) tvProductsHint.setText("Phim đang chiếu");
    }

    private void setupRecyclerView() {
        RecyclerView rvMovies = findViewById(R.id.rv_products);
        if (rvMovies != null) {
            rvMovies.setLayoutManager(new GridLayoutManager(this, 2));
            movieAdapter = new MovieAdapter(movieList, movie -> {
                // Luồng: Xem chi tiết phim -> Xem Showtimes
                Intent intent = new Intent(MainActivity.this, ShowtimesActivity.class);
                intent.putExtra("MOVIE_ID", movie.getMovieId());
                startActivity(intent);
            });
            rvMovies.setAdapter(movieAdapter);
        }
    }

    private void setupToolbarButtons() {
        ImageButton btnCart = findViewById(R.id.btn_toolbar_cart);
        if (btnCart != null) {
            btnCart.setOnClickListener(v -> {
                if (pref.isLoggedIn()) {
                    startActivity(new Intent(this, MyTicketsActivity.class));
                } else {
                    startActivity(new Intent(this, LoginActivity.class));
                }
            });
        }
    }

    private void setupHomeCardButtons() {
        btnHomePrimary.setOnClickListener(v -> {
            if (pref.isLoggedIn()) {
                pref.logout();
                Toast.makeText(this, "Đã đăng xuất!", Toast.LENGTH_SHORT).show();
                updateHomeCard();
            } else {
                startActivity(new Intent(this, LoginActivity.class));
            }
        });

        btnHomeSecondary.setOnClickListener(v -> {
            if (pref.isLoggedIn()) {
                startActivity(new Intent(this, MyTicketsActivity.class));
            } else {
                Toast.makeText(this, "Vui lòng đăng nhập!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, LoginActivity.class));
            }
        });
    }

    private void seedData() {
        if (db.movieDao().getAllMovies().isEmpty()) {
            db.userDao().insert(new User("admin", "123456", "Nguyen Van A", "admin@gmail.com"));

            db.movieDao().insert(new Movie("Oppenheimer", "Câu chuyện về cha đẻ bom nguyên tử", 180, "Tiểu sử / Chính kịch", "https://images.unsplash.com/photo-1485846234645-a62644f84728?q=80&w=2059&auto=format&fit=crop"));
            db.movieDao().insert(new Movie("Interstellar", "Hành trình xuyên không gian tìm hành tinh mới", 169, "Khoa học viễn tưởng", "https://images.unsplash.com/photo-1446776811953-b23d57bd21aa?q=80&w=2072&auto=format&fit=crop"));
            db.movieDao().insert(new Movie("Joker", "Nguồn gốc của kẻ thù Batman", 122, "Tội phạm / Chính kịch", "https://images.unsplash.com/photo-1531259683007-016a7b628fc3?q=80&w=1974&auto=format&fit=crop"));
            db.movieDao().insert(new Movie("Avatar 2", "Sự trỗi dậy của biển cả", 192, "Hành động / Phiêu lưu", "https://images.unsplash.com/photo-1598327105666-5b89351aff97?q=80&w=2070&auto=format&fit=crop"));

            db.theaterDao().insert(new Theater("CGV Vincom", "Tầng 5 Vincom Bà Triệu"));
            db.theaterDao().insert(new Theater("Lotte Cinema", "Tầng 3 Lotte Center"));

            List<Movie> movies = db.movieDao().getAllMovies();
            List<Theater> theaters = db.theaterDao().getAllTheaters();

            if (!movies.isEmpty() && !theaters.isEmpty()) {
                db.showTimesDao().insert(new ShowTimes(movies.get(0).getMovieId(), theaters.get(0).getTheaterId(), "20:00 - 20/05/2024", 95000));
                db.showTimesDao().insert(new ShowTimes(movies.get(0).getMovieId(), theaters.get(1).getTheaterId(), "21:30 - 20/05/2024", 85000));
                db.showTimesDao().insert(new ShowTimes(movies.get(1).getMovieId(), theaters.get(1).getTheaterId(), "18:30 - 20/05/2024", 85000));
            }
        }
    }

    private void loadMovies() {
        List<Movie> movies = db.movieDao().getAllMovies();
        runOnUiThread(() -> {
            if (movieAdapter != null) {
                movieAdapter.updateData(movies);
            }
            if (tvProductsHint != null) {
                tvProductsHint.setText(movies.size() + " phim đang chiếu");
            }
        });
    }

    private void updateHomeCard() {
        runOnUiThread(() -> {
            if (pref.isLoggedIn()) {
                tvHomeGreeting.setText("Xin chào!");
                tvHomeSummary.setText("Chào mừng bạn quay trở lại. Chọn phim để đặt vé ngay.");
                btnHomePrimary.setText("Đăng xuất");
                btnHomeSecondary.setText("Vé của tôi");
                tvHomeCartSummary.setVisibility(View.GONE);
            } else {
                tvHomeGreeting.setText("Movie Ticket App");
                tvHomeSummary.setText("Đăng nhập để đặt vé và xem lịch sử giao dịch.");
                tvHomeCartSummary.setVisibility(View.VISIBLE);
                tvHomeCartSummary.setText("Tài khoản mẫu: admin / 123456");
                btnHomePrimary.setText("Đăng nhập");
                btnHomeSecondary.setText("Xem phim");
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        executorService.execute(() -> {
            loadMovies();
            updateHomeCard();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}