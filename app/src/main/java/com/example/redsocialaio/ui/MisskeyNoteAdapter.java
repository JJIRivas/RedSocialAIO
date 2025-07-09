package com.example.redsocialaio.ui;

import android.content.Context;
import android.graphics.Typeface;
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

        Log.d(TAG, "=== Mostrando note " + position + " ===");
        Log.d(TAG, "ID: " + note.getId());
        Log.d(TAG, "Es repost: " + note.isRepost());
        Log.d(TAG, "Tiene renote: " + (note.getRenote() != null));

        if (note.isRepost() && note.getRenote() != null) {
            bindRenote(holder, note);
        } else {
            bindNormalNote(holder, note);
        }

        setupInteractionButtons(holder, note, position);
    }


    private void displayNoteContent(NoteViewHolder holder, MisskeyNoteTimeline note) {
        Log.d(TAG, "Mostrando contenido - ID: " + note.getId());
        Log.d(TAG, "Text: " + note.getText());
        Log.d(TAG, "User: " + (note.getUser() != null ? note.getUser().getUserName() : "null"));

        displayNoteText(holder, note);
        displayNoteUserInfo(holder, note);
        displayNoteDate(holder, note);
        displayNoteImage(holder, note);
    }


    private void setupInteractionButtons(NoteViewHolder holder, MisskeyNoteTimeline note, int position) {
        setupLikeButton(holder, note, position);
        setupRepostButton(holder, note, position);
    }


    private void bindRenote(NoteViewHolder holder, MisskeyNoteTimeline note) {
        MisskeyNoteTimeline originalNote = note.getRenote();

        Log.d(TAG, "Renote - Texto original: " + originalNote.getText());
        Log.d(TAG, "Renote - Usuario original: " +
                (originalNote.getUser() != null ? originalNote.getUser().getUserName() : "null"));

        if (note.getUser() != null) {
            String renoteText = "🔁 " + note.getUser().getUserName() + " reposteó";
            holder.postUser.setText(renoteText);
            holder.postUser.setTextSize(12);
            holder.postUser.setTypeface(null, Typeface.ITALIC);
        }

        displayNoteContent(holder, originalNote);

        if (note.getText() != null && !note.getText().trim().isEmpty()) {
            bindQuoteRenote(holder, originalNote, note.getText());
        }
    }

    private void bindNormalNote(NoteViewHolder holder, MisskeyNoteTimeline note) {
        holder.postUser.setTextSize(14);
        holder.postUser.setTypeface(null, Typeface.BOLD);
        displayNoteContent(holder, note);
    }

    private void bindQuoteRenote(NoteViewHolder holder, MisskeyNoteTimeline originalNote, String quotedText) {
        String originalText = "";

        if (originalNote.getText() != null && !originalNote.getText().isEmpty()) {
            originalText = originalNote.getText();
        } else if (originalNote.hasFiles()) {
            originalText = ""; // No texto si es solo imagen
        }

        if (!originalText.isEmpty() && !quotedText.isEmpty()) {
            holder.postText.setText(originalText + "\n\n💬 " + quotedText);
        } else if (!quotedText.isEmpty()) {
            holder.postText.setText("💬 " + quotedText);
        }
    }

    private void displayNoteText(NoteViewHolder holder, MisskeyNoteTimeline note) {
        String content = note.getText();

        if (content == null || content.trim().isEmpty()) {
            if (note.hasFiles()) {
                holder.postText.setVisibility(View.GONE);
            } else {
                holder.postText.setVisibility(View.VISIBLE);
                holder.postText.setText("");
            }
        } else {
            holder.postText.setVisibility(View.VISIBLE);
            holder.postText.setText(content);
        }
    }

    private void displayNoteUserInfo(NoteViewHolder holder, MisskeyNoteTimeline note) {
        if (note.getUser() == null) return;

        if (holder.postUser.getTypeface() == null || !holder.postUser.getTypeface().isItalic()) {
            holder.postUser.setText("@" + note.getUser().getUserName());
        }

        if (holder.userAvatar != null && note.getUser().getAvatarURL() != null) {
            Glide.with(context)
                    .load(note.getUser().getAvatarURL())
                    .circleCrop()
                    .placeholder(R.drawable.ic_profile)
                    .error(R.drawable.ic_profile)
                    .into(holder.userAvatar);
        }

        String instanceUrl = note.getUser().getInstanceUrl();
        if (instanceUrl != null && !instanceUrl.isEmpty()) {
            instanceUrl = instanceUrl.replace("https://", "").replace("http://", "");
            holder.postInstance.setText(instanceUrl);
        } else {
            holder.postInstance.setText("misskey");
        }
    }

    private void displayNoteDate(NoteViewHolder holder, MisskeyNoteTimeline note) {
        if (note.getCreatedAt() != null) {
            holder.postDate.setText(dateFormat.format(note.getCreatedAt()));
        } else {
            holder.postDate.setText("");
        }
    }

    private void displayNoteImage(NoteViewHolder holder, MisskeyNoteTimeline note) {
        if (note.hasFiles() && !note.getFiles().isEmpty()) {
            holder.postImage.setVisibility(View.VISIBLE);
            MisskeyFile firstImage = note.getFiles().get(0);
            String imageUrl = firstImage.getUrl();

            if (imageUrl != null && !imageUrl.isEmpty()) {
                Glide.with(context)
                        .load(imageUrl)
                        .placeholder(R.drawable.ic_image)
                        .error(R.drawable.ic_image)
                        .into(holder.postImage);

                holder.postImage.setOnClickListener(v ->
                        Toast.makeText(context, "Imagen: " + imageUrl, Toast.LENGTH_SHORT).show()
                );
            }
        } else {
            holder.postImage.setVisibility(View.GONE);
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

    private void setupLikeButton(NoteViewHolder holder, MisskeyNoteTimeline note, int position) {
        if (holder.likeButton == null) return;

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

    private void setupRepostButton(NoteViewHolder holder, MisskeyNoteTimeline note, int position) {
        if (holder.repostButton == null) return;

        if (note.isRenoted()) {
            holder.repostButton.setColorFilter(context.getColor(android.R.color.holo_green_dark));
        } else {
            holder.repostButton.clearColorFilter();
        }

        holder.repostButton.setOnClickListener(v -> {
            if (!note.isRenoted()) {
                handleRenote(note, position, holder);
            } else {
                Toast.makeText(context, "Ya hiciste renote de este post", Toast.LENGTH_SHORT).show();
            }
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
        ImageView userAvatar;


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
