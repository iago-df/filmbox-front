package com.example.filmbox_front;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.squareup.picasso.Picasso;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FilmPageActivity extends AppCompatActivity {

    private ImageView ivBackdrop, ivPoster, ivMyAvatar;
    private TextView tvTitle, tvSubtitle, tvSynopsis, tvWriteReviewHint;
    private RatingBar ratingBar;
    private MaterialButton btnTrailer, btnActionVista, btnActionWatchlist, btnActionFavorites, btnAllReviews;
    
    private ApiService apiService;
    private String authToken;
    private int movieId;
    private Film currentFilm;
    private boolean isWatched, isInWishlist, isFavorite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_film_page);

        // Obtener movieId del Intent
        movieId = getIntent().getIntExtra("movie_id", -1);
        android.util.Log.d("FilmPageActivity", "movie_id recibido: " + movieId);
        if (movieId == -1) {
            android.util.Log.e("FilmPageActivity", "Error: movie_id no válido");
            Toast.makeText(this, "Error: ID de película no válido", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Inicializar Retrofit
        apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);
        
        // Obtener token de autenticación
        SharedPreferences prefs = getSharedPreferences("FilmBoxPrefs", MODE_PRIVATE);
        authToken = prefs.getString("auth_token", null);

        initViews();
        setupClickListeners();
        loadMovieDetails();
        loadMovieReviews();
    }

    @SuppressLint("WrongViewCast")
    private void initViews() {
        ivBackdrop = findViewById(R.id.ivBackdrop);
        ivPoster = findViewById(R.id.ivPoster);
        ivMyAvatar = findViewById(R.id.ivMyAvatar);
        
        tvTitle = findViewById(R.id.tvTitle);
        tvSubtitle = findViewById(R.id.tvSubtitle);
        tvSynopsis = findViewById(R.id.tvSynopsis);
        tvWriteReviewHint = findViewById(R.id.tvWriteReviewHint);
        
        ratingBar = findViewById(R.id.ratingBar);
        
        btnTrailer = findViewById(R.id.btnTrailer);
        btnActionVista = findViewById(R.id.btnActionVista);
        btnActionWatchlist = findViewById(R.id.btnActionWatchlist);
        btnActionFavorites = findViewById(R.id.btnActionFavorites);
        btnAllReviews = findViewById(R.id.btnAllReviews);
    }

    private void setupClickListeners() {
        btnTrailer.setOnClickListener(v -> {
            // TODO: Implementar trailer
            Toast.makeText(this, "Trailer próximamente", Toast.LENGTH_SHORT).show();
        });

        btnActionVista.setOnClickListener(v -> toggleWatched());
        btnActionWatchlist.setOnClickListener(v -> toggleWishlist());
        btnActionFavorites.setOnClickListener(v -> toggleFavorite());
        
        btnAllReviews.setOnClickListener(v -> {
            Intent intent = new Intent(this, AllReviewsActivity.class);
            intent.putExtra("movie_id", movieId);
            startActivity(intent);
        });

        tvWriteReviewHint.setOnClickListener(v -> {
            // TODO: Implementar diálogo para escribir review
            Toast.makeText(this, "Escribir review próximamente", Toast.LENGTH_SHORT).show();
        });
    }

    private void loadMovieDetails() {
        Call<Film> call = apiService.getMovieDetails(movieId);
        call.enqueue(new Callback<Film>() {
            @Override
            public void onResponse(Call<Film> call, Response<Film> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentFilm = response.body();
                    displayMovieDetails();
                    checkUserMovieStatus();
                } else {
                    Toast.makeText(FilmPageActivity.this, "Error al cargar detalles", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Film> call, Throwable t) {
                Toast.makeText(FilmPageActivity.this, "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayMovieDetails() {
        tvTitle.setText(currentFilm.getTitle());
        tvSynopsis.setText(currentFilm.getDescription());
        
        // Formatear subtítulo: año • dirigido por [categorías]
        String subtitle = currentFilm.getYear() + " • ";
        if (currentFilm.getCategories() != null && !currentFilm.getCategories().isEmpty()) {
            subtitle += "CATEGORÍAS: ";
            for (int i = 0; i < currentFilm.getCategories().size(); i++) {
                if (i > 0) subtitle += ", ";
                subtitle += currentFilm.getCategories().get(i).getTitle();
            }
        }
        tvSubtitle.setText(subtitle);

        // Cargar imágenes con Picasso
        if (currentFilm.getImageUrl() != null && !currentFilm.getImageUrl().isEmpty()) {
            Picasso.get()
                .load(currentFilm.getImageUrl())
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_gallery)
                .fit()
                .centerCrop()
                .into(ivPoster);
            
            Picasso.get()
                .load(currentFilm.getImageUrl())
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_gallery)
                .fit()
                .centerCrop()
                .into(ivBackdrop);
        }
    }

    private void loadMovieReviews() {
        Call<ReviewResponse> call = apiService.getMovieReviews(movieId, false);
        call.enqueue(new Callback<ReviewResponse>() {
            @Override
            public void onResponse(Call<ReviewResponse> call, Response<ReviewResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    displayReviews(response.body().getPreview());
                }
            }

            @Override
            public void onFailure(Call<ReviewResponse> call, Throwable t) {
                // Error silencioso para reviews
            }
        });
    }

    private void displayReviews(List<Review> reviews) {
        if (reviews == null || reviews.isEmpty()) return;

        // Mostrar primera review en el layout existente (reviewPaco)
        if (reviews.size() > 0) {
            Review review1 = reviews.get(0);
            TextView tvUsernamePaco = findViewById(R.id.tvUsernamePaco);
            TextView tvReviewTextPaco = findViewById(R.id.tvReviewTextPaco);
            RatingBar rbPaco = findViewById(R.id.rbPaco);
            TextView tvRatingNumPaco = findViewById(R.id.tvRatingNumPaco);

            tvUsernamePaco.setText(review1.getAuthor());
            tvReviewTextPaco.setText(review1.getComment());
            rbPaco.setRating(review1.getRating());
            tvRatingNumPaco.setText(String.valueOf(review1.getRating()));
        }

        // Mostrar segunda review si existe (reviewJose)
        if (reviews.size() > 1) {
            Review review2 = reviews.get(1);
            TextView tvUsernameJose = findViewById(R.id.tvUsernameJose);
            TextView tvReviewTextJose = findViewById(R.id.tvReviewTextJose);
            RatingBar rbJose = findViewById(R.id.rbJose);
            TextView tvRatingNumJose = findViewById(R.id.tvRatingNumJose);
            
            tvUsernameJose.setText(review2.getAuthor());
            tvReviewTextJose.setText(review2.getComment());
            rbJose.setRating(review2.getRating());
            tvRatingNumJose.setText(String.valueOf(review2.getRating()));
        }
    }

    private void checkUserMovieStatus() {
        if (authToken == null) return;

        // TODO: Implementar verificación de estado cuando el backend tenga los endpoints
        // Por ahora asumimos valores por defecto
        updateActionButtons();
    }

    private void updateActionButtons() {
        btnActionVista.setBackgroundColor(isWatched ? 
            getResources().getColor(android.R.color.holo_green_dark) : 
            getResources().getColor(android.R.color.darker_gray));
        
        btnActionWatchlist.setBackgroundColor(isInWishlist ? 
            getResources().getColor(android.R.color.holo_blue_dark) : 
            getResources().getColor(android.R.color.darker_gray));
        
        btnActionFavorites.setBackgroundColor(isFavorite ? 
            getResources().getColor(android.R.color.holo_red_dark) : 
            getResources().getColor(android.R.color.darker_gray));
    }

    private void toggleWatched() {
        if (authToken == null) {
            Toast.makeText(this, "Debes iniciar sesión", Toast.LENGTH_SHORT).show();
            return;
        }

        Call<Void> call;
        if (isWatched) {
            call = apiService.removeFromWatched(movieId);
        } else {
            call = apiService.markAsWatched(movieId);
        }

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    isWatched = !isWatched;
                    updateActionButtons();
                    Toast.makeText(FilmPageActivity.this, 
                        isWatched ? "Marcada como vista" : "Eliminada de vistas", 
                        Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(FilmPageActivity.this, "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void toggleWishlist() {
        if (authToken == null) {
            Toast.makeText(this, "Debes iniciar sesión", Toast.LENGTH_SHORT).show();
            return;
        }

        Call<Void> call;
        if (isInWishlist) {
            call = apiService.removeFromWishlist(movieId);
        } else {
            call = apiService.addToWishlist(movieId);
        }

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    isInWishlist = !isInWishlist;
                    updateActionButtons();
                    Toast.makeText(FilmPageActivity.this, 
                        isInWishlist ? "Añadida a wishlist" : "Eliminada de wishlist", 
                        Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(FilmPageActivity.this, "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void toggleFavorite() {
        if (authToken == null) {
            Toast.makeText(this, "Debes iniciar sesión", Toast.LENGTH_SHORT).show();
            return;
        }

        Call<Void> call;
        if (isFavorite) {
            call = apiService.removeFavorite(movieId);
        } else {
            call = apiService.addFavorite(movieId);
        }

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    isFavorite = !isFavorite;
                    updateActionButtons();
                    Toast.makeText(FilmPageActivity.this, 
                        isFavorite ? "Añadida a favoritos" : "Eliminada de favoritos", 
                        Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(FilmPageActivity.this, "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
