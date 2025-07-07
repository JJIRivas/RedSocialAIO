package com.example.redsocialaio.core.security;

import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;

import java.nio.charset.StandardCharsets;
import java.security.KeyStore;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

public class CryptoManager {
    private static final String ANDROID_KEYSTORE = "AndroidKeyStore";
    private static final String KEY_ALIAS = "RedSocialAIOKey";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128;

    private final KeyStore keyStore;

    public CryptoManager() throws Exception {
        keyStore = KeyStore.getInstance(ANDROID_KEYSTORE);
        keyStore.load(null);

        if (!keyStore.containsAlias(KEY_ALIAS)) {
            generateKey();
        }
    }

    private void generateKey() throws Exception {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE
        );

        KeyGenParameterSpec keyGenParameterSpec = new KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT
        )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .build();

        keyGenerator.init(keyGenParameterSpec);
        keyGenerator.generateKey();
    }

    public String encrypt(String plainText) throws Exception {
        if (plainText == null) return null;

        SecretKey secretKey = (SecretKey) keyStore.getKey(KEY_ALIAS, null);
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);

        byte[] iv = cipher.getIV();
        byte[] encryption = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

        // Combinar IV + datos cifrados
        byte[] combined = new byte[iv.length + encryption.length];
        System.arraycopy(iv, 0, combined, 0, iv.length);
        System.arraycopy(encryption, 0, combined, iv.length, encryption.length);

        return Base64.encodeToString(combined, Base64.DEFAULT);
    }

    public String decrypt(String encryptedText) throws Exception {
        if (encryptedText == null) return null;

        byte[] combined = Base64.decode(encryptedText, Base64.DEFAULT);

        // Extraer IV (primeros 12 bytes para GCM)
        byte[] iv = new byte[12];
        byte[] cipherText = new byte[combined.length - 12];
        System.arraycopy(combined, 0, iv, 0, 12);
        System.arraycopy(combined, 12, cipherText, 0, cipherText.length);

        SecretKey secretKey = (SecretKey) keyStore.getKey(KEY_ALIAS, null);
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec);

        byte[] decryption = cipher.doFinal(cipherText);
        return new String(decryption, StandardCharsets.UTF_8);
    }
}
