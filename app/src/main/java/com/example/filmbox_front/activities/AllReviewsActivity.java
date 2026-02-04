package com.example.filmbox_front.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.filmbox_front.R;
import com.example.filmbox_front.models.Review;
import com.example.filmbox_front.network.ApiService;
import com.example.filmbox_front.network.RetrofitClient;
import com.example.filmbox_front.requests.ReviewRequest;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.squareup.picasso.Picasso;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AllReviewsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ReviewAdapter reviewAdapter;
    private ApiService apiService;
    private BottomNavigationView bottomNav;
    private TextView tvNoReviews;
    private RatingBar ratingBarNew;
    private EditText etReviewText;
    private Button btnSubmitReview;
    private int movieId;
    private String authToken;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_reviews);

        movieId = getIntent().getIntExtra("movie_id", -1);
        if (movieId == -1) {
            Toast.makeText(this, "Error: ID de película no válido", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);
        
        // Obtener token y username
        SharedPreferences prefs = getSharedPreferences("FilmBoxPrefs", MODE_PRIVATE);
        authToken = prefs.getString("SESSION_TOKEN", null);
        username = prefs.getString("USERNAME", "Usuario");
        
        initViews();
        setupBottomNavigation();
        setupReviewForm();
        loadAllReviews();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerViewReviews);
        tvNoReviews = findViewById(R.id.tvNoReviews);
        bottomNav = findViewById(R.id.bottom_nav);
        ratingBarNew = findViewById(R.id.ratingBarNew);
        etReviewText = findViewById(R.id.etReviewText);
        btnSubmitReview = findViewById(R.id.btnSubmitReview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setupBottomNavigation() {
        bottomNav.setItemIconTintList(null);
        bottomNav.setItemActiveIndicatorEnabled(false);
        bottomNav.setItemRippleColor(ColorStateList.valueOf(Color.TRANSPARENT));

        getWindow().setNavigationBarColor(Color.WHITE);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        );

        bottomNav.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_home) {
                Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
                return true;

            } else if (item.getItemId() == R.id.nav_search) {
                Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("navigate_to", "search");
                startActivity(intent);
                finish();
                return true;

            } else if (item.getItemId() == R.id.nav_profile) {
                Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("navigate_to", "profile");
                intent.putExtra("SESSION_TOKEN", authToken);
                intent.putExtra("USERNAME", username);
                startActivity(intent);
                finish();
                return true;
            }
            return false;
        });
    }

    private void setupReviewForm() {
        // Ocultar formulario si no hay token de autenticación
        if (authToken == null) {
            findViewById(R.id.cardCreateReview).setVisibility(View.GONE);
            return;
        }

        btnSubmitReview.setOnClickListener(v -> {
            float rating = ratingBarNew.getRating();
            String comment = etReviewText.getText().toString().trim();

            // Validar campos
            if (comment.isEmpty()) {
                Toast.makeText(this, "Debes escribir un comentario", Toast.LENGTH_SHORT).show();
                return;
            }

            if (rating < 1) {
                Toast.makeText(this, "Debes dar una calificación", Toast.LENGTH_SHORT).show();
                return;
            }

            // Deshabilitar botón para evitar múltiples envíos
            btnSubmitReview.setEnabled(false);
            btnSubmitReview.setText("Enviando...");

            // Crear y enviar review
            ReviewRequest reviewRequest = new ReviewRequest(rating, comment);
            submitReview(reviewRequest);
        });
    }

    private void submitReview(ReviewRequest reviewRequest) {
        // Validar que tengamos token de autenticación
        if (authToken == null || authToken.isEmpty()) {
            Toast.makeText(this, "Debes iniciar sesión para enviar reviews", Toast.LENGTH_SHORT).show();
            btnSubmitReview.setEnabled(true);
            btnSubmitReview.setText("Enviar Review");
            return;
        }

        Call<Review> call = apiService.submitReview(movieId, reviewRequest, "Bearer " + authToken);
        call.enqueue(new Callback<Review>() {
            @Override
            public void onResponse(Call<Review> call, Response<Review> response) {
                // Restaurar botón
                btnSubmitReview.setEnabled(true);
                btnSubmitReview.setText("Enviar Review");

                if (response.isSuccessful()) {
                    Toast.makeText(AllReviewsActivity.this, "Review enviada correctamente", Toast.LENGTH_SHORT).show();
                    
                    // Limpiar formulario
                    ratingBarNew.setRating(3);
                    etReviewText.setText("");
                    
                    // Recargar reviews para mostrar la nueva
                    loadAllReviews();
                } else {
                    String errorMsg = "Error al enviar review";
                    if (response.code() == 400) {
                        errorMsg = "Datos inválidos";
                    } else if (response.code() == 401) {
                        errorMsg = "Debes iniciar sesión";
                    } else if (response.code() == 404) {
                        errorMsg = "Película no encontrada";
                    }
                    Toast.makeText(AllReviewsActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Review> call, Throwable t) {
                // Restaurar botón
                btnSubmitReview.setEnabled(true);
                btnSubmitReview.setText("Enviar Review");
                
                Toast.makeText(AllReviewsActivity.this, "Error de conexión: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void loadAllReviews() {
        // Mostrar indicador de carga
        tvNoReviews.setText("Cargando reviews...");
        tvNoReviews.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        
        Call<List<Review>> call = apiService.getAllMovieReviews(movieId, true);
        call.enqueue(new Callback<List<Review>>() {
            @Override
            public void onResponse(Call<List<Review>> call, Response<List<Review>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Review> reviews = response.body();
                    
                    if (reviews == null || reviews.isEmpty()) {
                        // Mostrar mensaje de no hay reviews
                        recyclerView.setVisibility(View.GONE);
                        tvNoReviews.setText("No hay reviews para esta película");
                        tvNoReviews.setVisibility(View.VISIBLE);
                    } else {
                        // Mostrar reviews
                        recyclerView.setVisibility(View.VISIBLE);
                        tvNoReviews.setVisibility(View.GONE);
                        reviewAdapter = new ReviewAdapter(reviews);
                        recyclerView.setAdapter(reviewAdapter);
                    }
                } else {
                    // Error específico según el código
                    String errorMsg = "Error al cargar reviews";
                    if (response.code() == 404) {
                        errorMsg = "Película no encontrada";
                    } else if (response.code() == 500) {
                        errorMsg = "Error del servidor";
                    }
                    Toast.makeText(AllReviewsActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                    
                    // Mostrar mensaje de error
                    recyclerView.setVisibility(View.GONE);
                    tvNoReviews.setText("Error: " + errorMsg);
                    tvNoReviews.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<List<Review>> call, Throwable t) {
                String errorMsg = "Error de conexión";
                if (t.getMessage() != null) {
                    errorMsg = "Error de conexión: " + t.getMessage();
                }
                Toast.makeText(AllReviewsActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                
                // Mostrar mensaje de error
                recyclerView.setVisibility(View.GONE);
                tvNoReviews.setText("No se pudieron cargar las reviews. Intenta de nuevo.");
                tvNoReviews.setVisibility(View.VISIBLE);
            }
        });
    }

    private static class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {
        
        private List<Review> reviews;

        public ReviewAdapter(List<Review> reviews) {
            this.reviews = reviews;
        }

        @NonNull
        @Override
        public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_review, parent, false);
            return new ReviewViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
            Review review = reviews.get(position);
            holder.bind(review);
        }

        @Override
        public int getItemCount() {
            return reviews != null ? reviews.size() : 0;
        }

        static class ReviewViewHolder extends RecyclerView.ViewHolder {
            private ImageView ivAvatar;
            private TextView tvUsername, tvReviewText, tvRatingNum, tvDate;
            private RatingBar ratingBar;

            public ReviewViewHolder(@NonNull View itemView) {
                super(itemView);
                ivAvatar = itemView.findViewById(R.id.ivAvatar);
                tvUsername = itemView.findViewById(R.id.tvUsername);
                tvReviewText = itemView.findViewById(R.id.tvReviewText);
                tvRatingNum = itemView.findViewById(R.id.tvRatingNum);
                tvDate = itemView.findViewById(R.id.tvDate);
                ratingBar = itemView.findViewById(R.id.ratingBar);
            }

            public void bind(Review review) {
                tvUsername.setText(review.getAuthor());
                tvReviewText.setText(review.getComment());
                tvRatingNum.setText(String.valueOf(review.getRating()));
                tvDate.setText(review.getDate());
                ratingBar.setRating(review.getRating());

                // Cargar avatar por defecto
                Picasso.get()
                    .load(android.R.drawable.sym_def_app_icon)
                    .into(ivAvatar);
            }
        }
    }
}
