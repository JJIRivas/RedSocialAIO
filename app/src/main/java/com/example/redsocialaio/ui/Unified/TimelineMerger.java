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
 * Utilitario para intercalar posts de Misskey y Mastodon en un timeline unificado
 * Maneja la obtención, ordenamiento y combinación de ambas plataformas
 */
public class TimelineMerger {
    private static final String TAG = "TimelineMerger";

    private final Context context;
    private final MisskeyTimelineService misskeyService;
    private final MastodonTimelineService mastodonService;

    public TimelineMerger(Context context) {
        this.context = context;
        this.misskeyService = new MisskeyTimelineService(context);
        this.mastodonService = new MastodonTimelineService(context);
    }

    /**
     * Obtiene timeline unificado mezclando ambas plataformas
     *
     * @return CompletableFuture con lista de UnifiedPost ordenados por fecha
     */
    public CompletableFuture<List<UnifiedPost>> getUnifiedTimeline() {
        return getUnifiedTimeline(20, 20); // 20 posts de cada plataforma por defecto
    }

    /**
     * Obtiene timeline unificado con límites específicos
     *
     * @param misskeyLimit  Número de posts de Misskey a obtener
     * @param mastodonLimit Número de posts de Mastodon a obtener
     * @return CompletableFuture con lista unificada
     */
    public CompletableFuture<List<UnifiedPost>> getUnifiedTimeline(int misskeyLimit, int mastodonLimit) {
        Log.d(TAG, "Obteniendo timeline unificado - Misskey: " + misskeyLimit + ", Mastodon: " + mastodonLimit);

        // Crear requests para ambas plataformas
        MisskeyTimelineService.TimelineRequest misskeyRequest =
                new MisskeyTimelineService.TimelineRequest().withLimit(misskeyLimit);
        MastodonTimelineService.TimelineRequest mastodonRequest =
                new MastodonTimelineService.TimelineRequest().withLimit(mastodonLimit);

        // Ejecutar ambas operaciones en paralelo
        CompletableFuture<List<MisskeyNoteTimeline>> misskeyFuture =
                misskeyService.getHomeTimeline(misskeyRequest)
                        .exceptionally(e -> {
                            Log.w(TAG, "Error obteniendo timeline de Misskey", e);
                            return new ArrayList<>(); // Retornar lista vacía en caso de error
                        });

        CompletableFuture<List<MastodonStatusTimeline>> mastodonFuture =
                mastodonService.getHomeTimeline(mastodonRequest)
                        .exceptionally(e -> {
                            Log.w(TAG, "Error obteniendo timeline de Mastodon", e);
                            return new ArrayList<>(); // Retornar lista vacía en caso de error
                        });

        // Combinar resultados cuando ambos estén listos
        return CompletableFuture.allOf(misskeyFuture, mastodonFuture)
                .thenApply(v -> {
                    try {
                        List<MisskeyNoteTimeline> misskeyNotes = misskeyFuture.join();
                        List<MastodonStatusTimeline> mastodonStatuses = mastodonFuture.join();

                        Log.d(TAG, "Posts obtenidos - Misskey: " + misskeyNotes.size() +
                                ", Mastodon: " + mastodonStatuses.size());

                        return mergeAndSort(misskeyNotes, mastodonStatuses);

                    } catch (Exception e) {
                        Log.e(TAG, "Error procesando timeline unificado", e);
                        return new ArrayList<>();
                    }
                });
    }

    /**
     * Obtiene timeline público unificado (no requiere autenticación)
     *
     * @return CompletableFuture con lista unificada de posts públicos
     */
    public CompletableFuture<List<UnifiedPost>> getUnifiedPublicTimeline() {
        return getUnifiedPublicTimeline(20, 20);
    }

    /**
     * Obtiene timeline público unificado con límites específicos
     */
    public CompletableFuture<List<UnifiedPost>> getUnifiedPublicTimeline(int misskeyLimit, int mastodonLimit) {
        Log.d(TAG, "Obteniendo timeline público unificado");

        CompletableFuture<List<MisskeyNoteTimeline>> misskeyFuture =
                misskeyService.getHomeTimeline()
                        .exceptionally(e -> {
                            Log.w(TAG, "Error obteniendo timeline público de Misskey", e);
                            return new ArrayList<>();
                        });

        CompletableFuture<List<MastodonStatusTimeline>> mastodonFuture =
                mastodonService.getPublicTimeline()
                        .exceptionally(e -> {
                            Log.w(TAG, "Error obteniendo timeline público de Mastodon", e);
                            return new ArrayList<>();
                        });

        return CompletableFuture.allOf(misskeyFuture, mastodonFuture)
                .thenApply(v -> {
                    List<MisskeyNoteTimeline> misskeyNotes = misskeyFuture.join();
                    List<MastodonStatusTimeline> mastodonStatuses = mastodonFuture.join();

                    Log.d(TAG, "Posts públicos obtenidos - Misskey: " + misskeyNotes.size() +
                            ", Mastodon: " + mastodonStatuses.size());

                    return mergeAndSort(misskeyNotes, mastodonStatuses);
                });
    }

    /**
     * Combina y ordena posts de ambas plataformas por fecha
     *
     * @param misskeyNotes     Lista de posts de Misskey
     * @param mastodonStatuses Lista de posts de Mastodon
     * @return Lista unificada ordenada por fecha (más recientes primero)
     */
    private List<UnifiedPost> mergeAndSort(List<MisskeyNoteTimeline> misskeyNotes,
                                           List<MastodonStatusTimeline> mastodonStatuses) {

        List<UnifiedPost> unifiedPosts = new ArrayList<>();

        // Convertir posts de Misskey
        for (MisskeyNoteTimeline note : misskeyNotes) {
            unifiedPosts.add(new UnifiedPost(note));
        }

        // Convertir posts de Mastodon
        for (MastodonStatusTimeline status : mastodonStatuses) {
            unifiedPosts.add(new UnifiedPost(status));
        }

        // Ordenar por fecha (más recientes primero)
        Collections.sort(unifiedPosts, new Comparator<UnifiedPost>() {
            @Override
            public int compare(UnifiedPost a, UnifiedPost b) {
                // Comparar timestamps
                long timestampA = a.getTimestamp();
                long timestampB = b.getTimestamp();

                // Más recientes primero (orden descendente)
                return Long.compare(timestampB, timestampA);
            }
        });

        // Log para debug
        Log.d(TAG, "Timeline unificado creado: " + unifiedPosts.size() + " posts total");

        for (int i = 0; i < Math.min(5, unifiedPosts.size()); i++) {
            UnifiedPost post = unifiedPosts.get(i);
            Log.d(TAG, "Post " + i + ": " + post.toString());
        }

        return unifiedPosts;
    }

    /**
     * Añade un nuevo post al timeline y reordena
     *
     * @param unifiedPosts      Lista actual de posts
     * @param newMisskeyNote    Nuevo post de Misskey (puede ser null)
     * @param newMastodonStatus Nuevo post de Mastodon (puede ser null)
     * @return Lista actualizada y reordenada
     */
    public List<UnifiedPost> addNewPost(List<UnifiedPost> unifiedPosts,
                                        MisskeyNoteTimeline newMisskeyNote,
                                        MastodonStatusTimeline newMastodonStatus) {

        List<UnifiedPost> updatedPosts = new ArrayList<>(unifiedPosts);

        if (newMisskeyNote != null) {
            updatedPosts.add(0, new UnifiedPost(newMisskeyNote)); // Añadir al principio
            Log.d(TAG, "Nuevo post de Misskey añadido: " + newMisskeyNote.getId());
        }

        if (newMastodonStatus != null) {
            updatedPosts.add(0, new UnifiedPost(newMastodonStatus)); // Añadir al principio
            Log.d(TAG, "Nuevo post de Mastodon añadido: " + newMastodonStatus.getId());
        }

        // Reordenar por fecha
        Collections.sort(updatedPosts, (a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()));

        return updatedPosts;
    }

    /**
     * Filtra posts por plataforma
     *
     * @param unifiedPosts Lista de posts unificados
     * @param platformType Plataforma a filtrar
     * @return Lista filtrada
     */
    public List<UnifiedPost> filterByPlatform(List<UnifiedPost> unifiedPosts,
                                              UnifiedPost.PlatformType platformType) {
        List<UnifiedPost> filtered = new ArrayList<>();

        for (UnifiedPost post : unifiedPosts) {
            if (post.getPlatform() == platformType) {
                filtered.add(post);
            }
        }

        Log.d(TAG, "Posts filtrados por " + platformType + ": " + filtered.size());
        return filtered;
    }

    /**
     * Obtiene estadísticas del timeline unificado
     *
     * @param unifiedPosts Lista de posts
     * @return String con estadísticas formateadas
     */
    public String getTimelineStats(List<UnifiedPost> unifiedPosts) {
        long misskeyCount = 0;
        long mastodonCount = 0;

        for (UnifiedPost post : unifiedPosts) {
            if (post.isMisskey()) {
                misskeyCount++;
            } else {
                mastodonCount++;
            }
        }

        return String.format("Timeline: %d 🗾 + %d 🐘 = %d total",
                misskeyCount, mastodonCount, unifiedPosts.size());
    }

    /**
     * Verifica si el usuario tiene cuentas configuradas en ambas plataformas
     *
     * @return true si tiene ambas cuentas configuradas
     */
    public boolean hasBothAccountsConfigured() {
        // Verificar Misskey (asumo que usas Firebase Auth o similar)
        boolean hasMisskey = true; // TODO: Implementar verificación real

        // Verificar Mastodon (SharedPreferences)
        boolean hasMastodon = context.getSharedPreferences("mastodon_prefs", Context.MODE_PRIVATE)
                .getString("access_token", null) != null;

        Log.d(TAG, "Cuentas configuradas - Misskey: " + hasMisskey + ", Mastodon: " + hasMastodon);
        return hasMisskey && hasMastodon;
    }
}

