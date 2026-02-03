package com.example.filmbox_front;

import com.google.gson.annotations.SerializedName;

public class ReviewRequest {
    @SerializedName("rating")
    private float rating;

    @SerializedName("comment")
    private String comment;

    public ReviewRequest(float rating, String comment) {
        this.rating = rating;
        this.comment = comment;
    }

    // Getters
    public float getRating() {
        return rating;
    }

    public String getComment() {
        return comment;
    }

    // Setters
    public void setRating(float rating) {
        this.rating = rating;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
