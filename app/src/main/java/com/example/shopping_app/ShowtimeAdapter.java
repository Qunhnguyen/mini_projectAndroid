package com.example.shopping_app;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shopping_app.data.AppDatabase;
import com.example.shopping_app.data.entity.ShowTimes;
import com.example.shopping_app.data.entity.Theater;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ShowtimeAdapter extends RecyclerView.Adapter<ShowtimeAdapter.ShowtimeViewHolder> {
    private final AppDatabase db;
    private final OnShowtimeClickListener listener;
    private final ExecutorService theaterExecutor = Executors.newCachedThreadPool();
    private List<ShowTimes> showTimesList;

    public interface OnShowtimeClickListener {
        void onShowtimeClick(ShowTimes showTime);
    }

    public ShowtimeAdapter(List<ShowTimes> showTimesList, AppDatabase db, OnShowtimeClickListener listener) {
        this.showTimesList = showTimesList;
        this.db = db;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ShowtimeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_showtime, parent, false);
        return new ShowtimeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ShowtimeViewHolder holder, int position) {
        ShowTimes showTime = showTimesList.get(position);

        holder.tvTheater.setText(R.string.label_loading_theater);
        holder.tvTime.setText(showTime.getDateTime());
        holder.tvPrice.setText(UiUtils.formatCurrency(showTime.getPrice()));
        holder.btnSelect.setOnClickListener(v -> listener.onShowtimeClick(showTime));

        theaterExecutor.execute(() -> {
            Theater theater = db.theaterDao().getTheaterById(showTime.getTheaterId());
            holder.tvTheater.post(() -> holder.tvTheater.setText(
                    theater != null ? theater.getName() : holder.itemView.getContext().getString(R.string.label_unknown_theater)
            ));
        });
    }

    @Override
    public int getItemCount() {
        return showTimesList != null ? showTimesList.size() : 0;
    }

    public void updateData(List<ShowTimes> newList) {
        this.showTimesList = newList == null ? new ArrayList<>() : newList;
        notifyDataSetChanged();
    }

    static class ShowtimeViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvTheater;
        private final TextView tvTime;
        private final TextView tvPrice;
        private final Button btnSelect;

        public ShowtimeViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTheater = itemView.findViewById(R.id.tv_show_theater);
            tvTime = itemView.findViewById(R.id.tv_show_time);
            tvPrice = itemView.findViewById(R.id.tv_show_price);
            btnSelect = itemView.findViewById(R.id.btn_select_showtime);
        }
    }
}
