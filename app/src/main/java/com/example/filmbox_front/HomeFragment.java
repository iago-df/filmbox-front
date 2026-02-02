package com.example.filmbox_front;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";
    private RecyclerView rvMain;
    private MainCategoryAdapter mainAdapter;

    public HomeFragment() {
        super(R.layout.fragment_home);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Vinculamos el RecyclerView principal (Vertical)
        rvMain = view.findViewById(R.id.rvMain);

        if (rvMain == null) {
            Log.e(TAG, "ERROR: rvMain es null! Verifica el ID en fragment_home.xml");
            return;
        }

        rvMain.setLayoutManager(new LinearLayoutManager(getContext()));
        Log.d(TAG, "RecyclerView configurado correctamente");

        // Iniciamos la carga
        cargarTodoDinamico();
    }

    private void cargarTodoDinamico() {
        Log.d(TAG, "Iniciando carga dinámica...");
        ApiService api = RetrofitClient.getApiService();

        // 1. Obtenemos las categorías de Django
        api.getCategories().enqueue(new Callback<List<Category>>() {
            @Override
            public void onResponse(Call<List<Category>> call, Response<List<Category>> catResponse) {
                Log.d(TAG, "Respuesta de categorías recibida. Código: " + catResponse.code());

                if (!isAdded()) {
                    Log.w(TAG, "Fragment no está añadido, cancelando operación");
                    return;
                }

                if (!catResponse.isSuccessful()) {
                    Log.e(TAG, "Respuesta no exitosa: " + catResponse.code());
                    Toast.makeText(getContext(), "Error al cargar categorías: " + catResponse.code(), Toast.LENGTH_SHORT).show();
                    return;
                }

                if (catResponse.body() == null) {
                    Log.e(TAG, "Body de categorías es null");
                    Toast.makeText(getContext(), "No se recibieron categorías", Toast.LENGTH_SHORT).show();
                    return;
                }

                List<Category> categorias = catResponse.body();
                Log.d(TAG, "Categorías recibidas: " + categorias.size());

                for (Category cat : categorias) {
                    Log.d(TAG, "Categoría: ID=" + cat.getId() + ", Título=" + cat.getTitle());
                }

                // 2. Cargamos las películas para cada categoría
                Map<Integer, List<Film>> mapaPelisPorCategoria = new HashMap<>();
                int[] categoriasPendientes = {categorias.size()};
                
                // Inicializamos el mapa
                for (Category cat : categorias) {
                    mapaPelisPorCategoria.put(cat.getId(), new ArrayList<>());
                }

                // 3. Para cada categoría, obtenemos sus películas
                for (Category cat : categorias) {
                    api.getMoviesByCategory(cat.getId()).enqueue(new Callback<List<Film>>() {
                        @Override
                        public void onResponse(Call<List<Film>> call, Response<List<Film>> filmResponse) {
                            if (!isAdded()) return;

                            if (filmResponse.isSuccessful() && filmResponse.body() != null) {
                                List<Film> peliculas = filmResponse.body();
                                mapaPelisPorCategoria.put(cat.getId(), peliculas);
                                Log.d(TAG, "Categoría '" + cat.getTitle() + "' tiene " + peliculas.size() + " películas");
                            } else {
                                Log.w(TAG, "Error cargando películas para categoría " + cat.getTitle() + ": " + filmResponse.code());
                            }

                            categoriasPendientes[0]--;
                            if (categoriasPendientes[0] == 0) {
                                // Todas las categorías han sido procesadas
                                configurarAdapter(categorias, mapaPelisPorCategoria);
                            }
                        }

                        @Override
                        public void onFailure(Call<List<Film>> call, Throwable t) {
                            Log.e(TAG, "Error cargando películas para categoría " + cat.getTitle(), t);
                            categoriasPendientes[0]--;
                            if (categoriasPendientes[0] == 0) {
                                configurarAdapter(categorias, mapaPelisPorCategoria);
                            }
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<List<Category>> call, Throwable t) {
                Log.e(TAG, "Error cargando categorías", t);
                if(isAdded()) {
                    Toast.makeText(getContext(), "Error de conexión: " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void configurarAdapter(List<Category> categorias, Map<Integer, List<Film>> mapaPelisPorCategoria) {
        // Filtrar categorías que no tienen películas
        List<Category> categoriasConContenido = new ArrayList<>();
        for (Category cat : categorias) {
            if (!mapaPelisPorCategoria.get(cat.getId()).isEmpty()) {
                categoriasConContenido.add(cat);
            }
        }

        Log.d(TAG, "Categorías con contenido: " + categoriasConContenido.size());

        if (categoriasConContenido.isEmpty()) {
            Log.w(TAG, "No hay categorías con películas para mostrar");
            Toast.makeText(getContext(), "No hay contenido para mostrar", Toast.LENGTH_SHORT).show();
            return;
        }

        // Configuramos el Adaptador Maestro
        ApiService api = RetrofitClient.getApiService();
        mainAdapter = new MainCategoryAdapter(categoriasConContenido, mapaPelisPorCategoria, getContext(), api);
        rvMain.setAdapter(mainAdapter);
        Log.d(TAG, "Adapter configurado con " + categoriasConContenido.size() + " categorías");
    }
}