package com.example.filmbox_front.models;

import com.google.gson.annotations.SerializedName;

public class Review {
    @SerializedName("author")
    private String author;

    @SerializedName("rating")
    private float rating;

    @SerializedName("comment")
    private String comment;

    @SerializedName("date")
    private String date;

    // Getters
    public String getAuthor() {
        return author;
    }

    public float getRating() {
        return rating;
    }

    public String getComment() {
        return comment;
    }

    public String getDate() {
        return date;
    }

    // Setters
    public void setAuthor(String author) {
        this.author = author;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
