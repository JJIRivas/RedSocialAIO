package com.example.redsocialaio.ui.Unified;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.redsocialaio.core.checkers.InstancePickerActivity;
import com.example.redsocialaio.mastodon.MastodonStatusTimeline;
import com.example.redsocialaio.R;
import com.example.redsocialaio.misskey.core.notes.MisskeyNoteTimeline;
import com.google.android.material.navigation.NavigationView;

import java.util.List;

public class UnifiedTimeline extends AppCompatActivity
        implements UnifiedPostAdapter.OnUnifiedPostInteractionListener,
        UnifiedTimelinePagination.PaginationCallback {

    private static final String TAG = "UnifiedTimeline";
    private static final int LOAD_MORE_THRESHOLD = 3;

    // UI Components
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefresh;
    private ProgressBar progressBar;
    private UnifiedPostAdapter adapter;
    private LinearLayoutManager layoutManager;

    // Logic Components
    private TimelineMerger timelineMerger;
    private List<UnifiedPost> currentPosts;
    private final boolean isLoading = false;

    private UnifiedTimelinePagination pagination;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unified_timeline);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_profile) {
                // Ir al perfil del usuario
                Toast.makeText(this, "Mi Perfil (por implementar)", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.nav_AddInstance) {
                // Agregar o editar redes sociales
                startActivity(new Intent(this, InstancePickerActivity.class));
            }

            drawer.closeDrawer(GravityCompat.START);
            return true;
        });


        initializeComponents();
        setupRecyclerView();
        setupSwipeRefresh();
        setupPagination();
        loadInitialTimeline();
    }

    private void initializeComponents() {
        // UI
        recyclerView = findViewById(R.id.timelineRecyclerView);
        swipeRefresh = findViewById(R.id.swipeRefresh);

        // Logic
        timelineMerger = new TimelineMerger(this);
        adapter = new UnifiedPostAdapter(this);
        adapter.setInteractionListener(this);
        pagination = new UnifiedTimelinePagination(this);
        pagination.setCallback(this);


        layoutManager = new LinearLayoutManager(this);
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (layoutManager != null && !pagination.isLoading()) {
                    int visibleItemCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                    // Usar el método shouldLoadMore de la paginación
                    if (pagination.shouldLoadMore(visibleItemCount, totalItemCount,
                            firstVisibleItemPosition, LOAD_MORE_THRESHOLD)) {

                        Log.d(TAG, "Cargando más posts...");
                        pagination.loadMorePosts();
                    }
                }
            }
        });
    }

    private void setupSwipeRefresh() {
        swipeRefresh.setOnRefreshListener(() -> {
            Log.d(TAG, "Pull-to-refresh activado");
            pagination.refreshTimeline();
        });

        swipeRefresh.setColorSchemeResources(
                android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light
        );
    }

    private void setupPagination() {
        // El callback ya está configurado en initializeComponents()
        Log.d(TAG, "Paginación configurada");
    }

    private void loadInitialTimeline() {
        Log.d(TAG, "=== Cargando timeline inicial ===");
        pagination.loadInitialTimeline();
    }

    @Override
    public void onLoadingStateChanged(boolean isLoading) {
        runOnUiThread(() -> {
            // Mostrar/ocultar indicadores de carga
            if (pagination.getTotalPostsCount() == 0) {
                // Primera carga - usar swipe refresh
                swipeRefresh.setRefreshing(isLoading);
            } else {
                // Paginación - usar progress bar en bottom
                //progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            }

            Log.d(TAG, "Loading state: " + isLoading);
        });
    }

    @Override
    public void onPostsLoaded(List<UnifiedPost> allPosts, int newPostsCount) {
        runOnUiThread(() -> {
            // Actualizar adapter con todos los posts
            adapter.setPosts(allPosts);

            // Mostrar estadísticas
            String stats = pagination.getTimelineStats();
            Log.d(TAG, stats);

            /*if (newPostsCount > 0) {
                String message = newPostsCount > 20 ?
                        stats : // Carga inicial - mostrar stats completos
                        "+" + newPostsCount + " posts cargados"; // Paginación - mostrar incremento

                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            }*/

            // Debug de paginación
            pagination.debugPaginationState();
        });
    }

    @Override
    public void onError(String message) {
        runOnUiThread(() -> {
            Toast.makeText(this, "Error: " + message, Toast.LENGTH_LONG).show();
            Log.e(TAG, "Error en paginación: " + message);
        });
    }

    // Métodos adicionales para debugging/testing
    public void debugTimelineState() {
        Log.d(TAG, "=== ESTADO DEL TIMELINE ===");
        Log.d(TAG, "Total posts: " + pagination.getTotalPostsCount());
        Log.d(TAG, "Is loading: " + pagination.isLoading());
        Log.d(TAG, "Has more: " + pagination.hasMorePosts());
        Log.d(TAG, pagination.getTimelineStats());

        List<UnifiedPost> posts = pagination.getAllPosts();
        for (int i = 0; i < Math.min(5, posts.size()); i++) {
            UnifiedPost post = posts.get(i);
            Log.d(TAG, String.format("Post %d: %s %s", i + 1,
                    post.getPlatformEmoji(), post.getId()));
        }
        Log.d(TAG, "========================");
    }

    // Métodos para testing manual
    public void forceLoadMore() {
        if (!pagination.isLoading()) {
            Log.d(TAG, "Forzando carga de más posts...");
            pagination.loadMorePosts();
        } else {
            Toast.makeText(this, "Ya está cargando...", Toast.LENGTH_SHORT).show();
        }
    }

    public void showPaginationStats() {
        String stats = pagination.getTimelineStats();
        String fullStats = stats + "\nHas more: " + pagination.hasMorePosts() +
                "\nLoading: " + pagination.isLoading();

        Toast.makeText(this, fullStats, Toast.LENGTH_LONG).show();
        debugTimelineState();
    }

    private void checkAccountsAndLoadTimeline() {
        if (timelineMerger.hasBothAccountsConfigured()) {
            Log.d(TAG, "Ambas cuentas configuradas, cargando timeline unificado");
            loadUnifiedTimeline();
        } else {
            Log.w(TAG, "No todas las cuentas están configuradas");
            Toast.makeText(this, "Configura cuentas de Misskey y Mastodon para ver timeline unificado",
                    Toast.LENGTH_LONG).show();

            // Cargar timeline público como fallback
            loadPublicTimeline();
        }
    }

    private void loadUnifiedTimeline() {
        Log.d(TAG, "=== Cargando timeline unificado ===");
        swipeRefresh.setRefreshing(true);

        timelineMerger.getUnifiedTimeline()
                .thenAccept(unifiedPosts -> {
                    runOnUiThread(() -> {
                        currentPosts = unifiedPosts;
                        adapter.setPosts(unifiedPosts);
                        swipeRefresh.setRefreshing(false);

                        // Mostrar estadísticas
                        String stats = timelineMerger.getTimelineStats(unifiedPosts);
                        Log.d(TAG, stats);
                        Toast.makeText(this, stats, Toast.LENGTH_SHORT).show();

                        // Log detallado para debug
                        logTimelineDetails(unifiedPosts);
                    });
                })
                .exceptionally(e -> {
                    Log.e(TAG, "Error cargando timeline unificado", e);
                    runOnUiThread(() -> {
                        swipeRefresh.setRefreshing(false);
                        Toast.makeText(this, "Error cargando timeline", Toast.LENGTH_SHORT).show();
                    });
                    return null;
                });
    }

    private void refreshTimeline() {
        Log.d(TAG, "Refrescando timeline...");
        loadUnifiedTimeline();
    }

    private void loadPublicTimeline() {
        Log.d(TAG, "=== Cargando timeline público unificado ===");
        swipeRefresh.setRefreshing(true);

        timelineMerger.getUnifiedPublicTimeline()
                .thenAccept(unifiedPosts -> {
                    runOnUiThread(() -> {
                        currentPosts = unifiedPosts;
                        adapter.setPosts(unifiedPosts);
                        swipeRefresh.setRefreshing(false);

                        String stats = "Timeline público: " + timelineMerger.getTimelineStats(unifiedPosts);
                        Toast.makeText(this, stats, Toast.LENGTH_SHORT).show();
                    });
                })
                .exceptionally(e -> {
                    Log.e(TAG, "Error cargando timeline público", e);
                    runOnUiThread(() -> {
                        swipeRefresh.setRefreshing(false);
                        Toast.makeText(this, "Error cargando timeline público", Toast.LENGTH_SHORT).show();
                    });
                    return null;
                });
    }

    private void logTimelineDetails(List<UnifiedPost> posts) {
        Log.d(TAG, "=== DETALLES DEL TIMELINE UNIFICADO ===");
        Log.d(TAG, "Total posts: " + posts.size());

        // Mostrar primeros 10 posts con detalles
        for (int i = 0; i < Math.min(10, posts.size()); i++) {
            UnifiedPost post = posts.get(i);
            Log.d(TAG, String.format("Post %d: %s - %s by @%s",
                    i + 1,
                    post.getPlatformEmoji(),
                    post.getId(),
                    post.getUser() != null ? post.getUser().getUserName() : "unknown"
            ));
        }

        // Estadísticas de intercalado
        int consecutiveMisskey = 0;
        int consecutiveMastodon = 0;
        int maxConsecutiveMisskey = 0;
        int maxConsecutiveMastodon = 0;

        for (UnifiedPost post : posts) {
            if (post.isMisskey()) {
                consecutiveMisskey++;
                consecutiveMastodon = 0;
                maxConsecutiveMisskey = Math.max(maxConsecutiveMisskey, consecutiveMisskey);
            } else {
                consecutiveMastodon++;
                consecutiveMisskey = 0;
                maxConsecutiveMastodon = Math.max(maxConsecutiveMastodon, consecutiveMastodon);
            }
        }

        Log.d(TAG, "Max consecutivos - Misskey: " + maxConsecutiveMisskey +
                ", Mastodon: " + maxConsecutiveMastodon);
        Log.d(TAG, "=== FIN DETALLES ===");
    }

    // Implementación de OnUnifiedPostInteractionListener
    @Override
    public void onMisskeyNoteUpdated(MisskeyNoteTimeline note, int position) {
        Log.d(TAG, "Misskey note actualizada: " + note.getId());
        // Opcional: Actualizar el post en la lista local
    }

    @Override
    public void onMastodonStatusUpdated(MastodonStatusTimeline status, int position) {
        Log.d(TAG, "Mastodon status actualizado: " + status.getId());
        // Opcional: Actualizar el status en la lista local
    }

    @Override
    public void onMisskeyRenoteDone(MisskeyNoteTimeline newNote) {
        Log.d(TAG, "Nuevo renote de Misskey: " + newNote.getId());

        // Añadir el nuevo renote al timeline
        if (currentPosts != null) {
            currentPosts = timelineMerger.addNewPost(currentPosts, newNote, null);
            adapter.setPosts(currentPosts);

            // Scroll al top para mostrar el nuevo post
            recyclerView.smoothScrollToPosition(0);

            Toast.makeText(this, "🗾 Renote añadido al timeline", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onMastodonReblogDone(MastodonStatusTimeline newStatus) {
        Log.d(TAG, "Nuevo reblog de Mastodon: " + newStatus.getId());

        // Añadir el nuevo reblog al timeline
        if (currentPosts != null) {
            currentPosts = timelineMerger.addNewPost(currentPosts, null, newStatus);
            adapter.setPosts(currentPosts);

            // Scroll al top para mostrar el nuevo post
            recyclerView.smoothScrollToPosition(0);

            Toast.makeText(this, "🐘 Reblog añadido al timeline", Toast.LENGTH_SHORT).show();
        }
    }

    // Métodos adicionales para funcionalidad extra
    public void showOnlyMisskey() {
        if (currentPosts != null) {
            List<UnifiedPost> misskeyOnly = timelineMerger.filterByPlatform(
                    currentPosts, UnifiedPost.PlatformType.MISSKEY);
            adapter.setPosts(misskeyOnly);
            Toast.makeText(this, "Mostrando solo Misskey: " + misskeyOnly.size() + " posts",
                    Toast.LENGTH_SHORT).show();
        }
    }

    public void showOnlyMastodon() {
        if (currentPosts != null) {
            List<UnifiedPost> mastodonOnly = timelineMerger.filterByPlatform(
                    currentPosts, UnifiedPost.PlatformType.MASTODON);
            adapter.setPosts(mastodonOnly);
            Toast.makeText(this, "Mostrando solo Mastodon: " + mastodonOnly.size() + " posts",
                    Toast.LENGTH_SHORT).show();
        }
    }

    public void showUnifiedTimeline() {
        if (currentPosts != null) {
            adapter.setPosts(currentPosts);
            String stats = timelineMerger.getTimelineStats(currentPosts);
            Toast.makeText(this, "Timeline unificado: " + stats, Toast.LENGTH_SHORT).show();
        }
    }


}
