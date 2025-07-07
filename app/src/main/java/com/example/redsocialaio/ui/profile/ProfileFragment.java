package com.example.redsocialaio.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.redsocialaio.MastodonViewModel;
import com.example.redsocialaio.R;
import com.example.redsocialaio.ui.home.Status;

/**
 * ProfileFragment - Muestra el perfil del usuario autenticado
 * 
 * Funciones:
 * - Muestra información personal del usuario
 * - Estadísticas: posts, seguidores, seguidos
 * - Avatar y datos básicos
 */
public class ProfileFragment extends Fragment {
    private MastodonViewModel mastodonViewModel;
    private ImageView avatarImageView;
    private TextView usernameTextView, displayNameTextView, bioTextView;
    private TextView postsCountTextView, followersCountTextView, followingCountTextView;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_profile, container, false);
        
        // Inicializar vistas
        avatarImageView = root.findViewById(R.id.profile_avatar);
        usernameTextView = root.findViewById(R.id.profile_username);
        displayNameTextView = root.findViewById(R.id.profile_display_name);
        bioTextView = root.findViewById(R.id.profile_bio);
        postsCountTextView = root.findViewById(R.id.profile_posts_count);
        followersCountTextView = root.findViewById(R.id.profile_followers_count);
        followingCountTextView = root.findViewById(R.id.profile_following_count);
        
        // Configurar ViewModel
        mastodonViewModel = new ViewModelProvider(this, new ViewModelProvider.AndroidViewModelFactory(requireActivity().getApplication())).get(MastodonViewModel.class);
        
        // Observar datos del usuario
        mastodonViewModel.getCurrentUser().observe(getViewLifecycleOwner(), this::updateProfile);
        
        // Cargar datos del usuario
        if (mastodonViewModel.isLoggedIn()) {
            mastodonViewModel.fetchCurrentUser();
        } else {
            Toast.makeText(getContext(), "Inicia sesión para ver tu perfil", Toast.LENGTH_SHORT).show();
        }
        
        return root;
    }
    
    /**
     * Actualiza la interfaz del perfil con los datos del usuario
     * @param account Datos del usuario obtenidos de la API
     */
    private void updateProfile(Status.Account account) {
        if (account != null) {
            // Cargar avatar usando Glide con transformación circular
            Glide.with(this)
                .load(account.getAvatar()) // URL del avatar desde la API
                .circleCrop() // Hacer la imagen circular
                .into(avatarImageView); // Mostrar en el ImageView
            
            // Actualizar información básica del usuario
            usernameTextView.setText("@" + account.getUsername()); // Nombre de usuario con @
            displayNameTextView.setText(account.getDisplayName()); // Nombre para mostrar
            
            // Mostrar biografía o mensaje por defecto si está vacía
            bioTextView.setText(account.getNote().isEmpty() ? "Sin biografía" : account.getNote());
            
            // Actualizar estadísticas del usuario
            postsCountTextView.setText(String.valueOf(account.getStatusesCount())); // Número de posts
            followersCountTextView.setText(String.valueOf(account.getFollowersCount())); // Seguidores
            followingCountTextView.setText(String.valueOf(account.getFollowingCount())); // Siguiendo
        }
    }
}