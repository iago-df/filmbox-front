package com.example.filmbox_front;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ReviewResponse {
    @SerializedName("movie_id")
    private int movieId;

    @SerializedName("total_reviews")
    private int totalReviews;

    @SerializedName("preview")
    private List<Review> preview;

    // Getters
    public int getMovieId() {
        return movieId;
    }

    public int getTotalReviews() {
        return totalReviews;
    }

    public List<Review> getPreview() {
        return preview;
    }

    // Setters
    public void setMovieId(int movieId) {
        this.movieId = movieId;
    }

    public void setTotalReviews(int totalReviews) {
        this.totalReviews = totalReviews;
    }

    public void setPreview(List<Review> preview) {
        this.preview = preview;
    }
}
