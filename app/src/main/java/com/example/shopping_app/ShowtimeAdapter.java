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
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class ShowtimeAdapter extends RecyclerView.Adapter<ShowtimeAdapter.ShowtimeViewHolder> {

    private List<ShowTimes> showTimesList;
    private AppDatabase db;
    private OnShowtimeClickListener listener;

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
        
        // Load theater name in background or simple query if allowed
        new Thread(() -> {
            Theater theater = db.theaterDao().getTheaterById(showTime.getTheaterId());
            if (theater != null) {
                holder.tvTheater.post(() -> holder.tvTheater.setText(theater.getName()));
            }
        }).start();

        holder.tvTime.setText(showTime.getDateTime());
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        holder.tvPrice.setText(formatter.format(showTime.getPrice()));

        holder.btnSelect.setOnClickListener(v -> {
            if (listener != null) {
                listener.onShowtimeClick(showTime);
            }
        });
    }

    @Override
    public int getItemCount() {
        return showTimesList != null ? showTimesList.size() : 0;
    }

    static class ShowtimeViewHolder extends RecyclerView.ViewHolder {
        TextView tvTheater, tvTime, tvPrice;
        Button btnSelect;

        public ShowtimeViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTheater = itemView.findViewById(R.id.tv_show_theater);
            tvTime = itemView.findViewById(R.id.tv_show_time);
            tvPrice = itemView.findViewById(R.id.tv_show_price);
            btnSelect = itemView.findViewById(R.id.btn_select_showtime);
        }
    }
}