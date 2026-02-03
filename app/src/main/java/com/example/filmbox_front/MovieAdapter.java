package com.example.filmbox_front;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieViewHolder> {

    private List<FilmLite> movies; // Películas completas con IDs
    private List<String> movieUrls; // Solo URLs (para compatibilidad)
    private List<Integer> movieIds; // IDs correspondientes a las URLs
    private Context context;
    private OnMovieClickListener listener;
    private boolean useFilmLite = false; // Flag para saber qué modo usar

    public interface OnMovieClickListener {
        void onMovieClick(int position);
    }

    // Constructor para FilmLite (modo preferido)
    public MovieAdapter(Context context, List<FilmLite> movies, OnMovieClickListener listener) {
        this.context = context;
        this.movies = movies;
        this.listener = listener;
        this.useFilmLite = true;
    }

    // Constructor para URLs con IDs (compatibilidad)
    public MovieAdapter(Context context, List<String> movieUrls, List<Integer> movieIds, OnMovieClickListener listener) {
        this.context = context;
        this.movieUrls = movieUrls;
        this.movieIds = movieIds;
        this.listener = listener;
        this.useFilmLite = false;
    }

    // Constructor para solo URLs (legacy - no puede navegar a detalles)
    public MovieAdapter(Context context, List<String> movieUrls, OnMovieClickListener listener) {
        this(context, movieUrls, new ArrayList<>(), listener);
    }

    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_movie, parent, false);
        return new MovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieViewHolder holder, int position) {
        String imageUrl;
        int movieId = -1;

        if (useFilmLite) {
            // Modo FilmLite
            FilmLite movie = movies.get(position);
            imageUrl = movie.image_url;
            movieId = movie.id;
        } else {
            // Modo URLs
            if (position < movieUrls.size()) {
                imageUrl = movieUrls.get(position);
                if (position < movieIds.size()) {
                    movieId = movieIds.get(position);
                }
            } else {
                imageUrl = null;
            }
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
        
        // Click listener para navegar a detalles de película
        holder.itemView.setOnClickListener(v -> {
            // Primero ejecutar el listener personalizado si existe
            if (listener != null) {
                listener.onMovieClick(position);
            }
            
            // Luego navegar a detalles si tenemos un ID válido
            if (movieId != -1) {
                Intent intent = new Intent(context, FilmPageActivity.class);
                intent.putExtra("movie_id", movieId);
                context.startActivity(intent);
            } else {
                Log.w("MovieAdapter", "No hay ID disponible para la película en posición " + position);
            }
        });
    }

    @Override
    public int getItemCount() {
        if (useFilmLite) {
            return movies != null ? movies.size() : 0;
        } else {
            return movieUrls != null ? movieUrls.size() : 0;
        }
    }

    public static class MovieViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public MovieViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imgPosterGrid);
        }
    }

    // Update methods para ambos modos
    public void updateData(List<FilmLite> newMovies) {
        this.movies = newMovies;
        this.useFilmLite = true;
        notifyDataSetChanged();
    }

    public void updateData(List<String> newUrls, List<Integer> newIds) {
        this.movieUrls = newUrls;
        this.movieIds = newIds != null ? newIds : new ArrayList<>();
        this.useFilmLite = false;
        notifyDataSetChanged();
    }

    // Legacy method - solo URLs
    public void updateData(List<String> newUrls) {
        updateData(newUrls, new ArrayList<>());
    }
}



