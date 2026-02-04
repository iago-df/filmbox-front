package com.example.filmbox_front.adapters;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.filmbox_front.R;
import com.example.filmbox_front.activities.FilmPageActivity;
import com.example.filmbox_front.models.FilmLite;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class MovieResultAdapter extends RecyclerView.Adapter<MovieResultAdapter.VH> {

    private final List<FilmLite> items = new ArrayList<>();

    public void setItems(List<FilmLite> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_movie_result, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        FilmLite m = items.get(position);
        h.title.setText(m.title);
        h.meta.setText(m.year + " • " + m.duration + " min");

        Picasso.get()
                .load(m.image_url)
                .placeholder(R.drawable.ic_launcher_foreground)
                .error(R.drawable.ic_launcher_foreground)
                .fit()
                .centerCrop()
                .into(h.poster);

        // Click listener para navegar a detalles de película
        h.itemView.setOnClickListener(v -> {
            Log.d("MovieResultAdapter", "Click en película: " + m.title + " (ID: " + m.id + ")");
            Intent intent = new Intent(h.itemView.getContext(), FilmPageActivity.class);
            intent.putExtra("movie_id", m.id);
            Log.d("MovieResultAdapter", "Iniciando FilmPageActivity con movie_id: " + m.id);
            h.itemView.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView poster;
        TextView title, meta;

        VH(@NonNull View itemView) {
            super(itemView);
            poster = itemView.findViewById(R.id.imgPoster);
            title = itemView.findViewById(R.id.tvTitle);
            meta = itemView.findViewById(R.id.tvMeta);
        }
    }
}
