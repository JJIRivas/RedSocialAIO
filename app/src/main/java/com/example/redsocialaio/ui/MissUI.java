package com.example.redsocialaio.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.redsocialaio.R;
import com.example.redsocialaio.misskey.core.notes.MisskeyNoteTimeline;
import com.example.redsocialaio.misskey.core.notes.MisskeyTimelineService;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class MissUI extends AppCompatActivity implements MisskeyNoteAdapter.OnNoteInteractionListener {
    private static final String TAG = "MissUI";

    private MisskeyTimelineService timelineService;
    private MisskeyNoteAdapter noteAdapter;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefresh;
    private ProgressBar progressBar;
    private FloatingActionButton fabCompose;

    private String lastNoteId = null;
    private boolean isLoading = false;
    private final List<MisskeyNoteTimeline> allNotes = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unified_timeline);

//        initViews();
//        setupRecyclerView();
//        setupSwipeRefresh();
//        setupFab();

        timelineService = new MisskeyTimelineService(this);

        // Cargar primera página
        // loadTimeline(true);
    }

    private void initViews() {
        recyclerView = findViewById(R.id.timelineRecyclerView);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        progressBar = findViewById(R.id.progressBar);
        fabCompose = findViewById(R.id.fabCompose);
    }

    private void setupRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        noteAdapter = new MisskeyNoteAdapter(this);
        noteAdapter.setInteractionListener(this);
        recyclerView.setAdapter(noteAdapter);

        // Paginación infinita
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager manager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (manager != null && !isLoading) {
                    int visibleItemCount = manager.getChildCount();
                    int totalItemCount = manager.getItemCount();
                    int firstVisibleItemPosition = manager.findFirstVisibleItemPosition();

                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                            && firstVisibleItemPosition >= 0
                            && totalItemCount >= 20) {
                        loadMorePosts();
                    }
                }
            }
        });
    }

    private void setupSwipeRefresh() {
        swipeRefresh.setOnRefreshListener(() -> loadTimeline(true));
        swipeRefresh.setColorSchemeResources(
                R.color.white,
                R.color.black,
                R.color.purple_200
        );
    }

    private void setupFab() {
        fabCompose.setOnClickListener(v -> {
            // TODO: Abrir activity para crear post
            Toast.makeText(this, "Crear post - Por implementar", Toast.LENGTH_SHORT).show();
        });
    }

    private void loadTimeline(boolean refresh) {
        if (refresh) {
            allNotes.clear();
            lastNoteId = null;
            swipeRefresh.setRefreshing(true);
        } else {
            isLoading = true;
            progressBar.setVisibility(View.VISIBLE);
        }

        timelineService.getHomeTimeline()
                .thenAccept(notes -> {
                    Log.d(TAG, "Notas recibidas: " + notes.size());

                    if (!notes.isEmpty()) {
                        lastNoteId = notes.get(notes.size() - 1).getId();
                        allNotes.addAll(notes);
                    }

                    runOnUiThread(() -> {
                        noteAdapter.setNotes(new ArrayList<>(allNotes));
                        swipeRefresh.setRefreshing(false);
                        progressBar.setVisibility(View.GONE);
                        isLoading = false;
                    });
                })
                .exceptionally(e -> {
                    Log.e(TAG, "Error cargando timeline", e);
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Error al cargar timeline", Toast.LENGTH_LONG).show();
                        swipeRefresh.setRefreshing(false);
                        progressBar.setVisibility(View.GONE);
                        isLoading = false;
                    });
                    return null;
                });
    }

    private void loadMorePosts() {
        if (lastNoteId == null || isLoading) return;

        isLoading = true;
        progressBar.setVisibility(View.VISIBLE);

        MisskeyTimelineService.TimelineRequest request = new MisskeyTimelineService.TimelineRequest()
                .withLimit(20)
                .withUntilId(lastNoteId);

        timelineService.getHomeTimeline(request)
                .thenAccept(notes -> {
                    if (!notes.isEmpty()) {
                        lastNoteId = notes.get(notes.size() - 1).getId();
                        allNotes.addAll(notes);
                    }

                    runOnUiThread(() -> {
                        noteAdapter.setNotes(new ArrayList<>(allNotes));
                        progressBar.setVisibility(View.GONE);
                        isLoading = false;
                    });
                })
                .exceptionally(e -> {
                    Log.e(TAG, "Error cargando más posts", e);
                    runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        isLoading = false;
                    });
                    return null;
                });
    }

    @Override
    public void onNoteUpdated(MisskeyNoteTimeline note, int position) {
        // Actualizar la nota en la lista
        if (position >= 0 && position < allNotes.size()) {
            allNotes.set(position, note);
        }
    }

    @Override
    public void onRenoteDone(MisskeyNoteTimeline newNote) {
        // Añadir el nuevo renote al principio del timeline
        allNotes.add(0, newNote);
        noteAdapter.setNotes(new ArrayList<>(allNotes));
        recyclerView.scrollToPosition(0);
    }
}
