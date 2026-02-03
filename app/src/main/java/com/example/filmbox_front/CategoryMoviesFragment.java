package com.example.filmbox_front;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

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

public class CategoryMoviesFragment extends Fragment {

    private static final String ARG_CAT_ID = "cat_id";
    private static final String ARG_CAT_TITLE = "cat_title";

    public static CategoryMoviesFragment newInstance(int categoryId, String categoryTitle) {
        CategoryMoviesFragment f = new CategoryMoviesFragment();
        Bundle b = new Bundle();
        b.putInt(ARG_CAT_ID, categoryId);
        b.putString(ARG_CAT_TITLE, categoryTitle);
        f.setArguments(b);
        return f;
    }

    public CategoryMoviesFragment() {
        super(R.layout.fragment_category_movies);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        int categoryId = requireArguments().getInt(ARG_CAT_ID);
        String categoryTitle = requireArguments().getString(ARG_CAT_TITLE, "");

        ImageView ivBack = view.findViewById(R.id.ivBack);
        TextView tvTitle = view.findViewById(R.id.tvTitle);
        RecyclerView rv = view.findViewById(R.id.rvCategoryMovies);

        tvTitle.setText(categoryTitle);
        ivBack.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new SearchFragment())
                    .commit();
        });

        rv.setLayoutManager(new GridLayoutManager(getContext(), 3));
        PosterGridAdapter adapter = new PosterGridAdapter(requireContext());
        rv.setAdapter(adapter);

        RetrofitClient.api().categoryMovies(categoryId).enqueue(new Callback<List<FilmLite>>() {
            @Override
            public void onResponse(@NonNull Call<List<FilmLite>> call, @NonNull Response<List<FilmLite>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    adapter.setItems(response.body());
                } else {
                    adapter.setItems(new ArrayList<>());
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<FilmLite>> call, @NonNull Throwable t) {
                t.printStackTrace();
                adapter.setItems(new ArrayList<>());
            }
        });
    }
}
