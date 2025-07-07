package com.example.redsocialaio.misskey.userInfo;

import android.content.Context;

import com.example.redsocialaio.core.security.SecureTokenStorage;
import com.example.redsocialaio.misskey.core.MisskeyAccount;
import com.example.redsocialaio.misskey.conversion.MisskeyAccountMapper;
import com.example.redsocialaio.misskey.conversion.MisskeyAccountRepository;
import com.google.android.gms.tasks.*;

import org.json.JSONObject;

import java.util.Objects;

public class MisskeyAccountService {
    private final Context context;
    private final MisskeyAccountRepository repository;
    private final SecureTokenStorage tokenStorage;

    public MisskeyAccountService(Context context) {
        this.context = context;
        this.repository = new MisskeyAccountRepository();
        this.tokenStorage = new SecureTokenStorage(context, "misskey");
    }

    /**
     * Guarda la cuenta y el token después del login
     */
    public Task<Void> saveAccountWithToken(String firebaseUid, JSONObject userJson,
                                           String token, String instanceUrl) {
        // Crear cuenta desde JSON
        MisskeyAccount account = MisskeyAccountMapper.fromJSON(userJson);
        account.setInstanceUrl(instanceUrl);

        // Guardar token de forma segura
        tokenStorage.saveToken(account.getUserID(), token);
        tokenStorage.saveInstanceUrl(account.getUserID(), instanceUrl);

        // Guardar datos públicos en Firestore
        return repository.saveAccount(firebaseUid, account);
    }

    /**
     * Obtiene la cuenta con su token
     */
    public Task<AccountWithToken> getAccountWithToken(String firebaseUid) {
        return repository.loadAccount(firebaseUid)
                .continueWith(task -> {
                    if (task.isSuccessful()) {
                        MisskeyAccount account = task.getResult();
                        String token = tokenStorage.getToken(account.getUserID());
                        String instanceUrl = tokenStorage.getInstanceUrl(account.getUserID());

                        if (token == null) {
                            throw new Exception("Token no encontrado - reautenticación necesaria");
                        }

                        return new AccountWithToken(account, token, instanceUrl);
                    }
                    throw Objects.requireNonNull(task.getException());
                });
    }

    /**
     * Clase para retornar cuenta + token
     */
    public static class AccountWithToken {
        private final MisskeyAccount account;
        private final String token;
        private final String instanceUrl;

        public AccountWithToken(MisskeyAccount account, String token, String instanceUrl) {
            this.account = account;
            this.token = token;
            this.instanceUrl = instanceUrl;
        }

        public MisskeyAccount getAccount() {
            return account;
        }

        public String getToken() {
            return token;
        }

        public String getInstanceUrl() {
            return instanceUrl;
        }
    }
}