package com.example.redsocialaio.ui.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.redsocialaio.R;

import java.util.ArrayList;
import java.util.List;

/**
 * StatusAdapter - Adaptador para mostrar posts en RecyclerView
 * 
 * Responsabilidades:
 * - Gestionar la lista de posts
 * - Crear y reutilizar ViewHolders
 * - Vincular datos de posts con elementos de UI
 * - Manejar clicks en botones de interacción
 * - Cargar imágenes de avatar con Glide
 */
public class StatusAdapter extends RecyclerView.Adapter<StatusAdapter.StatusViewHolder> {
    // Variables del adaptador
    private List<Status> statusList = new ArrayList<>(); // Lista de posts
    private OnStatusInteractionListener listener; // Listener para interacciones



    @NonNull
    @Override
    public StatusViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_status, parent, false);
        return new StatusViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StatusViewHolder holder, int position) {
        Status status = statusList.get(position);
        holder.bind(status, listener);
    }

    @Override
    public int getItemCount() {
        return statusList.size();
    }

    public void setStatusList(List<Status> statuses) {
        this.statusList.clear();
        if (statuses != null) {
            this.statusList.addAll(statuses);
        }
        notifyDataSetChanged();
    }
    
    public void setOnStatusInteractionListener(OnStatusInteractionListener listener) {
        this.listener = listener;
    }

    static class StatusViewHolder extends RecyclerView.ViewHolder {
        private final TextView displayNameTextView;
        private final TextView contentTextView;
        private final ImageView avatarImageView;
        private final ImageView likeButton;
        private final ImageView reblogButton;
        private final ImageView followButton;
        private final TextView likeCount;
        private final TextView reblogCount;

        public StatusViewHolder(@NonNull View itemView) {
            super(itemView);
            displayNameTextView = itemView.findViewById(R.id.text_display_name);
            contentTextView = itemView.findViewById(R.id.text_content);
            avatarImageView = itemView.findViewById(R.id.image_avatar);
            likeButton = itemView.findViewById(R.id.btn_like);
            reblogButton = itemView.findViewById(R.id.btn_reblog);
            followButton = itemView.findViewById(R.id.btn_follow);
            likeCount = itemView.findViewById(R.id.text_like_count);
            reblogCount = itemView.findViewById(R.id.text_reblog_count);
        }

        public void bind(Status status, OnStatusInteractionListener listener) {
            // Configurar usuario
            if (status.getAccount() != null) {
                String displayName = status.getAccount().getDisplayName();
                displayNameTextView.setText(displayName != null && !displayName.isEmpty() ? displayName : "Usuario");
                
                // Cargar avatar de forma segura
                try {
                    if (status.getAccount().getAvatar() != null && !status.getAccount().getAvatar().isEmpty()) {
                        Glide.with(itemView.getContext())
                            .load(status.getAccount().getAvatar())
                            .placeholder(R.drawable.ic_launcher_background)
                            .error(R.drawable.ic_launcher_background)
                            .circleCrop()
                            .into(avatarImageView);
                    } else {
                        avatarImageView.setImageResource(R.drawable.ic_launcher_background);
                    }
                } catch (Exception e) {
                    avatarImageView.setImageResource(R.drawable.ic_launcher_background);
                }
            } else {
                displayNameTextView.setText("Usuario");
                avatarImageView.setImageResource(R.drawable.ic_launcher_background);
            }
            
            // Configurar contenido
            String content = status.getContent();
            if (content != null) {
                content = content.replaceAll("<.*?>", "").trim();
                contentTextView.setText(content.isEmpty() ? "[Sin contenido]" : content);
            } else {
                contentTextView.setText("[Sin contenido]");
            }
            
            // Configurar contadores de forma segura
            likeCount.setText(String.valueOf(Math.max(0, status.getFavouritesCount())));
            reblogCount.setText(String.valueOf(Math.max(0, status.getReblogsCount())));
            
            // Configurar colores de botones
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    likeButton.setColorFilter(status.isFavourited() ? 
                        itemView.getContext().getColor(android.R.color.holo_red_dark) : 
                        itemView.getContext().getColor(android.R.color.darker_gray));
                        
                    reblogButton.setColorFilter(status.isReblogged() ? 
                        itemView.getContext().getColor(android.R.color.holo_green_dark) : 
                        itemView.getContext().getColor(android.R.color.darker_gray));
                } else {
                    likeButton.setColorFilter(status.isFavourited() ? 0xFFFF0000 : 0xFF666666);
                    reblogButton.setColorFilter(status.isReblogged() ? 0xFF00FF00 : 0xFF666666);
                }
            } catch (Exception e) {
                // Ignorar errores de color
            }
            
            // Configurar listeners
            if (listener != null) {
                likeButton.setOnClickListener(v -> listener.onLikeClick(status));
                reblogButton.setOnClickListener(v -> listener.onReblogClick(status));
                if (status.getAccount() != null) {
                    followButton.setOnClickListener(v -> listener.onFollowClick(status.getAccount()));
                }
            }
        }
    }
    
    public interface OnStatusInteractionListener {
        void onLikeClick(Status status);
        void onReblogClick(Status status);
        void onFollowClick(Status.Account account);
    }
}
