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
import com.example.shopping_app.data.entity.ShowTimes;
import com.example.shopping_app.data.entity.Theater;
import com.example.shopping_app.data.entity.User;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private AppDatabase db;
    private MovieAdapter movieAdapter;
    private final List<Movie> movieList = new ArrayList<>();
    private PrefManager pref;

    private TextView tvHomeGreeting;
    private TextView tvHomeSummary;
    private TextView tvHomeCartSummary;
    private TextView tvProductsHint;
    private MaterialButton btnHomePrimary;
    private MaterialButton btnHomeSecondary;
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

    private void setupDependencies() {
        db = AppDatabase.getInstance(this);
        pref = new PrefManager(this);
    }

    private void initViews() {
        tvHomeGreeting = findViewById(R.id.tv_home_greeting);
        tvHomeSummary = findViewById(R.id.tv_home_summary);
        tvHomeCartSummary = findViewById(R.id.tv_home_cart_summary);
        tvProductsHint = findViewById(R.id.tv_products_hint);
        btnHomePrimary = findViewById(R.id.btn_home_primary);
        btnHomeSecondary = findViewById(R.id.btn_home_secondary);
        layoutCategories = findViewById(R.id.layout_categories);

        tvProductsHint.setText(R.string.movies_loading);
        populateCategories();
    }

    private void setupRecyclerView() {
        RecyclerView rvMovies = findViewById(R.id.rv_products);
        rvMovies.setLayoutManager(new GridLayoutManager(this, 2));
        movieAdapter = new MovieAdapter(movieList, movie -> navigateToShowtimes(movie.getMovieId()));
        rvMovies.setAdapter(movieAdapter);
    }

    private void attachListeners() {
        ImageButton btnCart = findViewById(R.id.btn_toolbar_cart);
        btnCart.setOnClickListener(v -> navigateToMyTickets());

        btnHomePrimary.setOnClickListener(v -> handlePrimaryAction());
        btnHomeSecondary.setOnClickListener(v -> navigateToMyTickets());
    }

    private void populateCategories() {
        layoutCategories.removeAllViews();
        String[] categories = {
                getString(R.string.category_now_showing),
                getString(R.string.category_scifi),
                getString(R.string.category_drama),
                getString(R.string.category_family)
        };

        for (String category : categories) {
            TextView chip = new TextView(this);
            chip.setText(category);
            chip.setPadding(32, 18, 32, 18);
            chip.setTextColor(getColor(R.color.text_primary));
            chip.setBackgroundResource(R.drawable.bg_chip_unselected);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMarginEnd(16);
            chip.setLayoutParams(params);
            layoutCategories.addView(chip);
        }
    }

    private void executeDataLoading() {
        executorService.execute(() -> {
            try {
                seedData();
                loadMovies();
                updateUserInterface();
            } catch (Exception e) {
                Log.e(TAG, "Failed to load home data", e);
                runOnUiThread(() -> showToast(getString(R.string.error_load_home)));
            }
        });
    }

    private void handlePrimaryAction() {
        if (pref.isLoggedIn()) {
            pref.logout();
            showToast(getString(R.string.logout_success));
            updateUserInterface();
        } else {
            startActivity(new Intent(this, LoginActivity.class));
        }
    }

    private void navigateToShowtimes(int movieId) {
        Intent intent = new Intent(this, ShowtimesActivity.class);
        intent.putExtra(AppConstants.EXTRA_MOVIE_ID, movieId);
        startActivity(intent);
    }

    private void navigateToMyTickets() {
        if (pref.isLoggedIn()) {
            startActivity(new Intent(this, MyTicketsActivity.class));
            return;
        }

        showToast(getString(R.string.prompt_login_for_tickets));
        startActivity(new Intent(this, LoginActivity.class));
    }

    private void loadMovies() {
        List<Movie> movies = db.movieDao().getAllMovies();
        runOnUiThread(() -> {
            movieAdapter.updateData(movies);
            tvProductsHint.setText(getString(R.string.movies_count, movies.size()));
        });
    }

    private void updateUserInterface() {
        runOnUiThread(() -> {
            if (pref.isLoggedIn()) {
                tvHomeGreeting.setText(R.string.home_greeting_logged_in);
                tvHomeSummary.setText(R.string.home_summary_logged_in);
                btnHomePrimary.setText(R.string.action_logout);
                btnHomeSecondary.setText(R.string.action_my_tickets);
                tvHomeCartSummary.setVisibility(View.GONE);
            } else {
                tvHomeGreeting.setText(R.string.app_name);
                tvHomeSummary.setText(R.string.home_summary_guest);
                tvHomeCartSummary.setVisibility(View.VISIBLE);
                tvHomeCartSummary.setText(
                        getString(
                                R.string.demo_account_message,
                                AppConstants.DEMO_USERNAME,
                                AppConstants.DEMO_PASSWORD
                        )
                );
                btnHomePrimary.setText(R.string.action_login);
                btnHomeSecondary.setText(R.string.action_get_started);
            }
        });
    }

    private void seedData() {
        if (!db.movieDao().getAllMovies().isEmpty()) {
            return;
        }

        db.userDao().insert(new User(
                AppConstants.DEMO_USERNAME,
                AppConstants.DEMO_PASSWORD,
                "Quản trị viên",
                "admin@movieticket.com"
        ));

        db.movieDao().insert(new Movie(
                "Oppenheimer",
                "Kiệt tác về cha đẻ bom nguyên tử của Christopher Nolan",
                180,
                "Tiểu sử / Chính kịch",
                "https://images.unsplash.com/photo-1485846234645-a62644f84728?q=80&w=2059&auto=format&fit=crop"
        ));
        db.movieDao().insert(new Movie(
                "Interstellar",
                "Hành trình vĩ đại xuyên không gian để cứu lấy nhân loại",
                169,
                "Khoa học viễn tưởng",
                "https://images.unsplash.com/photo-1446776811953-b23d57bd21aa?q=80&w=2072&auto=format&fit=crop"
        ));
        db.movieDao().insert(new Movie(
                "Joker: Folie à Deux",
                "Sự điên rồ nhân đôi trong phần tiếp theo đầy ám ảnh",
                138,
                "Tội phạm / Nhạc kịch",
                "https://images.unsplash.com/photo-1531259683007-016a7b628fc3?q=80&w=1974&auto=format&fit=crop"
        ));
        db.movieDao().insert(new Movie(
                "Avatar: The Way of Water",
                "Trở lại hành tinh Pandora với những khung hình đại dương choáng ngợp",
                192,
                "Hành động / Phiêu lưu",
                "https://images.unsplash.com/photo-1598327105666-5b89351aff97?q=80&w=2070&auto=format&fit=crop"
        ));

        db.theaterDao().insert(new Theater("CGV Vincom Center", "72 Lê Thánh Tôn, Quận 1"));
        db.theaterDao().insert(new Theater("Lotte Cinema Cantavil", "Tầng 7 Cantavil Premier, Quận 2"));

        List<Movie> movies = db.movieDao().getAllMovies();
        List<Theater> theaters = db.theaterDao().getAllTheaters();

        if (movies.size() < 2 || theaters.size() < 2) {
            return;
        }

        db.showTimesDao().insert(new ShowTimes(movies.get(0).getMovieId(), theaters.get(0).getTheaterId(), "19:00 - Hôm nay", 120000));
        db.showTimesDao().insert(new ShowTimes(movies.get(0).getMovieId(), theaters.get(1).getTheaterId(), "21:30 - Ngày mai", 95000));
        db.showTimesDao().insert(new ShowTimes(movies.get(1).getMovieId(), theaters.get(1).getTheaterId(), "18:00 - Cuối tuần", 110000));
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
        executorService.shutdown();
    }
}
