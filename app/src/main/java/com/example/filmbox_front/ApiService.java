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
import retrofit2.http.Query;

public interface ApiService {

    // Auth
    @POST("register")
    Call<RegisterResponse> registerUser(@Body UserRegistration user);

    @POST("users/login")
    Call<LoginResponse> loginUser(@Body LoginRequest loginRequest);


    // Categorias
    @GET("categories")
    Call<List<Category>> getCategories();


    // Peliculas
    @GET("movies")
    Call<List<Film>> getFilms();

    @GET("movies")
    Call<List<FilmLite>> searchMovies(@Query("query") String query);

    @GET("categories/{category_id}/movies")
    Call<List<Film>> getMoviesByCategory(@Path("category_id") int categoryId);

    @GET("categories/{category_id}/movies")
    Call<List<FilmLite>> categoryMovies(@Path("category_id") int categoryId);


    // Busqueda de usuarios
    @GET("users")
    Call<List<UserLite>> searchUsers(@Query("query") String query);


    // Endpoints de listas de usuarios
    @GET("watched")
    Call<List<FilmResponse>> getWatched(@Header("Authorization") String authHeader);

    @GET("wishlist")
    Call<List<FilmResponse>> getWishlist(@Header("Authorization") String authHeader);

    @GET("favorites")
    Call<List<Film>> getFavorites();

    @GET("favorites")
    Call<List<FilmResponse>> getFavoritesAuth(@Header("Authorization") String authHeader);

    @PUT("favorites/{movie_id}")
    Call<Void> addFavorite(@Path("movie_id") int movieId);

    @DELETE("favorites/{movie_id}")
    Call<Void> removeFavorite(@Path("movie_id") int movieId);
}
