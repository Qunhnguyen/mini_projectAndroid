package com.example.shopping_app;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.shopping_app.data.AppDatabase;
import com.example.shopping_app.data.entity.Movie;
import com.example.shopping_app.data.entity.ShowTimes;
import com.example.shopping_app.data.entity.Theater;
import com.example.shopping_app.data.entity.Tickets;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class TicketAdapter extends RecyclerView.Adapter<TicketAdapter.TicketViewHolder> {

    private List<Tickets> ticketList;
    private AppDatabase db;

    public TicketAdapter(List<Tickets> ticketList, AppDatabase db) {
        this.ticketList = ticketList;
        this.db = db;
    }

    @NonNull
    @Override
    public TicketViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ticket, parent, false);
        return new TicketViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TicketViewHolder holder, int position) {
        Tickets ticket = ticketList.get(position);
        
        holder.tvSeat.setText(ticket.getSeatNumber());
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        holder.tvPrice.setText(formatter.format(ticket.getTotalPrice()));

        new Thread(() -> {
            ShowTimes st = db.showTimesDao().getShowTimeById(ticket.getShowTimeId());
            if (st != null) {
                Movie movie = db.movieDao().getMovieById(st.getMovieId());
                Theater theater = db.theaterDao().getTheaterById(st.getTheaterId());
                
                holder.itemView.post(() -> {
                    if (movie != null) holder.tvMovieTitle.setText(movie.getTitle());
                    if (theater != null) holder.tvTheater.setText(theater.getName());
                    holder.tvTime.setText(st.getDateTime());
                });
            }
        }).start();
    }

    @Override
    public int getItemCount() {
        return ticketList != null ? ticketList.size() : 0;
    }

    public void updateData(List<Tickets> newList) {
        this.ticketList = newList;
        notifyDataSetChanged();
    }

    static class TicketViewHolder extends RecyclerView.ViewHolder {
        TextView tvMovieTitle, tvTheater, tvTime, tvSeat, tvPrice;

        public TicketViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMovieTitle = itemView.findViewById(R.id.tv_ticket_movie_title);
            tvTheater = itemView.findViewById(R.id.tv_ticket_theater);
            tvTime = itemView.findViewById(R.id.tv_ticket_time);
            tvSeat = itemView.findViewById(R.id.tv_ticket_seat);
            tvPrice = itemView.findViewById(R.id.tv_ticket_price);
        }
    }
}