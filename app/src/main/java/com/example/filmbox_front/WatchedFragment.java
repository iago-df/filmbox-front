package com.example.filmbox_front;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WatchedFragment extends Fragment {

    private static final String PREFS_NAME = "FilmBoxPrefs";
    private static final String TOKEN_KEY = "SESSION_TOKEN";
    private static final String BASE_URL = "http://10.0.2.2:8000";

    private ApiService api;
    private String sessionToken = "";

    public WatchedFragment() {
        super(R.layout.fragment_watched);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            String fromArgs = getArguments().getString("SESSION_TOKEN", "");
            if (fromArgs != null && !fromArgs.isEmpty()) sessionToken = fromArgs;
        }
        if (sessionToken == null || sessionToken.isEmpty()) {
            SharedPreferences prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            sessionToken = prefs.getString(TOKEN_KEY, "");
        }

        api = RetrofitClient.getApiService();

        RecyclerView recyclerView = view.findViewById(R.id.watched_recycler);
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

        loadWatchedMovies(adapter);

        ImageView ivBack = view.findViewById(R.id.ivBack);
        ivBack.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());
    }

    private void loadWatchedMovies(MovieAdapter adapter) {
        if (sessionToken == null || sessionToken.isEmpty()) return;

        api.getWatched("Bearer " + sessionToken).enqueue(new Callback<List<FilmResponse>>() {
            @Override
            public void onResponse(Call<List<FilmResponse>> call, Response<List<FilmResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {

                    final List<String> urls = new ArrayList<>();
                    final List<Integer> ids = new ArrayList<>();

                    for (FilmResponse f : response.body()) {
                        if (f != null && f.image_url != null && !f.image_url.isEmpty()) {
                            urls.add(buildFullImageUrl(f.image_url));
                            ids.add(f.id);
                        }
                    }

                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> adapter.updateUrlsData(urls, ids));
                    } else {
                        adapter.updateUrlsData(urls, ids);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<FilmResponse>> call, Throwable t) {
                t.printStackTrace();
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
