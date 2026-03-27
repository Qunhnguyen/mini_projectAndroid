package com.example.shopping_app;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shopping_app.data.AppDatabase;
import com.example.shopping_app.data.entity.Tickets;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MyTicketsActivity extends AppCompatActivity {
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private RecyclerView rvTickets;
    private TextView tvEmptyState;
    private TicketAdapter adapter;
    private AppDatabase db;
    private PrefManager pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_tickets);

        db = AppDatabase.getInstance(this);
        pref = new PrefManager(this);

        if (!pref.isLoggedIn()) {
            Toast.makeText(this, R.string.prompt_login_for_tickets, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        rvTickets = findViewById(R.id.rv_my_tickets);
        tvEmptyState = findViewById(R.id.tv_tickets_empty);
        ImageButton btnBack = findViewById(R.id.btn_tickets_back);

        btnBack.setOnClickListener(v -> finish());

        rvTickets.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TicketAdapter(new ArrayList<>(), db);
        rvTickets.setAdapter(adapter);

        loadTickets();
    }

    private void loadTickets() {
        executor.execute(() -> {
            List<Tickets> tickets = db.ticketsDao().getTicketsByUser(pref.getUserId());
            runOnUiThread(() -> bindTickets(tickets));
        });
    }

    private void bindTickets(List<Tickets> tickets) {
        adapter.updateData(tickets);
        boolean hasTickets = !tickets.isEmpty();
        rvTickets.setVisibility(hasTickets ? View.VISIBLE : View.GONE);
        tvEmptyState.setVisibility(hasTickets ? View.GONE : View.VISIBLE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (pref != null && pref.isLoggedIn()) {
            loadTickets();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
