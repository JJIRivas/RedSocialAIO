package com.example.redsocialaio.misskey.core.notes;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import android.content.Context;
import android.util.Log;

import com.example.redsocialaio.core.security.SecureTokenStorage;
import com.example.redsocialaio.exceptions.SocialNetworkApiException;
import com.example.redsocialaio.exceptions.SocialNetworkAuthException;
import com.example.redsocialaio.misskey.core.notes.MisskeyNoteTimeline;
import com.example.redsocialaio.misskey.userInfo.MisskeyAccountService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONObject;

import okhttp3.*;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class MisskeyNoteInteract {

    private static final String TAG = "MisskeyNoteInteract";
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private final Context context;
    private final OkHttpClient client;
    private final SecureTokenStorage tokenStorage;
    private final MisskeyAccountService accountService;

    public MisskeyNoteInteract(Context context) {
        this.context = context;
        this.client = new OkHttpClient();
        this.tokenStorage = new SecureTokenStorage(context, "misskey");
        this.accountService = new MisskeyAccountService(context);
    }

    /**
     * Añade una reacción (like) a una nota
     */
    public CompletableFuture<Boolean> addReaction(String noteId, String reaction) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        getAccountInfo().thenAccept(accountInfo -> {
            try {

                if (accountInfo.getToken() == null || accountInfo.getToken().isEmpty()) {
                    throw new SocialNetworkAuthException(
                            SocialNetworkAuthException.AuthErrorType.TOKEN_INVALID,
                            "misskey",
                            "Token de acceso no encontrado"
                    );
                }

                JSONObject body = new JSONObject();
                body.put("i", accountInfo.getToken()); // TOKEN AQUÍ
                body.put("noteId", noteId);
                body.put("reaction", reaction); // Por defecto "❤️"

                Request request = new Request.Builder()
                        .url("https://" + accountInfo.getInstanceUrl() + "/api/notes/reactions/create")
                        .post(RequestBody.create(body.toString(), JSON))
                        .build();

                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        // Crear excepción específica para error de red
                        SocialNetworkApiException apiException = new SocialNetworkApiException(
                                SocialNetworkApiException.ApiErrorType.NETWORK_ERROR,
                                "misskey",
                                "/api/notes/reactions/create",
                                e
                        );
                        future.completeExceptionally(apiException);
                    }

                    @Override
                    public void onResponse(Call call, Response response) {
                        boolean success = response.isSuccessful();
                        response.close();
                        future.complete(success);
                    }
                });

            } catch (Exception e) {
                // Excepción genérica convertida a específica
                SocialNetworkApiException apiException = new SocialNetworkApiException(
                        SocialNetworkApiException.ApiErrorType.SERVER_ERROR,
                        "misskey",
                        "/api/notes/reactions/create",
                        e
                );
                future.completeExceptionally(apiException);
            }
        }).exceptionally(e -> {
            // Error obteniendo account info
            SocialNetworkAuthException authException = new SocialNetworkAuthException(
                    SocialNetworkAuthException.AuthErrorType.TOKEN_INVALID,
                    "misskey",
                    "No se pudo obtener información de la cuenta",
                    e
            );
            future.completeExceptionally(authException);
            return null;
        });

        return future;
    }

    /**
     * Elimina una reacción
     */
    public CompletableFuture<Boolean> removeReaction(String noteId) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        getAccountInfo().thenAccept(accountInfo -> {
            try {
                JSONObject body = new JSONObject();
                body.put("i", accountInfo.getToken());
                body.put("noteId", noteId);

                Request request = new Request.Builder()
                        .url("https://" + accountInfo.getInstanceUrl() + "/api/notes/reactions/delete")
                        .post(RequestBody.create(body.toString(), JSON))
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
     * Hace renote (repost) de una nota
     */
    public CompletableFuture<MisskeyNoteTimeline> renote(String noteId) {
        CompletableFuture<MisskeyNoteTimeline> future = new CompletableFuture<>();

        getAccountInfo().thenAccept(accountInfo -> {
            try {
                JSONObject body = new JSONObject();
                body.put("i", accountInfo.getToken());
                body.put("renoteId", noteId);

                Request request = new Request.Builder()
                        .url("https://" + accountInfo.getInstanceUrl() + "/api/notes/create")
                        .post(RequestBody.create(body.toString(), JSON))
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
                            JSONObject createdNote = result.getJSONObject("createdNote");

                            MisskeyNoteTimeline note = MisskeyNoteTimeline.fromJSON(createdNote);
                            future.complete(note);

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
    public CompletableFuture<MisskeyNoteTimeline> createPost(String text) {
        CompletableFuture<MisskeyNoteTimeline> future = new CompletableFuture<>();

        getAccountInfo().thenAccept(accountInfo -> {
            try {
                JSONObject body = new JSONObject();
                body.put("i", accountInfo.getToken());
                body.put("text", text);
                body.put("visibility", "public");

                Request request = new Request.Builder()
                        .url("https://" + accountInfo.getInstanceUrl() + "/api/notes/create")
                        .post(RequestBody.create(body.toString(), JSON))
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
                            JSONObject createdNote = result.getJSONObject("createdNote");

                            MisskeyNoteTimeline note = MisskeyNoteTimeline.fromJSON(createdNote);
                            future.complete(note);

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
     * Helper para like simple (siempre corazón)
     */
    public CompletableFuture<Boolean> likePost(String noteId) {
        return addReaction(noteId, "❤️");
    }

    /**
     * Helper para obtener info de cuenta y token
     */

    /**
     * Helper para obtener info de cuenta y token
     */
    private CompletableFuture<MisskeyAccountService.AccountWithToken> getAccountInfo() {
        CompletableFuture<MisskeyAccountService.AccountWithToken> future = new CompletableFuture<>();

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser == null) {
            future.completeExceptionally(new Exception("No autenticado"));
            return future;
        }

        accountService.getAccountWithToken(firebaseUser.getUid())
                .addOnSuccessListener(future::complete)
                .addOnFailureListener(future::completeExceptionally);

        return future;
    }
}
