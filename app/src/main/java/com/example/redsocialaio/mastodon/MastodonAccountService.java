package com.example.redsocialaio.mastodon;

import android.content.Context;

import com.example.redsocialaio.core.security.SecureTokenStorage;
import com.google.android.gms.tasks.Task;

import org.json.JSONObject;

import java.util.Objects;

public class MastodonAccountService {
    private final Context context;
    private final MastodonAccountRepository repository;
    private final SecureTokenStorage tokenStorage;

    public MastodonAccountService(Context context) {
        this.context = context;
        this.repository = new MastodonAccountRepository();
        this.tokenStorage = new SecureTokenStorage(context, "mastodon");
    }

    public Task<Void> saveAccountWithToken(String firebaseUid, JSONObject userJson,
                                           String token, String instanceUrl) {
        MastodonAccount account = MastodonAccountMapper.fromJSON(userJson);
        account.setInstanceUrl(instanceUrl);

        tokenStorage.saveToken(account.getUserID(), token);
        tokenStorage.saveInstanceUrl(account.getUserID(), instanceUrl);

        return repository.saveAccount(firebaseUid, account);
    }

    public Task<AccountWithToken> getAccountWithToken(String firebaseUid) {
        return repository.loadAccount(firebaseUid)
                .continueWith(task -> {
                    if (task.isSuccessful()) {
                        MastodonAccount account = task.getResult();
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

    public static class AccountWithToken {
        private final MastodonAccount account;
        private final String token;
        private final String instanceUrl;

        public AccountWithToken(MastodonAccount account, String token, String instanceUrl) {
            this.account = account;
            this.token = token;
            this.instanceUrl = instanceUrl;
        }

        public MastodonAccount getAccount() {
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

