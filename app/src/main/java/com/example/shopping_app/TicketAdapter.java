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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TicketAdapter extends RecyclerView.Adapter<TicketAdapter.TicketViewHolder> {
    private final AppDatabase db;
    private final ExecutorService ticketExecutor = Executors.newCachedThreadPool();
    private List<Tickets> ticketList;

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

        holder.tvMovieTitle.setText(R.string.label_loading_movie);
        holder.tvTheater.setText(R.string.label_loading_theater);
        holder.tvTime.setText(ticket.getBookingDate());
        holder.tvSeat.setText(ticket.getSeatNumber());
        holder.tvPrice.setText(UiUtils.formatCurrency(ticket.getTotalPrice()));

        ticketExecutor.execute(() -> {
            ShowTimes showTime = db.showTimesDao().getShowTimeById(ticket.getShowTimeId());
            if (showTime == null) {
                return;
            }

            Movie movie = db.movieDao().getMovieById(showTime.getMovieId());
            Theater theater = db.theaterDao().getTheaterById(showTime.getTheaterId());

            holder.itemView.post(() -> {
                holder.tvMovieTitle.setText(movie != null
                        ? movie.getTitle()
                        : holder.itemView.getContext().getString(R.string.label_unknown_movie));
                holder.tvTheater.setText(theater != null
                        ? theater.getName()
                        : holder.itemView.getContext().getString(R.string.label_unknown_theater));
                holder.tvTime.setText(showTime.getDateTime());
            });
        });
    }

    @Override
    public int getItemCount() {
        return ticketList != null ? ticketList.size() : 0;
    }

    public void updateData(List<Tickets> newList) {
        this.ticketList = newList == null ? new ArrayList<>() : newList;
        notifyDataSetChanged();
    }

    static class TicketViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvMovieTitle;
        private final TextView tvTheater;
        private final TextView tvTime;
        private final TextView tvSeat;
        private final TextView tvPrice;

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
