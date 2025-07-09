package com.example.redsocialaio.mastodon;

import java.util.concurrent.CompletableFuture;

import android.content.Context;
import android.util.Log;

import com.example.redsocialaio.core.security.SecureTokenStorage;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONObject;

import okhttp3.*;

import java.io.IOException;

public class MastodonStatusInteract {

    private static final String TAG = "MastodonStatusInteract";
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final MediaType FORM = MediaType.parse("application/x-www-form-urlencoded");

    private final Context context;
    private final OkHttpClient client;
    private final SecureTokenStorage tokenStorage;
    private final MastodonAccountService mastodonAccountService;

    public MastodonStatusInteract(Context context) {
        this.context = context;
        this.client = new OkHttpClient();
        this.tokenStorage = new SecureTokenStorage(context, "mastodon");
        this.mastodonAccountService = new MastodonAccountService(context);
    }

    /**
     * Añade un favourite (like) a un status
     */
    public CompletableFuture<Boolean> addFavourite(String statusId) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        getAccountInfo().thenAccept(accountInfo -> {
            try {
                Request request = new Request.Builder()
                        .url(accountInfo.getInstanceUrl() + "/api/v1/statuses/" + statusId + "/favourite")
                        .post(RequestBody.create("", FORM)) // POST vacío
                        .addHeader("Authorization", "Bearer " + accountInfo.getToken())
                        .build();

                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.e(TAG, "Error añadiendo favourite", e);
                        future.complete(false);
                    }

                    @Override
                    public void onResponse(Call call, Response response) {
                        boolean success = response.isSuccessful();
                        response.close();
                        future.complete(success);
                    }
                });

            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        }).exceptionally(e -> {
            future.completeExceptionally(e);
            return null;
        });

        return future;
    }

    /**
     * Elimina un favourite
     */
    public CompletableFuture<Boolean> removeFavourite(String statusId) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        getAccountInfo().thenAccept(accountInfo -> {
            try {
                Request request = new Request.Builder()
                        .url(accountInfo.getInstanceUrl() + "/api/v1/statuses/" + statusId + "/unfavourite")
                        .post(RequestBody.create("", FORM))
                        .addHeader("Authorization", "Bearer " + accountInfo.getToken())
                        .build();

                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        future.complete(false);
                    }

                    @Override
                    public void onResponse(Call call, Response response) {
                        future.complete(response.isSuccessful());
                        response.close();
                    }
                });

            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        });

        return future;
    }

    /**
     * Hace reblog (repost) de un status
     */
    public CompletableFuture<MastodonStatusTimeline> reblog(String statusId) {
        CompletableFuture<MastodonStatusTimeline> future = new CompletableFuture<>();

        getAccountInfo().thenAccept(accountInfo -> {
            try {
                Request request = new Request.Builder()
                        .url(accountInfo.getInstanceUrl() + "/api/v1/statuses/" + statusId + "/reblog")
                        .post(RequestBody.create("", FORM))
                        .addHeader("Authorization", "Bearer " + accountInfo.getToken())
                        .build();

                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        future.completeExceptionally(e);
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        try {
                            if (!response.isSuccessful()) {
                                throw new IOException("Error: " + response.code());
                            }

                            String responseBody = response.body().string();
                            JSONObject result = new JSONObject(responseBody);

                            MastodonStatusTimeline status = MastodonStatusTimeline.fromJSON(result);
                            future.complete(status);

                        } catch (Exception e) {
                            future.completeExceptionally(e);
                        }
                    }
                });

            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        });
        return future;
    }

    /**
     * Crea un post simple
     */
    public CompletableFuture<MastodonStatusTimeline> createPost(String text) {
        return createPost(text, null, "public");
    }

    /**
     * Crea un post con parámetros completos
     */
    public CompletableFuture<MastodonStatusTimeline> createPost(String text, String spoilerText, String visibility) {
        CompletableFuture<MastodonStatusTimeline> future = new CompletableFuture<>();

        getAccountInfo().thenAccept(accountInfo -> {
            try {
                // Crear form data
                FormBody.Builder formBuilder = new FormBody.Builder()
                        .add("status", text)
                        .add("visibility", visibility);

                if (spoilerText != null && !spoilerText.isEmpty()) {
                    formBuilder.add("spoiler_text", spoilerText);
                }

                RequestBody formBody = formBuilder.build();

                Request request = new Request.Builder()
                        .url(accountInfo.getInstanceUrl() + "/api/v1/statuses")
                        .post(formBody)
                        .addHeader("Authorization", "Bearer " + accountInfo.getToken())
                        .build();

                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        future.completeExceptionally(e);
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        try {
                            if (!response.isSuccessful()) {
                                throw new IOException("Error: " + response.code());
                            }

                            String responseBody = response.body().string();
                            JSONObject result = new JSONObject(responseBody);

                            MastodonStatusTimeline status = MastodonStatusTimeline.fromJSON(result);
                            future.complete(status);

                        } catch (Exception e) {
                            future.completeExceptionally(e);
                        }
                    }
                });

            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        });
        return future;
    }

    /**
     * Toggle favourite - similar al toggle reaction de Misskey
     */
    public CompletableFuture<Boolean> toggleFavourite(String statusId, boolean currentlyFavourited) {
        if (currentlyFavourited) {
            return removeFavourite(statusId);
        } else {
            return addFavourite(statusId);
        }
    }

    /**
     * Helper para like simple (equivalente al likePost de Misskey)
     */
    public CompletableFuture<Boolean> likePost(String statusId) {
        return addFavourite(statusId);
    }

    /**
     * Unreblog un status
     */
    public CompletableFuture<MastodonStatusTimeline> unreblog(String statusId) {
        CompletableFuture<MastodonStatusTimeline> future = new CompletableFuture<>();

        getAccountInfo().thenAccept(accountInfo -> {
            try {
                Request request = new Request.Builder()
                        .url(accountInfo.getInstanceUrl() + "/api/v1/statuses/" + statusId + "/unreblog")
                        .post(RequestBody.create("", FORM))
                        .addHeader("Authorization", "Bearer " + accountInfo.getToken())
                        .build();

                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        future.completeExceptionally(e);
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        try {
                            if (!response.isSuccessful()) {
                                throw new IOException("Error: " + response.code());
                            }

                            String responseBody = response.body().string();
                            JSONObject result = new JSONObject(responseBody);

                            MastodonStatusTimeline status = MastodonStatusTimeline.fromJSON(result);
                            future.complete(status);

                        } catch (Exception e) {
                            future.completeExceptionally(e);
                        }
                    }
                });

            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        });
        return future;
    }

    /**
     * Helper para obtener token de acceso
     */
    private CompletableFuture<MastodonAccountService.AccountWithToken> getAccountInfo() {
        CompletableFuture<MastodonAccountService.AccountWithToken> future = new CompletableFuture<>();

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser == null) {
            future.completeExceptionally(new Exception("No autenticado"));
            return future;
        }

        mastodonAccountService.getAccountWithToken(firebaseUser.getUid())
                .addOnSuccessListener(future::complete)
                .addOnFailureListener(future::completeExceptionally);

        return future;
    }
}