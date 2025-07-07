package com.example.redsocialaio.ui.home;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.redsocialaio.MastodonViewModel;
import com.example.redsocialaio.R;

/**
 * HomeFragment - Fragmento principal que muestra el timeline de posts
 * 
 * Funciones:
 * - Muestra lista de posts en un RecyclerView
 * - Maneja interacciones: like, reblog, follow
 * - Observa cambios en el timeline desde el ViewModel
 * - Valida autenticación antes de permitir acciones
 */
public class HomeFragment extends Fragment implements StatusAdapter.OnStatusInteractionListener {
    // Componentes principales
    private MastodonViewModel mastodonViewModel; // ViewModel para manejar datos
    private StatusAdapter statusAdapter; // Adaptador para el RecyclerView

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        
        // Configurar RecyclerView
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) RecyclerView recyclerView = root.findViewById(R.id.recycler_home);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        statusAdapter = new StatusAdapter();
        statusAdapter.setOnStatusInteractionListener(this);
        recyclerView.setAdapter(statusAdapter);
        
        // Configurar FAB de actualizar
        com.google.android.material.floatingactionbutton.FloatingActionButton fabRefresh = root.findViewById(R.id.fab_refresh);
        fabRefresh.setOnClickListener(v -> {
            // Animación de rotación
            v.animate().rotation(360f).setDuration(500).start();
            fabRefresh.setEnabled(false);
            
            mastodonViewModel.fetchTimeline();
            
            // Reactivar FAB después de 1.5 segundos
            v.postDelayed(() -> {
                fabRefresh.setEnabled(true);
                v.setRotation(0f); // Reset rotación
            }, 1500);
        });

        // Configurar ViewModel
        mastodonViewModel = new ViewModelProvider(this, new ViewModelProvider.AndroidViewModelFactory(requireActivity().getApplication())).get(MastodonViewModel.class);
        mastodonViewModel.getTimeline().observe(getViewLifecycleOwner(), statuses -> {
            android.util.Log.d("HomeFragment", "Timeline updated. Posts: " + (statuses != null ? statuses.size() : "null"));
            if (statuses != null) {
                statusAdapter.setStatusList(statuses);
                // Scroll al top cuando se actualiza
                if (!statuses.isEmpty()) {
                    recyclerView.smoothScrollToPosition(0);
                }
                Toast.makeText(getContext(), "Timeline actualizado", Toast.LENGTH_SHORT).show();
            } else {
                android.util.Log.e("HomeFragment", "Statuses is null!");
            }
        });
        
        // Cargar timeline inicial
        mastodonViewModel.fetchTimeline();
        return root;
    }
    
    @Override
    public void onLikeClick(Status status) {
        if (mastodonViewModel.isLoggedIn()) {
            mastodonViewModel.toggleFavourite(status.getId(), status.isFavourited());
            status.setFavourited(!status.isFavourited());
            status.setFavouritesCount(status.getFavouritesCount() + (status.isFavourited() ? 1 : -1));
            statusAdapter.notifyDataSetChanged();
        } else {
            Toast.makeText(getContext(), "Inicia sesión para dar like", Toast.LENGTH_SHORT).show();
        }
    }
    
    @Override
    public void onReblogClick(Status status) {
        if (mastodonViewModel.isLoggedIn()) {
            mastodonViewModel.reblogStatus(status.getId());
            Toast.makeText(getContext(), "Reblog enviado", Toast.LENGTH_SHORT).show();
            // Actualizar timeline después del reblog
            mastodonViewModel.fetchTimeline();
        } else {
            Toast.makeText(getContext(), "Inicia sesión para reblog", Toast.LENGTH_SHORT).show();
        }
    }
    
    @Override
    public void onFollowClick(Status.Account account) {
        if (mastodonViewModel.isLoggedIn()) {
            mastodonViewModel.followAccount(account.getId());
            Toast.makeText(getContext(), "Siguiendo a " + account.getDisplayName(), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), "Inicia sesión para seguir", Toast.LENGTH_SHORT).show();
        }
    }
}

