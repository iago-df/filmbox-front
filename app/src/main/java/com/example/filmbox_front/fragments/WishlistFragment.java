package com.example.filmbox_front.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.filmbox_front.R;
import com.example.filmbox_front.adapters.MovieAdapter;
import com.example.filmbox_front.network.ApiService;
import com.example.filmbox_front.network.RetrofitClient;
import com.example.filmbox_front.responses.FilmResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WishlistFragment extends Fragment {

    private ApiService api;
    private String sessionToken = "";

    private static final String PREFS_NAME = "FilmBoxPrefs";
    private static final String TOKEN_KEY = "SESSION_TOKEN";
    private static final String BASE_URL = "http://10.0.2.2:8000";

    public WishlistFragment() {
        super(R.layout.fragment_wishlist);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1) Token: args -> prefs
        if (getArguments() != null) {
            String fromArgs = getArguments().getString("SESSION_TOKEN", "");
            if (fromArgs != null && !fromArgs.isEmpty()) sessionToken = fromArgs;
        }
        if (sessionToken == null || sessionToken.isEmpty()) {
            SharedPreferences prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            sessionToken = prefs.getString(TOKEN_KEY, "");
        }

        Log.d("WishlistFragment", "Token presente: " + (sessionToken != null && !sessionToken.isEmpty()));

        // 2) API
        api = RetrofitClient.getApiService();

        // 3) RecyclerView grid
        RecyclerView recyclerView = view.findViewById(R.id.wishlist_recycler);
        recyclerView.setHasFixedSize(true);

        final int spanCount = 3;
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), spanCount));

        while (recyclerView.getItemDecorationCount() > 0) {
            recyclerView.removeItemDecorationAt(0);
        }

        final int spacing = dp(8);
        recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View v,
                                       @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                int position = parent.getChildAdapterPosition(v);
                if (position == RecyclerView.NO_POSITION) return;

                int column = position % spanCount;

                outRect.left = spacing - column * spacing / spanCount;
                outRect.right = (column + 1) * spacing / spanCount;

                if (position < spanCount) outRect.top = spacing;
                outRect.bottom = spacing;
            }
        });

        MovieAdapter adapter = MovieAdapter.createWithUrlsOnly(requireContext(), new ArrayList<>(), pos -> {});
        recyclerView.setAdapter(adapter);

        loadWishlist(adapter);

        // 4) Flecha volver
        ImageView ivBack = view.findViewById(R.id.ivBack);
        ivBack.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());
    }

    private void loadWishlist(MovieAdapter adapter) {
        if (sessionToken == null || sessionToken.isEmpty()) {
            Log.e("WishlistFragment", "Token vacío, no se puede cargar wishlist");
            return;
        }

        api.getWishlist("Bearer " + sessionToken).enqueue(new Callback<List<FilmResponse>>() {
            @Override
            public void onResponse(Call<List<FilmResponse>> call, Response<List<FilmResponse>> response) {
                Log.d("WishlistFragment", "Respuesta: code=" + response.code() + ", body!=null=" + (response.body() != null));

                if (response.isSuccessful() && response.body() != null) {

                    List<FilmResponse> body = response.body();
                    final List<String> urls = new ArrayList<>();
                    final List<Integer> ids = new ArrayList<>();

                    for (FilmResponse film : body) {
                        if (film != null && film.image_url != null && !film.image_url.isEmpty()) {
                            urls.add(buildFullImageUrl(film.image_url));
                            ids.add(film.id);
                        }
                    }

                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> adapter.updateUrlsData(urls, ids));
                    } else {
                        adapter.updateUrlsData(urls, ids);
                    }

                } else {
                    Log.e("WishlistFragment", "Error wishlist: " + response.code() + " " + response.message());
                }
            }

            @Override
            public void onFailure(Call<List<FilmResponse>> call, Throwable t) {
                Log.e("WishlistFragment", "Fallo red: " + t.getMessage(), t);
            }
        });
    }

    private int dp(int v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }

    private static String buildFullImageUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) return imageUrl;
        if (imageUrl.startsWith("http://") || imageUrl.startsWith("https://")) return imageUrl;
        String path = imageUrl.startsWith("/") ? imageUrl : "/" + imageUrl;
        return BASE_URL + path;
    }
}
