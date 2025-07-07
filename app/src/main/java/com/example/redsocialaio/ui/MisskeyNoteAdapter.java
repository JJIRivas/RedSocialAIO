package com.example.redsocialaio.ui;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.redsocialaio.R;
import com.example.redsocialaio.misskey.core.notes.MisskeyNoteInteract;
import com.example.redsocialaio.misskey.core.notes.MisskeyNoteTimeline;
import com.example.redsocialaio.misskey.core.notes.MisskeyFile;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MisskeyNoteAdapter extends RecyclerView.Adapter<MisskeyNoteAdapter.NoteViewHolder> {
    private static final String TAG = "MisskeyNoteAdapter";

    public interface OnNoteInteractionListener {
        void onNoteUpdated(MisskeyNoteTimeline note, int position);

        void onRenoteDone(MisskeyNoteTimeline newNote);
    }

    private List<MisskeyNoteTimeline> notes = new ArrayList<>();
    private final Context context;
    private final MisskeyNoteInteract noteInteract;
    private final SimpleDateFormat dateFormat;

    public MisskeyNoteAdapter(Context context) {
        this.context = context;
        this.noteInteract = new MisskeyNoteInteract(context);
        this.dateFormat = new SimpleDateFormat("dd/MM HH:mm", Locale.getDefault());
    }

    private OnNoteInteractionListener listener;

    public void setInteractionListener(OnNoteInteractionListener listener) {
        this.listener = listener;
    }

    public void setNotes(List<MisskeyNoteTimeline> notes) {
        this.notes = notes;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_post, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        MisskeyNoteTimeline note = notes.get(position);

        // Texto del post
        holder.postText.setText(note.getText());

        // Información del usuario
        if (note.getUser() != null) {
            holder.postUser.setText("@" + note.getUser().getUserName());

            // Cargar avatar si tienes el ImageView con ID
            if (holder.userAvatar != null && note.getUser().getAvatarURL() != null) {
                Glide.with(context)
                        .load(note.getUser().getAvatarURL())
                        .circleCrop()
                        .placeholder(R.drawable.ic_profile)
                        .into(holder.userAvatar);
            }
        }

        // Instancia
        if (note.getUser() != null && note.getUser().getInstanceUrl() != null) {
            holder.postInstance.setText(note.getUser().getInstanceUrl());
        } else {
            holder.postInstance.setText("");
        }

        // Fecha
        holder.postDate.setText(dateFormat.format(note.getCreatedAt()));

        // Imagen del post
        if (note.hasFiles() && !note.getFiles().isEmpty()) {
            holder.postImage.setVisibility(View.VISIBLE);
            MisskeyFile firstImage = note.getFiles().get(0);

            Glide.with(context)
                    .load(firstImage.getUrl())
                    .placeholder(R.drawable.ic_image)
                    .into(holder.postImage);
        } else {
            holder.postImage.setVisibility(View.GONE);
        }

        // Configurar listeners para los botones
        setupInteractionButtons(holder, note, position);
    }

    private void setupInteractionButtons(NoteViewHolder holder, MisskeyNoteTimeline note, int position) {
        // Like button
        if (holder.likeButton != null) {
            // Cambiar color si ya reaccioné
            if (note.hasReacted()) {
                holder.likeButton.setColorFilter(context.getColor(android.R.color.holo_red_dark));
            } else {
                holder.likeButton.clearColorFilter();
            }

            holder.likeButton.setOnClickListener(v -> {
                if (note.hasReacted()) {
                    handleRemoveReaction(note, position, holder);
                } else {
                    handleAddReaction(note, position, holder);
                }
            });
        }

        // Repost button
        if (holder.repostButton != null) {
            if (note.isRenoted()) {
                holder.repostButton.setColorFilter(context.getColor(android.R.color.holo_green_dark));
            } else {
                holder.repostButton.clearColorFilter();
            }

            holder.repostButton.setOnClickListener(v -> {
                if (!note.isRenoted()) {
                    handleRenote(note, position, holder);
                }
            });
        }
    }

    private void handleAddReaction(MisskeyNoteTimeline note, int position, NoteViewHolder holder) {
        Log.d(TAG, "Añadiendo reacción a nota: " + note.getId());

        noteInteract.likePost(note.getId())
                .thenAccept(success -> {
                    if (success) {
                        ((MissUI) context).runOnUiThread(() -> {
                            holder.likeButton.setColorFilter(context.getColor(android.R.color.holo_red_dark));
                            Toast.makeText(context, "¡Like añadido!", Toast.LENGTH_SHORT).show();
                            if (listener != null) {
                                listener.onNoteUpdated(note, position);
                            }
                        });
                    }
                })
                .exceptionally(e -> {
                    Log.e(TAG, "Error al añadir reacción", e);
                    ((MissUI) context).runOnUiThread(() -> {
                        Toast.makeText(context, "Error al dar like", Toast.LENGTH_SHORT).show();
                    });
                    return null;
                });
    }

    private void handleRemoveReaction(MisskeyNoteTimeline note, int position, NoteViewHolder holder) {
        Log.d(TAG, "Eliminando reacción de nota: " + note.getId());

        noteInteract.removeReaction(note.getId())
                .thenAccept(success -> {
                    if (success) {
                        ((MissUI) context).runOnUiThread(() -> {
                            holder.likeButton.clearColorFilter();
                            Toast.makeText(context, "Like eliminado", Toast.LENGTH_SHORT).show();
                            if (listener != null) {
                                listener.onNoteUpdated(note, position);
                            }
                        });
                    }
                })
                .exceptionally(e -> {
                    Log.e(TAG, "Error al eliminar reacción", e);
                    return null;
                });
    }

    private void handleRenote(MisskeyNoteTimeline note, int position, NoteViewHolder holder) {
        Log.d(TAG, "Haciendo renote de nota: " + note.getId());

        noteInteract.renote(note.getId())
                .thenAccept(newNote -> {
                    ((MissUI) context).runOnUiThread(() -> {
                        holder.repostButton.setColorFilter(context.getColor(android.R.color.holo_green_dark));
                        Toast.makeText(context, "¡Renote exitoso!", Toast.LENGTH_SHORT).show();
                        if (listener != null) {
                            listener.onRenoteDone(newNote);
                        }
                    });
                })
                .exceptionally(e -> {
                    Log.e(TAG, "Error al hacer renote", e);
                    ((MissUI) context).runOnUiThread(() -> {
                        Toast.makeText(context, "Error al repostear", Toast.LENGTH_SHORT).show();
                    });
                    return null;
                });
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    static class NoteViewHolder extends RecyclerView.ViewHolder {
        TextView postUser, postInstance, postDate, postText;
        ImageView postImage;
        ImageView userAvatar; // El avatar en tu layout

        // Botones de interacción
        ImageView likeButton;
        ImageView repostButton;
        ImageView replyButton;
        ImageView bookmarkButton;
        ImageView moreButton;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            postUser = itemView.findViewById(R.id.postUser);
            postInstance = itemView.findViewById(R.id.postInstance);
            postDate = itemView.findViewById(R.id.postDate);
            postText = itemView.findViewById(R.id.postText);
            postImage = itemView.findViewById(R.id.postImage);

            userAvatar = itemView.findViewById(R.id.userAvatar);

            likeButton = itemView.findViewById(R.id.likeButton);
            replyButton = itemView.findViewById(R.id.replyButton);
            repostButton = itemView.findViewById(R.id.repostButton);
            bookmarkButton = itemView.findViewById(R.id.bookmarkButton);
            moreButton = itemView.findViewById(R.id.moreButton);
        }
    }
}
