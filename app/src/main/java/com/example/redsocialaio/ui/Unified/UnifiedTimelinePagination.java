package com.example.redsocialaio.ui.Unified;


import android.content.Context;
import android.util.Log;

import com.example.redsocialaio.misskey.core.notes.MisskeyNoteTimeline;
import com.example.redsocialaio.misskey.core.notes.MisskeyTimelineService;
import com.example.redsocialaio.mastodon.MastodonStatusTimeline;
import com.example.redsocialaio.mastodon.MastodonTimelineService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Maneja la paginación infinita para timeline unificado
 * Carga más posts de Misskey y Mastodon manteniendo el orden cronológico
 */
public class UnifiedTimelinePagination {
    private static final String TAG = "UnifiedPagination";

    // Services
    private final Context context;
    private final MisskeyTimelineService misskeyService;
    private final MastodonTimelineService mastodonService;

    // Pagination state
    private String lastMisskeyNoteId = null;
    private String lastMastodonStatusId = null;
    private boolean isLoading = false;
    private boolean hasMoreMisskey = true;
    private boolean hasMoreMastodon = true;

    // Data
    private final List<UnifiedPost> allUnifiedPosts = new ArrayList<>();

    // UI callback
    public interface PaginationCallback {
        void onLoadingStateChanged(boolean isLoading);

        void onPostsLoaded(List<UnifiedPost> allPosts, int newPostsCount);

        void onError(String message);
    }

    private PaginationCallback callback;

    public UnifiedTimelinePagination(Context context) {
        this.context = context;
        this.misskeyService = new MisskeyTimelineService(context);
        this.mastodonService = new MastodonTimelineService(context);
    }

    public void setCallback(PaginationCallback callback) {
        this.callback = callback;
    }

    /**
     * Carga el timeline inicial (primera página)
     */
    public void loadInitialTimeline() {
        Log.d(TAG, "=== Cargando timeline inicial ===");

        // Reset pagination state
        lastMisskeyNoteId = null;
        lastMastodonStatusId = null;
        hasMoreMisskey = true;
        hasMoreMastodon = true;
        allUnifiedPosts.clear();

        loadMorePosts();
    }

    /**
     * Carga más posts de ambas plataformas (paginación)
     */
    public void loadMorePosts() {
        if (isLoading) {
            Log.d(TAG, "Ya está cargando, ignorando request");
            return;
        }

        if (!hasMoreMisskey && !hasMoreMastodon) {
            Log.d(TAG, "No hay más posts en ninguna plataforma");
            return;
        }

        Log.d(TAG, "=== Cargando más posts ===");
        Log.d(TAG, "Last Misskey ID: " + lastMisskeyNoteId);
        Log.d(TAG, "Last Mastodon ID: " + lastMastodonStatusId);
        Log.d(TAG, "Has more - Misskey: " + hasMoreMisskey + ", Mastodon: " + hasMoreMastodon);

        isLoading = true;
        notifyLoadingState(true);

        // Crear requests para ambas plataformas
        CompletableFuture<List<MisskeyNoteTimeline>> misskeyFuture = loadMoreMisskeyPosts();
        CompletableFuture<List<MastodonStatusTimeline>> mastodonFuture = loadMoreMastodonPosts();

        // Procesar resultados cuando ambos terminen
        CompletableFuture.allOf(misskeyFuture, mastodonFuture)
                .thenApply(v -> {
                    try {
                        List<MisskeyNoteTimeline> newMisskeyNotes = misskeyFuture.join();
                        List<MastodonStatusTimeline> newMastodonStatuses = mastodonFuture.join();

                        Log.d(TAG, "Nuevos posts obtenidos - Misskey: " + newMisskeyNotes.size() +
                                ", Mastodon: " + newMastodonStatuses.size());

                        return processNewPosts(newMisskeyNotes, newMastodonStatuses);

                    } catch (Exception e) {
                        Log.e(TAG, "Error procesando nuevos posts", e);
                        return 0;
                    }
                })
                .thenAccept(newPostsCount -> {
                    isLoading = false;
                    notifyLoadingState(false);

                    if (callback != null) {
                        callback.onPostsLoaded(new ArrayList<>(allUnifiedPosts), newPostsCount);
                    }

                    Log.d(TAG, "Paginación completada. Total posts: " + allUnifiedPosts.size() +
                            ", Nuevos: " + newPostsCount);
                })
                .exceptionally(e -> {
                    Log.e(TAG, "Error en paginación", e);
                    isLoading = false;
                    notifyLoadingState(false);

                    if (callback != null) {
                        callback.onError("Error cargando más posts");
                    }
                    return null;
                });
    }

    /**
     * Carga más posts de Misskey
     */
    private CompletableFuture<List<MisskeyNoteTimeline>> loadMoreMisskeyPosts() {
        if (!hasMoreMisskey) {
            return CompletableFuture.completedFuture(new ArrayList<>());
        }

        MisskeyTimelineService.TimelineRequest request = new MisskeyTimelineService.TimelineRequest()
                .withLimit(20);

        if (lastMisskeyNoteId != null) {
            request.withUntilId(lastMisskeyNoteId);
        }

        return misskeyService.getHomeTimeline(request)
                .thenApply(notes -> {
                    if (notes == null || notes.isEmpty()) {
                        Log.d(TAG, "No más posts de Misskey disponibles");
                        hasMoreMisskey = false;
                        return new ArrayList<MisskeyNoteTimeline>();
                    }

                    // Actualizar último ID para próxima paginación
                    lastMisskeyNoteId = notes.get(notes.size() - 1).getId();
                    Log.d(TAG, "Misskey: " + notes.size() + " nuevos posts, último ID: " + lastMisskeyNoteId);

                    return notes;
                })
                .exceptionally(e -> {
                    Log.w(TAG, "Error cargando más posts de Misskey", e);
                    hasMoreMisskey = false; // Desactivar Misskey si falla
                    return new ArrayList<>();
                });
    }

    /**
     * Carga más posts de Mastodon
     */
    private CompletableFuture<List<MastodonStatusTimeline>> loadMoreMastodonPosts() {
        if (!hasMoreMastodon) {
            return CompletableFuture.completedFuture(new ArrayList<>());
        }

        MastodonTimelineService.TimelineRequest request = new MastodonTimelineService.TimelineRequest()
                .withLimit(20);

        if (lastMastodonStatusId != null) {
            request.withMaxId(lastMastodonStatusId); // Mastodon usa maxId en lugar de untilId
        }

        return mastodonService.getHomeTimeline(request)
                .thenApply(statuses -> {
                    if (statuses == null || statuses.isEmpty()) {
                        Log.d(TAG, "No más posts de Mastodon disponibles");
                        hasMoreMastodon = false;
                        return new ArrayList<MastodonStatusTimeline>();
                    }

                    // Actualizar último ID para próxima paginación
                    lastMastodonStatusId = statuses.get(statuses.size() - 1).getId();
                    Log.d(TAG, "Mastodon: " + statuses.size() + " nuevos posts, último ID: " + lastMastodonStatusId);

                    return statuses;
                })
                .exceptionally(e -> {
                    Log.w(TAG, "Error cargando más posts de Mastodon", e);
                    hasMoreMastodon = false; // Desactivar Mastodon si falla
                    return new ArrayList<>();
                });
    }

    /**
     * Procesa y añade los nuevos posts manteniendo orden cronológico
     */
    private int processNewPosts(List<MisskeyNoteTimeline> newMisskeyNotes,
                                List<MastodonStatusTimeline> newMastodonStatuses) {

        List<UnifiedPost> newUnifiedPosts = new ArrayList<>();

        // Convertir nuevos posts de Misskey
        for (MisskeyNoteTimeline note : newMisskeyNotes) {
            newUnifiedPosts.add(new UnifiedPost(note));
        }

        // Convertir nuevos posts de Mastodon
        for (MastodonStatusTimeline status : newMastodonStatuses) {
            newUnifiedPosts.add(new UnifiedPost(status));
        }

        if (newUnifiedPosts.isEmpty()) {
            Log.d(TAG, "No hay nuevos posts para procesar");
            return 0;
        }

        // Añadir a la lista principal
        allUnifiedPosts.addAll(newUnifiedPosts);

        // Reordenar todo por fecha (más recientes primero)
        Collections.sort(allUnifiedPosts, new Comparator<UnifiedPost>() {
            @Override
            public int compare(UnifiedPost a, UnifiedPost b) {
                return Long.compare(b.getTimestamp(), a.getTimestamp());
            }
        });

        Log.d(TAG, "Posts procesados y ordenados. Total: " + allUnifiedPosts.size());

        return newUnifiedPosts.size();
    }

    /**
     * Verifica si se debe cargar más posts basándose en la posición del scroll
     */
    public boolean shouldLoadMore(int visibleItemCount, int totalItemCount,
                                  int firstVisibleItemPosition, int loadMoreThreshold) {

        if (isLoading) return false;
        if (!hasMoreMisskey && !hasMoreMastodon) return false;

        // Cargar más cuando esté cerca del final
        boolean shouldLoad = (visibleItemCount + firstVisibleItemPosition) >=
                (totalItemCount - loadMoreThreshold);

        if (shouldLoad) {
            Log.d(TAG, "Should load more: visibleItems=" + visibleItemCount +
                    ", totalItems=" + totalItemCount +
                    ", firstVisible=" + firstVisibleItemPosition);
        }

        return shouldLoad;
    }

    /**
     * Añade un nuevo post al principio del timeline (para renotes/reblogs)
     */
    public void addNewPost(MisskeyNoteTimeline newMisskeyNote, MastodonStatusTimeline newMastodonStatus) {
        if (newMisskeyNote != null) {
            allUnifiedPosts.add(0, new UnifiedPost(newMisskeyNote));
            Log.d(TAG, "Nuevo post de Misskey añadido al timeline");
        }

        if (newMastodonStatus != null) {
            allUnifiedPosts.add(0, new UnifiedPost(newMastodonStatus));
            Log.d(TAG, "Nuevo post de Mastodon añadido al timeline");
        }

        // Reordenar si es necesario
        if (allUnifiedPosts.size() > 1) {
            Collections.sort(allUnifiedPosts, (a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()));
        }

        if (callback != null) {
            callback.onPostsLoaded(new ArrayList<>(allUnifiedPosts), 1);
        }
    }

    /**
     * Refresca el timeline (equivalente a pull-to-refresh)
     */
    public void refreshTimeline() {
        Log.d(TAG, "Refrescando timeline completo");
        loadInitialTimeline();
    }

    // Getters para estado
    public boolean isLoading() {
        return isLoading;
    }

    public boolean hasMorePosts() {
        return hasMoreMisskey || hasMoreMastodon;
    }

    public int getTotalPostsCount() {
        return allUnifiedPosts.size();
    }

    public List<UnifiedPost> getAllPosts() {
        return new ArrayList<>(allUnifiedPosts);
    }

    public String getTimelineStats() {
        long misskeyCount = allUnifiedPosts.stream()
                .filter(UnifiedPost::isMisskey)
                .count();
        long mastodonCount = allUnifiedPosts.stream()
                .filter(UnifiedPost::isMastodon)
                .count();

        return String.format("Timeline: %d 🗾 + %d 🐘 = %d total",
                misskeyCount, mastodonCount, allUnifiedPosts.size());
    }

    private void notifyLoadingState(boolean loading) {
        if (callback != null) {
            callback.onLoadingStateChanged(loading);
        }
    }

    /**
     * Debug: Muestra información de paginación
     */
    public void debugPaginationState() {
        Log.d(TAG, "=== Estado de Paginación ===");
        Log.d(TAG, "Is loading: " + isLoading);
        Log.d(TAG, "Has more Misskey: " + hasMoreMisskey + " (lastId: " + lastMisskeyNoteId + ")");
        Log.d(TAG, "Has more Mastodon: " + hasMoreMastodon + " (lastId: " + lastMastodonStatusId + ")");
        Log.d(TAG, "Total posts: " + allUnifiedPosts.size());
        Log.d(TAG, getTimelineStats());
        Log.d(TAG, "=========================");
    }
}
