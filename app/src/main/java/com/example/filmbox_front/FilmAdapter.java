package com.example.filmbox_front;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.squareup.picasso.Picasso;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FilmAdapter extends RecyclerView.Adapter<FilmAdapter.FilmViewHolder> {
    private List<Film> mList;
    private Context context;

    private ApiService apiService;

    public FilmAdapter(List<Film> mList, Context context, ApiService apiService) {
        this.mList = mList;
        this.context = context;
        this.apiService = apiService;
    }

    @NonNull
    @Override
    public FilmViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_film, parent, false);
        return new FilmViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FilmViewHolder holder, int position) {
        Film film = mList.get(position);

        holder.tvMovieTitle.setText(film.getTitle());
        holder.tvMovieYear.setText(String.valueOf(film.getYear()));

        Picasso.get()
                .load(film.getImageUrl())
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_gallery)
                .fit()
                .centerCrop()
                .into(holder.imgFilm);

        // Cargar estado inicial de favoritos
        cargarEstadoFavorite(film, holder);

        // Click listener para navegar a detalles de película
        holder.itemView.setOnClickListener(v -> {
            android.util.Log.d("FilmAdapter", "Click en película: " + film.getTitle() + " (ID: " + film.getId() + ")");
            Intent intent = new Intent(context, FilmPageActivity.class);
            intent.putExtra("movie_id", film.getId());
            android.util.Log.d("FilmAdapter", "Iniciando FilmPageActivity con movie_id: " + film.getId());
            context.startActivity(intent);
        });

        holder.imgFavorite.setOnClickListener(v -> {
            int movieId = film.getId();
            boolean newState = !film.isFavorite();

            // 1. Cambio visual instantáneo (Optimismo)
            film.setFavorite(newState);
            holder.imgFavorite.setImageResource(newState ?
                    android.R.drawable.btn_star_big_on :
                    android.R.drawable.btn_star_big_off);

            // 2. Llamada real a Django según vuestro urls.py
            // La ruta es: favorites/<int:movie_id>
            Call<Void> call;
            if (newState) {
                // Llama al método PUT de vuestra FavoriteFilmView
                call = apiService.addFavorite(movieId, "Bearer " + getAuthToken());
            } else {
                // Llama al método DELETE de vuestra FavoriteFilmView
                call = apiService.removeFavorite(movieId, "Bearer " + getAuthToken());
            }

            call.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (!response.isSuccessful()) {
                        // Si el servidor falla (ej. error 403), revertimos el cambio
                        film.setFavorite(!newState);
                        holder.imgFavorite.setImageResource(!newState ?
                                android.R.drawable.btn_star_big_on :
                                android.R.drawable.btn_star_big_off);
                        
                        if (response.code() == 401 || response.code() == 403) {
                            Toast.makeText(context, "Error de autenticación", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(context, "Error al actualizar favoritos", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // Éxito: mostrar mensaje breve
                        Toast.makeText(context, newState ? "Añadido a favoritos" : "Eliminado de favoritos", 
                                     Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    // Si no hay internet, revertimos el cambio
                    film.setFavorite(!newState);
                    holder.imgFavorite.setImageResource(!newState ?
                            android.R.drawable.btn_star_big_on :
                            android.R.drawable.btn_star_big_off);
                    Toast.makeText(context, "Error de conexión", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void cargarEstadoFavorite(Film film, FilmViewHolder holder) {
        // Usar la lista de favoritos completa para verificar si es favorita
        String authToken = getAuthToken();
        if (authToken == null) {
            // Si no hay token, asumimos que no es favorita
            film.setFavorite(false);
            holder.imgFavorite.setImageResource(android.R.drawable.btn_star_big_off);
            return;
        }

        apiService.getFavoritesAuth("Bearer " + authToken).enqueue(new Callback<List<FilmResponse>>() {
            @Override
            public void onResponse(Call<List<FilmResponse>> call, Response<List<FilmResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<FilmResponse> favorites = response.body();
                    boolean esFavorita = favorites.stream().anyMatch(f -> f.id == film.getId());
                    film.setFavorite(esFavorita);
                    
                    // Actualizar el estado visual
                    holder.imgFavorite.setImageResource(esFavorita ?
                            android.R.drawable.btn_star_big_on :
                            android.R.drawable.btn_star_big_off);
                } else {
                    // Si hay error, asumimos que no es favorita
                    film.setFavorite(false);
                    holder.imgFavorite.setImageResource(android.R.drawable.btn_star_big_off);
                }
            }

            @Override
            public void onFailure(Call<List<FilmResponse>> call, Throwable t) {
                // Si falla la carga, asumimos que no es favorita
                film.setFavorite(false);
                holder.imgFavorite.setImageResource(android.R.drawable.btn_star_big_off);
            }
        });
    }

    private String getAuthToken() {
        SharedPreferences prefs = context.getSharedPreferences("FilmBoxPrefs", Context.MODE_PRIVATE);
        return prefs.getString("SESSION_TOKEN", null);
    }

    @Override
    public int getItemCount() {
        return mList != null ? mList.size() : 0;
    }

    public static class FilmViewHolder extends RecyclerView.ViewHolder {
        TextView tvMovieTitle, tvMovieYear;
        ImageView imgFilm, imgFavorite;

        public FilmViewHolder(@NonNull View itemView) {
            super(itemView);
            imgFavorite = itemView.findViewById(R.id.imgFavorite);
            tvMovieTitle = itemView.findViewById(R.id.tvMovieTitle);
            tvMovieYear = itemView.findViewById(R.id.tvMovieYear);
            imgFilm = itemView.findViewById(R.id.imgFilm);
        }
    }
}