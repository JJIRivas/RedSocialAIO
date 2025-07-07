package com.example.redsocialaio.core.security;

import android.content.Context;
import android.util.Log;

import androidx.datastore.preferences.core.MutablePreferences;
import androidx.datastore.preferences.core.Preferences;
import androidx.datastore.preferences.core.PreferencesKeys;
import androidx.datastore.preferences.rxjava3.RxPreferenceDataStoreBuilder;
import androidx.datastore.rxjava3.RxDataStore;

import java.util.concurrent.CompletableFuture;

import io.reactivex.rxjava3.core.Single;

/**
 * Almacenamiento seguro de tokens usando DataStore con cifrado
 * Compatible con Java mediante RxJava3
 */
public class SecureTokenStorage {
    private static final String TAG = "SecureTokenStorage";
    private static final String DATASTORE_NAME = "secure_tokens";

    private final RxDataStore<Preferences> dataStore;
    private final CryptoManager cryptoManager;
    private final String networkPrefix;

    public SecureTokenStorage(Context context, String networkType) {
        this.networkPrefix = networkType.toLowerCase();

        // Usar instancia compartida de DataStore (singleton)
        this.dataStore = DataStoreHolder.get(context);

        try {
            this.cryptoManager = new CryptoManager();
        } catch (Exception e) {
            Log.e(TAG, "Error inicializando CryptoManager", e);
            throw new RuntimeException("No se pudo inicializar el cifrado", e);
        }
    }

    /**
     * Guarda el token de forma segura y cifrada
     *
     * @return CompletableFuture para compatibilidad con Java
     */
    public CompletableFuture<Void> saveToken(String userId, String token) {
        if (userId == null || token == null) {
            return CompletableFuture.completedFuture(null);
        }

        CompletableFuture<Void> future = new CompletableFuture<>();

        try {
            String encryptedToken = cryptoManager.encrypt(token);
            String key = buildKey(userId, "token");

            dataStore.updateDataAsync(preferences -> {
                MutablePreferences mutablePreferences = preferences.toMutablePreferences();
                mutablePreferences.set(
                        PreferencesKeys.stringKey(key),
                        encryptedToken
                );
                return Single.just(mutablePreferences);
            }).subscribe(
                    preferences -> {
                        Log.d(TAG, "Token guardado para usuario: " + userId);
                        future.complete(null);
                    },
                    error -> {
                        Log.e(TAG, "Error guardando token", error);
                        future.completeExceptionally(error);
                    }
            );

        } catch (Exception e) {
            future.completeExceptionally(e);
        }

        return future;
    }

    /**
     * Guarda la URL de la instancia
     */
    public CompletableFuture<Void> saveInstanceUrl(String userId, String instanceUrl) {
        if (userId == null || instanceUrl == null) {
            return CompletableFuture.completedFuture(null);
        }

        CompletableFuture<Void> future = new CompletableFuture<>();
        String key = buildKey(userId, "instance");

        dataStore.updateDataAsync(preferences -> {
            MutablePreferences mutablePreferences = preferences.toMutablePreferences();
            mutablePreferences.set(
                    PreferencesKeys.stringKey(key),
                    instanceUrl  // No necesita cifrado
            );
            return Single.just(mutablePreferences);
        }).subscribe(
                preferences -> future.complete(null),
                error -> future.completeExceptionally(error)
        );

        return future;
    }

    /**
     * Obtiene el token descifrado
     *
     * @return Token o null si no existe
     */
    public String getToken(String userId) {
        if (userId == null) return null;

        try {
            String key = buildKey(userId, "token");

            // Bloquear para obtener valor (necesario en Java)
            Preferences preferences = dataStore.data()
                    .blockingFirst();

            String encryptedToken = preferences.get(PreferencesKeys.stringKey(key));

            if (encryptedToken != null) {
                return cryptoManager.decrypt(encryptedToken);
            }

        } catch (Exception e) {
            Log.e(TAG, "Error obteniendo token", e);
        }

        return null;
    }

    /**
     * Obtiene la URL de la instancia
     */
    public String getInstanceUrl(String userId) {
        if (userId == null) return null;

        try {
            String key = buildKey(userId, "instance");

            Preferences preferences = dataStore.data()
                    .blockingFirst();

            return preferences.get(PreferencesKeys.stringKey(key));

        } catch (Exception e) {
            Log.e(TAG, "Error obteniendo instance URL", e);
        }

        return null;
    }

    /**
     * Versión asíncrona para obtener token (recomendada)
     */
    public CompletableFuture<String> getTokenAsync(String userId) {
        if (userId == null) {
            return CompletableFuture.completedFuture(null);
        }

        CompletableFuture<String> future = new CompletableFuture<>();
        String key = buildKey(userId, "token");

        dataStore.data().firstOrError()
                .subscribe(
                        preferences -> {
                            try {
                                String encryptedToken = preferences.get(
                                        PreferencesKeys.stringKey(key)
                                );

                                if (encryptedToken != null) {
                                    String decrypted = cryptoManager.decrypt(encryptedToken);
                                    future.complete(decrypted);
                                } else {
                                    future.complete(null);
                                }
                            } catch (Exception e) {
                                future.completeExceptionally(e);
                            }
                        },
                        error -> future.completeExceptionally(error)
                );

        return future;
    }

    /**
     * Verifica si existe un token para el usuario
     */
    public boolean hasToken(String userId) {
        return getToken(userId) != null;
    }

    /**
     * Elimina todos los datos del usuario
     */
    public CompletableFuture<Void> clearUserData(String userId) {
        if (userId == null) {
            return CompletableFuture.completedFuture(null);
        }

        CompletableFuture<Void> future = new CompletableFuture<>();

        dataStore.updateDataAsync(preferences -> {
            MutablePreferences mutablePreferences = preferences.toMutablePreferences();

            // Eliminar token e instance URL
            mutablePreferences.remove(PreferencesKeys.stringKey(buildKey(userId, "token")));
            mutablePreferences.remove(PreferencesKeys.stringKey(buildKey(userId, "instance")));

            return Single.just(mutablePreferences);
        }).subscribe(
                preferences -> {
                    Log.d(TAG, "Datos eliminados para usuario: " + userId);
                    future.complete(null);
                },
                error -> future.completeExceptionally(error)
        );

        return future;
    }

    /**
     * Elimina todos los tokens de esta red social
     */
    public CompletableFuture<Void> clearAllTokens() {
        CompletableFuture<Void> future = new CompletableFuture<>();

        dataStore.updateDataAsync(preferences -> {
            MutablePreferences mutablePreferences = preferences.toMutablePreferences();

            // Filtrar y eliminar todas las claves de esta red
            preferences.asMap().keySet().stream()
                    .filter(key -> key.getName().startsWith(networkPrefix + "_"))
                    .forEach(mutablePreferences::remove);

            return Single.just(mutablePreferences);
        }).subscribe(
                preferences -> {
                    Log.d(TAG, "Todos los tokens eliminados para: " + networkPrefix);
                    future.complete(null);
                },
                error -> future.completeExceptionally(error)
        );

        return future;
    }

    private String buildKey(String userId, String dataType) {
        return networkPrefix + "_" + userId + "_" + dataType;
    }

    /**
     * Clase interna estática que mantiene una sola instancia de DataStore
     */
    private static class DataStoreHolder {
        private static RxDataStore<Preferences> instance;

        public static synchronized RxDataStore<Preferences> get(Context context) {
            if (instance == null) {
                instance = new RxPreferenceDataStoreBuilder(
                        context.getApplicationContext(),
                        DATASTORE_NAME
                ).build();
            }
            return instance;
        }
    }
}
