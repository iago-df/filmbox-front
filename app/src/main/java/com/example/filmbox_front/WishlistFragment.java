package com.example.filmbox_front;

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
import retrofit2.*;
import retrofit2.converter.gson.GsonConverterFactory;

public class WishlistFragment extends Fragment {

    private ApiService api;
    private String sessionToken = "";

    public WishlistFragment() {
        super(R.layout.fragment_wishlist); // Asegúrate de crear fragment_wishlist.xml con RecyclerView y header
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Obtener token
        if (getArguments() != null) {
            sessionToken = getArguments().getString("SESSION_TOKEN", "");
        }

        // Inicializar Retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:8000/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        api = retrofit.create(ApiService.class);

        // Configurar RecyclerView
        RecyclerView recyclerView = view.findViewById(R.id.wishlist_grid);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));

        loadWishlist(recyclerView);

        // Flecha de volver
        ImageView backArrow = view.findViewById(R.id.back_arrow);
        backArrow.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().popBackStack();
        });
    }

    private void loadWishlist(RecyclerView recyclerView) {
        api.getWishlist("Bearer " + sessionToken)
                .enqueue(new Callback<List<FilmResponse>>() {
                    @Override
                    public void onResponse(Call<List<FilmResponse>> call, Response<List<FilmResponse>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<String> urls = new ArrayList<>();
                            for (FilmResponse f : response.body()) {
                                urls.add(f.image_url);
                            }
                            recyclerView.setAdapter(new MovieAdapter(requireContext(), urls, pos -> {}));
                        }
                    }

                    @Override
                    public void onFailure(Call<List<FilmResponse>> call, Throwable t) {
                        t.printStackTrace();
                    }
                });
    }
}
