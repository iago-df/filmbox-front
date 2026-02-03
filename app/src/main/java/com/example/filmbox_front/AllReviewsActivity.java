package com.example.filmbox_front;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AllReviewsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ReviewAdapter reviewAdapter;
    private ApiService apiService;
    private int movieId;

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
        
        initViews();
        loadAllReviews();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerViewReviews);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void loadAllReviews() {
        Call<ReviewResponse> call = apiService.getMovieReviews(movieId, true);
        call.enqueue(new Callback<ReviewResponse>() {
            @Override
            public void onResponse(Call<ReviewResponse> call, Response<ReviewResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Review> reviews = response.body().getPreview();
                    reviewAdapter = new ReviewAdapter(reviews);
                    recyclerView.setAdapter(reviewAdapter);
                } else {
                    Toast.makeText(AllReviewsActivity.this, "Error al cargar reviews", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ReviewResponse> call, Throwable t) {
                Toast.makeText(AllReviewsActivity.this, "Error de conexión", Toast.LENGTH_SHORT).show();
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
