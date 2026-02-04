package com.example.filmbox_front;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieViewHolder> {

    // --- Modos de datos ---
    private List<String> movieImages;              // legacy: solo URLs
    private List<String> movieUrls;                // nuevo: URLs
    private List<Integer> movieIds;                // nuevo: IDs correspondientes a URLs

    private boolean useUrlsMode = false;

    private final Context context;
    private final OnMovieClickListener listener;

    public interface OnMovieClickListener {
        void onMovieClick(int position);
    }

    // --- Constructor legacy (tu versión vieja) ---
    public MovieAdapter(Context context, List<String> movieImages, OnMovieClickListener listener) {
        this.context = context;
        this.movieImages = movieImages;
        this.listener = listener;
        this.useUrlsMode = false;
    }

    // --- Constructor nuevo (URLs + IDs) ---
    public MovieAdapter(Context context, List<String> movieUrls, List<Integer> movieIds, OnMovieClickListener listener) {
        this.context = context;
        this.movieUrls = movieUrls;
        this.movieIds = movieIds;
        this.listener = listener;
        this.useUrlsMode = true;
    }

    // --- Factory como en los cambios de tu compi (URLs-only compatible con IDs después) ---
    public static MovieAdapter createWithUrlsOnly(Context context, List<String> movieUrls, OnMovieClickListener listener) {
        return new MovieAdapter(context, movieUrls, new ArrayList<>(), listener);
    }

    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_movie, parent, false);

        // mismo adapter para GRID y carrusel horizontal
        RecyclerView.LayoutManager lm = null;
        if (parent instanceof RecyclerView) {
            lm = ((RecyclerView) parent).getLayoutManager();
        }

        ViewGroup.LayoutParams lp = view.getLayoutParams();
        if (lp instanceof RecyclerView.LayoutParams) {
            if (lm instanceof GridLayoutManager) {
                lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
            } else {
                lp.width = dp(120);
            }
            view.setLayoutParams(lp);
        }

        return new MovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieViewHolder holder, int position) {

        String imageUrl;
        int movieId = -1;

        if (useUrlsMode) {
            if (movieUrls == null || position < 0 || position >= movieUrls.size()) return;

            imageUrl = movieUrls.get(position);

            if (movieIds != null && position < movieIds.size()) {
                movieId = movieIds.get(position);
            }
        } else {
            if (movieImages == null || position < 0 || position >= movieImages.size()) return;
            imageUrl = movieImages.get(position);
        }

        if (imageUrl != null && !imageUrl.isEmpty()) {
            Picasso.get()
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .error(R.drawable.ic_launcher_foreground)
                    .fit()
                    .centerCrop()
                    .into(holder.imageView);
        } else {
            holder.imageView.setImageResource(R.drawable.ic_launcher_foreground);
        }

        final int finalMovieId = movieId;

        holder.itemView.setOnClickListener(v -> {
            // 1) Listener custom (si lo usas)
            if (listener != null) {
                listener.onMovieClick(position);
            }

            // 2) Navegar a detalles si hay ID
            if (finalMovieId != -1) {
                Intent intent = new Intent(context, FilmPageActivity.class);
                intent.putExtra("movie_id", finalMovieId);
                context.startActivity(intent);
            } else {
                Log.w("MovieAdapter", "No hay ID disponible para position=" + position);
            }
        });
    }

    @Override
    public int getItemCount() {
        if (useUrlsMode) {
            return movieUrls == null ? 0 : movieUrls.size();
        }
        return movieImages == null ? 0 : movieImages.size();
    }

    public static class MovieViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public MovieViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imgPosterGrid);
        }
    }

    // --- Nuevo update para URLs + IDs ---
    public void updateUrlsData(List<String> newUrls, List<Integer> newIds) {
        this.useUrlsMode = true;

        this.movieUrls = newUrls != null ? newUrls : new ArrayList<>();
        this.movieIds = newIds != null ? newIds : new ArrayList<>();

        notifyDataSetChanged();
    }

    // --- Mantener tu método viejo (legacy) para que no rompa otras pantallas ---
    public void updateData(List<String> newImages) {
        // si el adapter está en modo nuevo, mantenlo en modo nuevo y mete IDs vacíos
        if (useUrlsMode) {
            updateUrlsData(newImages, new ArrayList<>());
            return;
        }

        if (this.movieImages != null) this.movieImages.clear();
        if (newImages != null) this.movieImages.addAll(newImages);
        notifyDataSetChanged();
    }

    private int dp(int v) {
        return Math.round(v * context.getResources().getDisplayMetrics().density);
    }
}
