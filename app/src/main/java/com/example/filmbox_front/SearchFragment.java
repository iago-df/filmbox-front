package com.example.filmbox_front;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchFragment extends Fragment {

    public SearchFragment() {
        super(R.layout.fragment_search);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        RecyclerView rv = view.findViewById(R.id.rvCategories);

        rv.setLayoutManager(new GridLayoutManager(getContext(), 2));
        CategoryAdapter adapter = new CategoryAdapter();
        rv.setAdapter(adapter);

        RetrofitClient.api().getCategories().enqueue(new Callback<List<Category>>() {
            @Override
            public void onResponse(@NonNull Call<List<Category>> call, @NonNull Response<List<Category>> response) {
                System.out.println("HTTP CODE = " + response.code());
                System.out.println("URL = " + call.request().url());

                if (response.body() != null) {
                    System.out.println("SIZE = " + response.body().size());
                    adapter.setItems(response.body());
                } else {
                    System.out.println("BODY NULL");
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Category>> call, @NonNull Throwable t) {
                System.out.println("FAIL URL = " + call.request().url());
                t.printStackTrace();
            }
        });

    }
}