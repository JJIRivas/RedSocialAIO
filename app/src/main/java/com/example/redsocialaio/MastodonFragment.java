package com.example.redsocialaio;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MastodonFragment extends Fragment {

    private RecyclerView recyclerView;
    private TootAdapter adapter;
    private ProgressBar progressBar;

    @SuppressLint("MissingInflatedId")
    @Nullable
    @Override
    //Carga el layout con la lista
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mastodon, container, false);
        //Referencias a las vistas y los elementos del layout
        progressBar = view.findViewById(R.id.progressBarLoading);
        recyclerView = view.findViewById(R.id.recyclerViewToots);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        //Mostrar ProgressBar y ocultar RecyclerView mientras se carga
        showLoading(true);


        fetchToots();

        return view;
    }

    private void showLoading(boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            progressBar.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }

    }


    //Llama a la API de publica de mastodon usando RetroFit para traer los posts
    private void fetchToots() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://mastodon.social/")  // Base URL de Mastodon pública
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        // Cuando llegan los datos, crea el adaptador y los pone en la lista
        MastodonApiService apiService = retrofit.create(MastodonApiService.class);
        progressBar.postDelayed(() -> showLoading(false), 2000); // Simula un retraso de 2 segundos para mostrar la ProgressBar

        Call<List<Toot>> call = apiService.getPublicTimeline();
        call.enqueue(new Callback<List<Toot>>() {
            @Override

            //Muestran mensajes de error si hay error de red o la API no funciona bien
            public void onResponse(Call<List<Toot>> call, Response<List<Toot>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Toot> toots = response.body();
                    adapter = new TootAdapter(toots);
                    recyclerView.setAdapter(adapter);
                } else {
                    Toast.makeText(getContext(), "Error al cargar los posts", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Toot>> call, Throwable t) {
                Toast.makeText(getContext(), "Error de red: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
