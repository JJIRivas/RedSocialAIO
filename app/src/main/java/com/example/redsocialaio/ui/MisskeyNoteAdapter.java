package com.example.redsocialaio.ui;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.redsocialaio.misskey.core.notes.MisskeyNoteTimeline;
import com.example.redsocialaio.R;

import java.util.ArrayList;
import java.util.List;

public class MisskeyNoteAdapter extends RecyclerView.Adapter<MisskeyNoteAdapter.NoteViewHolder> {

    private List<MisskeyNoteTimeline> notes = new ArrayList<>();

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
        Log.d("Adapter", "Mostrando nota: " + note.getText());
        holder.postText.setText(note.getText());
        if (note.getUser() != null) {
            holder.postUser.setText(note.getUser().getUserName());
            holder.postInstance.setText("note.getUser().getHost()"); // o como guardes la instancia
        }

        // Fecha (formatea Date -> String a tu gusto)
        holder.postDate.setText(note.getCreatedAt().toString()); // mejor usa un formatter

        // Imagen (solo si hay)
        if (note.hasFiles() && !note.getFiles().isEmpty()) {
            holder.postImage.setVisibility(View.VISIBLE);
            // Aquí deberías usar Glide o Picasso para cargar la URL real
            // holder.postImage.setImageURI(...) o Glide.with(...).load(...).into(holder.postImage)
        } else {
            holder.postImage.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    static class NoteViewHolder extends RecyclerView.ViewHolder {
        TextView postUser, postInstance, postDate, postText;
        ImageView postImage;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            postUser = itemView.findViewById(R.id.postUser);
            postInstance = itemView.findViewById(R.id.postInstance);
            postDate = itemView.findViewById(R.id.postDate);
            postText = itemView.findViewById(R.id.postText);
            postImage = itemView.findViewById(R.id.postImage);
        }
    }
}
