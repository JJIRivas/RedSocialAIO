package com.example.redsocialaio.ui;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.example.redsocialaio.R;
import com.example.redsocialaio.misskey.core.notes.MisskeyNoteTimeline;
import com.example.redsocialaio.misskey.core.notes.MisskeyTimelineService;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Map;

public class MissUI extends AppCompatActivity {

    private MisskeyTimelineService timelineService;
    private String lastNoteId = null; // Para paginación
    private MisskeyNoteAdapter noteAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_miss_ui);

        RecyclerView recyclerView = findViewById(R.id.timelineRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        noteAdapter = new MisskeyNoteAdapter();
        recyclerView.setAdapter(noteAdapter);

        timelineService = new MisskeyTimelineService(this);

        // Cargar primera página
        loadTimeline();
    }

    private void loadTimeline() {
        // Primera carga
        timelineService.getHomeTimeline()
                .thenAccept(notes -> {
                    Log.d("MissUI", "Notas recibidas: " + notes.size());
                    // Guardar ID del último post para paginación
                    if (!notes.isEmpty()) {
                        lastNoteId = notes.get(notes.size() - 1).getId();
                    }
                    runOnUiThread(() -> noteAdapter.setNotes(notes));
                });
    }

    private void loadMorePosts() {
        // Cargar siguientes posts (paginación)
        MisskeyTimelineService.TimelineRequest request = new MisskeyTimelineService.TimelineRequest()
                .withLimit(20)
                .withUntilId(lastNoteId); // Posts más viejos que este ID

        timelineService.getHomeTimeline(request)
                .thenAccept(notes -> {
                    if (!notes.isEmpty()) {
                        lastNoteId = notes.get(notes.size() - 1).getId();
                    }
                    displayNotes(notes);
                });
    }

    private void displayNotes(List<MisskeyNoteTimeline> notes) {
        for (MisskeyNoteTimeline note : notes) {
            // Manejar reacciones (pueden ser custom)
            if (note.getReactions() != null) {
                for (Map.Entry<String, Integer> reaction : note.getReactions().entrySet()) {
                    String emoji = reaction.getKey();
                    int count = reaction.getValue();

                    if (note.isCustomEmoji(emoji)) {
                        // Es un emoji custom, obtener URL
                        String emojiUrl = note.getCustomEmojiUrl(emoji);
                        Log.d("Reaction", emoji + " (" + count + ") - URL: " + emojiUrl);
                    } else {
                        // Es un emoji Unicode normal
                        Log.d("Reaction", emoji + " (" + count + ")");
                    }
                }
            }
        }
    }
}
