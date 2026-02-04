package com.example.filmbox_front.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.filmbox_front.R;
import com.example.filmbox_front.activities.LoginActivity;
import com.example.filmbox_front.adapters.MovieAdapter;
import com.example.filmbox_front.network.ApiService;
import com.example.filmbox_front.network.RetrofitClient;
import com.example.filmbox_front.responses.FilmResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileFragment extends Fragment {

    private static final String BASE_URL = "http://10.0.2.2:8000";

    private ApiService api;
    private String sessionToken = "";
    private String username = "Usuario";

    public ProfileFragment() {
        super(R.layout.fragment_profile);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Args
        if (getArguments() != null) {
            sessionToken = getArguments().getString("SESSION_TOKEN", "");
            username = getArguments().getString("USERNAME", "Usuario");
        }

        api = RetrofitClient.getApiService();

        // UI perfil
        ImageView profileImage = view.findViewById(R.id.profile_image);
        profileImage.setImageResource(R.drawable.ic_profile_image);

        TextView usernameText = view.findViewById(R.id.username_text);
        usernameText.setText(username);

        // Headers clickable
        View watchedHeader = view.findViewById(R.id.watched_header);
        View favoritesHeader = view.findViewById(R.id.favorites_header);
        View wishlistHeader = view.findViewById(R.id.wishlist_header);

        watchedHeader.setOnClickListener(v -> openWatched());
        favoritesHeader.setOnClickListener(v -> openFavorites());
        wishlistHeader.setOnClickListener(v -> openWishlist());

        // Cargar previews solo si hay token
        if (sessionToken != null && !sessionToken.isEmpty()) {
            loadWatchedMovies(view);
            loadFavoriteMovies(view);
            loadWishlistMovies(view);
        }

        view.findViewById(R.id.logout_button).setOnClickListener(v -> {
            SharedPreferences prefs = requireContext().getSharedPreferences("FilmBoxPrefs", Context.MODE_PRIVATE);
            prefs.edit().remove("SESSION_TOKEN").remove("USERNAME").apply();

            Intent i = new Intent(requireContext(), LoginActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
        });
    }

    // -------- Navegación --------

    private void openWatched() {
        WatchedFragment fragment = new WatchedFragment();
        Bundle args = new Bundle();
        args.putString("SESSION_TOKEN", sessionToken);
        fragment.setArguments(args);

        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    private void openFavorites() {
        FavoritesFragment fragment = new FavoritesFragment();
        Bundle args = new Bundle();
        args.putString("SESSION_TOKEN", sessionToken);
        fragment.setArguments(args);

        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    private void openWishlist() {
        WishlistFragment fragment = new WishlistFragment();
        Bundle args = new Bundle();
        args.putString("SESSION_TOKEN", sessionToken);
        fragment.setArguments(args);

        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    // -------- Setup Recycler horizontal con separación --------

    private void setupHorizontalPreviewRecycler(RecyclerView rv) {
        rv.setHasFixedSize(true);
        rv.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        while (rv.getItemDecorationCount() > 0) {
            rv.removeItemDecorationAt(0);
        }

        final int space = dp(10);

        rv.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View v,
                                       @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                int pos = parent.getChildAdapterPosition(v);
                if (pos == RecyclerView.NO_POSITION) return;

                outRect.left = (pos == 0) ? 0 : space;
                outRect.right = 0;
                outRect.top = 0;
                outRect.bottom = 0;
            }
        });
    }

    // -------- Cargar datos --------

    private void loadWatchedMovies(View root) {
        api.getWatched("Bearer " + sessionToken).enqueue(new Callback<List<FilmResponse>>() {
            @Override
            public void onResponse(Call<List<FilmResponse>> call, Response<List<FilmResponse>> response) {
                if (!isAdded() || getContext() == null) return;
                if (response.isSuccessful() && response.body() != null) {

                    List<String> urls = new ArrayList<>();
                    List<Integer> ids = new ArrayList<>();

                    for (FilmResponse f : response.body()) {
                        if (f != null && f.image_url != null && !f.image_url.isEmpty()) {
                            urls.add(buildFullImageUrl(f.image_url));
                            ids.add(f.id);
                        }
                    }

                    RecyclerView rv = root.findViewById(R.id.watched_recycler);
                    setupHorizontalPreviewRecycler(rv);

                    MovieAdapter adapter = MovieAdapter.createWithUrlsOnly(getContext(), urls, position -> {});
                    adapter.updateUrlsData(urls, ids);
                    rv.setAdapter(adapter);
                }
            }

            @Override
            public void onFailure(Call<List<FilmResponse>> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    private void loadFavoriteMovies(View root) {
        api.getFavoritesAuth("Bearer " + sessionToken).enqueue(new Callback<List<FilmResponse>>() {
            @Override
            public void onResponse(Call<List<FilmResponse>> call, Response<List<FilmResponse>> response) {
                if (!isAdded() || getContext() == null) return;
                if (response.isSuccessful() && response.body() != null) {

                    List<String> urls = new ArrayList<>();
                    List<Integer> ids = new ArrayList<>();

                    for (FilmResponse f : response.body()) {
                        if (f != null && f.image_url != null && !f.image_url.isEmpty()) {
                            urls.add(buildFullImageUrl(f.image_url));
                            ids.add(f.id);
                        }
                    }

                    RecyclerView rv = root.findViewById(R.id.favorites_recycler);
                    setupHorizontalPreviewRecycler(rv);

                    MovieAdapter adapter = MovieAdapter.createWithUrlsOnly(getContext(), urls, position -> {});
                    adapter.updateUrlsData(urls, ids);
                    rv.setAdapter(adapter);
                }
            }

            @Override
            public void onFailure(Call<List<FilmResponse>> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    private void loadWishlistMovies(View root) {
        api.getWishlist("Bearer " + sessionToken).enqueue(new Callback<List<FilmResponse>>() {
            @Override
            public void onResponse(Call<List<FilmResponse>> call, Response<List<FilmResponse>> response) {
                if (!isAdded() || getContext() == null) return;
                if (response.isSuccessful() && response.body() != null) {

                    List<String> urls = new ArrayList<>();
                    List<Integer> ids = new ArrayList<>();

                    for (FilmResponse f : response.body()) {
                        if (f != null && f.image_url != null && !f.image_url.isEmpty()) {
                            urls.add(buildFullImageUrl(f.image_url));
                            ids.add(f.id);
                        }
                    }

                    RecyclerView rv = root.findViewById(R.id.wishlist_recycler);
                    setupHorizontalPreviewRecycler(rv);

                    MovieAdapter adapter = MovieAdapter.createWithUrlsOnly(getContext(), urls, position -> {});
                    adapter.updateUrlsData(urls, ids);
                    rv.setAdapter(adapter);
                }
            }

            @Override
            public void onFailure(Call<List<FilmResponse>> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    // -------- Utils --------

    private int dp(int v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }

    private String buildFullImageUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) return imageUrl;
        if (imageUrl.startsWith("http://") || imageUrl.startsWith("https://")) return imageUrl;
        String path = imageUrl.startsWith("/") ? imageUrl : "/" + imageUrl;
        return BASE_URL + path;
    }
}
