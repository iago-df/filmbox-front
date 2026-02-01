package com.example.filmbox_front;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface ApiService {

    // Endpoints de autenticación
    @POST("register")
    Call<RegisterResponse> registerUser(@Body UserRegistration user);

    @POST("users/login")
    Call<LoginResponse> loginUser(@Body LoginRequest loginRequest);

    // Endpoints de películas y categorías
    @GET("movies")
    Call<List<Film>> getFilms();

    @GET("categories")
    Call<List<Category>> getCategories();

    @GET("categories/{category_id}/movies")
    Call<List<Film>> getMoviesByCategory(@Path("category_id") int categoryId);

    // Endpoints de listas de usuario (con autenticación)
    @GET("watched")
    Call<List<FilmResponse>> getWatched(@Header("Authorization") String authHeader);

    @GET("favorites")
    Call<List<Film>> getFavorites();

    @GET("favorites")
    Call<List<FilmResponse>> getFavoritesAuth(@Header("Authorization") String authHeader);

    @PUT("favorites/{movie_id}")
    Call<Void> addFavorite(@Path("movie_id") int movieId);

    @DELETE("favorites/{movie_id}")
    Call<Void> removeFavorite(@Path("movie_id") int movieId);

    @GET("wishlist")
    Call<List<FilmResponse>> getWishlist(@Header("Authorization") String authHeader);
}