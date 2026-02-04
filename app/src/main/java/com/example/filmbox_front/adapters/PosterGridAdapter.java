package com.example.filmbox_front.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.filmbox_front.R;
import com.example.filmbox_front.activities.FilmPageActivity;
import com.example.filmbox_front.models.FilmLite;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class PosterGridAdapter extends RecyclerView.Adapter<PosterGridAdapter.VH> {

    private final List<FilmLite> items = new ArrayList<>();
    private Context context;

    public PosterGridAdapter(Context context) {
        this.context = context;
    }

    public void setItems(List<FilmLite> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_movie_poster, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        FilmLite film = items.get(position);

        Picasso.get()
                .load(film.image_url)
                .placeholder(R.drawable.ic_launcher_foreground)
                .error(R.drawable.ic_launcher_foreground)
                .fit()
                .centerCrop()
                .into(h.poster);

        // Click listener para navegar a detalles de película
        h.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, FilmPageActivity.class);
            intent.putExtra("movie_id", film.id);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView poster;

        VH(@NonNull View itemView) {
            super(itemView);
            poster = itemView.findViewById(R.id.imgPosterGrid);
        }
    }
}

