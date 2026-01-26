package com.example.filmbox_front;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
public interface ApiService {
    @GET("/api/categories")
    Call<List<Category>> getCategories();
}
