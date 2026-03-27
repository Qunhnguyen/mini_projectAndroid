package com.example.shopping_app;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
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
    private RecyclerView rvTickets;
    private TicketAdapter adapter;
    private AppDatabase db;
    private PrefManager pref;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_tickets);

        db = AppDatabase.getInstance(this);
        pref = new PrefManager(this);

        rvTickets = findViewById(R.id.rv_my_tickets);
        ImageButton btnBack = findViewById(R.id.btn_tickets_back);

        btnBack.setOnClickListener(v -> finish());

        rvTickets.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TicketAdapter(new ArrayList<>(), db);
        rvTickets.setAdapter(adapter);

        loadTickets();
    }

    private void loadTickets() {
        executor.execute(() -> {
            List<Tickets> list = db.ticketsDao().getTicketsByUser(pref.getUserId());
            runOnUiThread(() -> {
                adapter.updateData(list);
            });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}