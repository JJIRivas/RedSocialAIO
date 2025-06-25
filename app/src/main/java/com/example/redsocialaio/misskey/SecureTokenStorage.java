package com.example.redsocialaio.misskey;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

/*Clase no implementada todavia, pero quiero que se encarge de guardar localmente el token del
usuario despues de cifrarlo (para evitar que otra cosa pueda acceder a este) - estoy viendo que
libreria de android sirve para esto todavia, ya que la que pensaba ocupar esta deprecada. De todas
formas, lo que voy a añadir es de la misma libreria de android, asi que no planeo que sea externo.*/

//Mati- una vez que este implementada podrias ver si se puede aplicar para mastodon igual - o hacer una clase similar para mastodon.
public class SecureTokenStorage {

    private static final String PREF_NAME = "secure_misskey_tokens";
    private static final String TOKEN_KEY = "access_token";
    private SharedPreferences securePrefs;

    public SecureTokenStorage(Context context) {
        try {
            String masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);

            SharedPreferences securePrefs = EncryptedSharedPreferences.create(
                    "secure_misskey_tokens",
                    masterKeyAlias,
                    context,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );

        } catch (Exception e) {
            throw new RuntimeException("No se pudo inicializar el almacenamiento cifrado", e);
        }
    }

    public void saveToken(String token) {
        securePrefs.edit().putString(TOKEN_KEY, token).apply();
    }

    public String getToken() {
        return securePrefs.getString(TOKEN_KEY, null);
    }

    public void clearToken() {
        securePrefs.edit().remove(TOKEN_KEY).apply();
    }
}
