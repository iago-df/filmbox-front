package com.example.filmbox_front;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieViewHolder> {

    private List<String> movieImages; // URLs de las portadas
    private final Context context;
    private final OnMovieClickListener listener;

    public interface OnMovieClickListener {
        void onMovieClick(int position);
    }

    public MovieAdapter(Context context, List<String> movieImages, OnMovieClickListener listener) {
        this.context = context;
        this.movieImages = movieImages;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_movie, parent, false);

        // 🔥 CLAVE: mismo adapter se usa en GRID y en carrusel horizontal.
        // Si el RecyclerView usa GridLayoutManager, el item debe ocupar el ancho de su columna.
        // Si no, dejamos el ancho fijo (120dp) para previews/carruseles.
        RecyclerView.LayoutManager lm = null;
        if (parent instanceof RecyclerView) {
            lm = ((RecyclerView) parent).getLayoutManager();
        }

        ViewGroup.LayoutParams lp = view.getLayoutParams();
        if (lp instanceof RecyclerView.LayoutParams) {
            if (lm instanceof GridLayoutManager) {
                lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
                // La altura la controla tu XML (180dp). Si quieres 170dp en grid, cámbialo en XML
                // o añade aquí una condición extra.
            } else {
                lp.width = dp(120);
            }
            view.setLayoutParams(lp);
        }

        return new MovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieViewHolder holder, int position) {
        if (movieImages == null || position < 0 || position >= movieImages.size()) return;

        String url = movieImages.get(position);

        if (url != null && !url.isEmpty()) {
            Picasso.get()
                    .load(url)
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .error(R.drawable.ic_launcher_foreground)
                    .fit()
                    .centerCrop()
                    .into(holder.imageView);
        } else {
            holder.imageView.setImageResource(R.drawable.ic_launcher_foreground);
        }

        if (listener != null) {
            holder.itemView.setOnClickListener(v -> listener.onMovieClick(position));
        }
    }

    @Override
    public int getItemCount() {
        return movieImages == null ? 0 : movieImages.size();
    }

    public static class MovieViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public MovieViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imgPosterGrid);
        }
    }

    public void updateData(List<String> newImages) {
        if (this.movieImages != null) this.movieImages.clear();
        if (newImages != null) this.movieImages.addAll(newImages);
        notifyDataSetChanged();
    }

    private int dp(int v) {
        return Math.round(v * context.getResources().getDisplayMetrics().density);
    }
}
