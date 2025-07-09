package com.example.redsocialaio;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.example.redsocialaio.R;
import com.example.redsocialaio.core.repositories.SocialAccountInfo;

/**
 * Activity para mostrar el perfil de cualquier usuario (Misskey o Mastodon)
 * Se puede abrir desde el click en avatares de posts
 */
public class UserProfileActivity extends AppCompatActivity {
    private static final String TAG = "UserProfileActivity";

    // Extras para el Intent
    private static final String EXTRA_USER_ID = "user_id";
    private static final String EXTRA_USERNAME = "username";
    private static final String EXTRA_DISPLAY_NAME = "display_name";
    private static final String EXTRA_AVATAR_URL = "avatar_url";
    private static final String EXTRA_BIO = "bio";
    private static final String EXTRA_POSTS_COUNT = "posts_count";
    private static final String EXTRA_FOLLOWERS_COUNT = "followers_count";
    private static final String EXTRA_FOLLOWING_COUNT = "following_count";
    private static final String EXTRA_PLATFORM = "platform";
    private static final String EXTRA_INSTANCE_URL = "instance_url";

    // UI Elements
    private ImageView profileAvatar;
    private TextView profileDisplayName;
    private TextView profileUsername;
    private TextView profileBio;
    private TextView profilePostsCount;
    private TextView profileFollowersCount;
    private TextView profileFollowingCount;
    private TextView profilePlatformIndicator;

    // Data
    private String userId;
    private String platform;

    /**
     * Método helper para abrir el perfil de un usuario
     */
    public static void openUserProfile(Context context, SocialAccountInfo userAccount) {
        Intent intent = new Intent(context, UserProfileActivity.class);

        // Pasar todos los datos del usuario
        intent.putExtra(EXTRA_USER_ID, userAccount.getUserID());
        intent.putExtra(EXTRA_USERNAME, userAccount.getUserName());
        intent.putExtra(EXTRA_DISPLAY_NAME, userAccount.getDisplayName());
        intent.putExtra(EXTRA_AVATAR_URL, userAccount.getAvatarURL());
        intent.putExtra(EXTRA_POSTS_COUNT, userAccount.getPostsCount());
        intent.putExtra(EXTRA_FOLLOWERS_COUNT, userAccount.getFollowersCount());
        intent.putExtra(EXTRA_FOLLOWING_COUNT, userAccount.getFollowingCount());
        intent.putExtra(EXTRA_PLATFORM, userAccount.getNetworkType());
        intent.putExtra(EXTRA_INSTANCE_URL, userAccount.getInstanceUrl());

        context.startActivity(intent);
    }

    /**
     * Método helper simplificado para usuarios con datos mínimos
     */
    public static void openUserProfile(Context context, String userId, String username,
                                       String displayName, String avatarUrl, String platform) {
        Intent intent = new Intent(context, UserProfileActivity.class);

        intent.putExtra(EXTRA_USER_ID, userId);
        intent.putExtra(EXTRA_USERNAME, username);
        intent.putExtra(EXTRA_DISPLAY_NAME, displayName);
        intent.putExtra(EXTRA_AVATAR_URL, avatarUrl);
        intent.putExtra(EXTRA_PLATFORM, platform);
        // El resto se cargará dinámicamente

        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        setupToolbar();
        initializeViews();
        loadUserDataFromIntent();
        setupInteractions();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setDisplayShowHomeEnabled(true);
                getSupportActionBar().setTitle("Perfil");
            }
        }
    }

    private void initializeViews() {
        profileAvatar = findViewById(R.id.profile_avatar);
        profileDisplayName = findViewById(R.id.profile_display_name);
        profileUsername = findViewById(R.id.profile_username);
        profileBio = findViewById(R.id.profile_bio);
        profilePostsCount = findViewById(R.id.profile_posts_count);
        profileFollowersCount = findViewById(R.id.profile_followers_count);
        profileFollowingCount = findViewById(R.id.profile_following_count);

        // Agregamos un TextView para mostrar la plataforma (opcional)
        profilePlatformIndicator = findViewById(R.id.profile_platform_indicator);
    }

    private void loadUserDataFromIntent() {
        Intent intent = getIntent();

        if (intent == null) {
            Log.e(TAG, "No intent data received");
            finish();
            return;
        }

        // Obtener datos básicos
        userId = intent.getStringExtra(EXTRA_USER_ID);
        platform = intent.getStringExtra(EXTRA_PLATFORM);
        String username = intent.getStringExtra(EXTRA_USERNAME);
        String displayName = intent.getStringExtra(EXTRA_DISPLAY_NAME);
        String avatarUrl = intent.getStringExtra(EXTRA_AVATAR_URL);
        String bio = intent.getStringExtra(EXTRA_BIO);
        String instanceUrl = intent.getStringExtra(EXTRA_INSTANCE_URL);

        // Obtener estadísticas
        long postsCount = intent.getLongExtra(EXTRA_POSTS_COUNT, 0);
        long followersCount = intent.getLongExtra(EXTRA_FOLLOWERS_COUNT, 0);
        long followingCount = intent.getLongExtra(EXTRA_FOLLOWING_COUNT, 0);

        Log.d(TAG, "Cargando perfil de: " + displayName + " (@" + username + ") - " + platform);

        // Mostrar datos en la UI
        displayUserInfo(userId, username, displayName, avatarUrl, bio, instanceUrl,
                postsCount, followersCount, followingCount, platform);

        // Si no tenemos todos los datos, cargar más información
        if (postsCount == 0 && followersCount == 0) {
            loadAdditionalUserInfo();
        }
    }

    private void displayUserInfo(String userId, String username, String displayName,
                                 String avatarUrl, String bio, String instanceUrl,
                                 long postsCount, long followersCount, long followingCount,
                                 String platform) {

        // Nombre y usuario
        profileDisplayName.setText(displayName != null && !displayName.isEmpty() ?
                displayName : username);

        // Username con indicador de plataforma
        String platformEmoji = "misskey".equals(platform) ? "🗾" : "🐘";
        profileUsername.setText(platformEmoji + " @" + username);

        // Avatar
        if (avatarUrl != null && !avatarUrl.isEmpty()) {
            Glide.with(this)
                    .load(avatarUrl)
                    .circleCrop()
                    .placeholder(R.drawable.ic_menu_gallery)
                    .error(R.drawable.ic_menu_gallery)
                    .into(profileAvatar);
        }

        // Biografía
        if (bio != null && !bio.isEmpty()) {
            // Limpiar HTML si es de Mastodon
            String cleanBio = bio.replaceAll("<.*?>", "").trim();
            profileBio.setText(cleanBio.isEmpty() ? "Sin biografía" : cleanBio);
        } else {
            profileBio.setText("Sin biografía");
        }

        // Estadísticas
        profilePostsCount.setText(String.valueOf(postsCount));
        profileFollowersCount.setText(String.valueOf(followersCount));
        profileFollowingCount.setText(String.valueOf(followingCount));

        // Información de instancia (si tenemos TextView para eso)
        if (profilePlatformIndicator != null && instanceUrl != null) {
            String cleanInstance = instanceUrl.replace("https://", "").replace("http://", "");
            profilePlatformIndicator.setText(cleanInstance);
        }

        // Actualizar título de la toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("@" + username);
        }
    }

    private void loadAdditionalUserInfo() {
        Log.d(TAG, "Cargando información adicional del usuario...");

        // TODO: requests a las APIs para obtener más info

        if ("mastodon".equals(platform)) {
            loadMastodonUserInfo();
        } else if ("misskey".equals(platform)) {
            loadMisskeyUserInfo();
        }
    }

    private void loadMastodonUserInfo() {
        // TODO: Implementar carga de info completa de Mastodon
        // MastodonApiService.getAccount(userId)
        Log.d(TAG, "Cargando info completa de Mastodon para usuario: " + userId);
    }

    private void loadMisskeyUserInfo() {
        // TODO: Implementar carga de info completa de Misskey
        Log.d(TAG, "Cargando info completa de Misskey para usuario: " + userId);
    }

    private void setupInteractions() {
        // Click en avatar para ver imagen completa
        profileAvatar.setOnClickListener(v -> {
            String avatarUrl = getIntent().getStringExtra(EXTRA_AVATAR_URL);
            if (avatarUrl != null && !avatarUrl.isEmpty()) {
                // TODO: Abrir imagen en fullscreen
                Toast.makeText(this, "Ver avatar completo: " + avatarUrl, Toast.LENGTH_SHORT).show();
            }
        });

        // Click en estadísticas (opcional)
        findViewById(R.id.profile_posts_count).setOnClickListener(v -> {
            Toast.makeText(this, "Ver posts del usuario (por implementar)", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.profile_followers_count).setOnClickListener(v -> {
            Toast.makeText(this, "Ver seguidores (por implementar)", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.profile_following_count).setOnClickListener(v -> {
            Toast.makeText(this, "Ver siguiendo (por implementar)", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    /**
     * Método helper para compartir perfil
     */
    private void shareProfile() {
        String username = getIntent().getStringExtra(EXTRA_USERNAME);
        String instanceUrl = getIntent().getStringExtra(EXTRA_INSTANCE_URL);

        if (username != null && instanceUrl != null) {
            String profileUrl = instanceUrl + "/@" + username;

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, "Mira este perfil: " + profileUrl);

            startActivity(Intent.createChooser(shareIntent, "Compartir perfil"));
        }
    }

    /**
     * Debug: Mostrar toda la información del perfil
     */
    private void debugProfileInfo() {
        Intent intent = getIntent();
        Log.d(TAG, "=== DEBUG PROFILE INFO ===");
        Log.d(TAG, "User ID: " + intent.getStringExtra(EXTRA_USER_ID));
        Log.d(TAG, "Username: " + intent.getStringExtra(EXTRA_USERNAME));
        Log.d(TAG, "Display Name: " + intent.getStringExtra(EXTRA_DISPLAY_NAME));
        Log.d(TAG, "Platform: " + intent.getStringExtra(EXTRA_PLATFORM));
        Log.d(TAG, "Instance: " + intent.getStringExtra(EXTRA_INSTANCE_URL));
        Log.d(TAG, "Posts: " + intent.getLongExtra(EXTRA_POSTS_COUNT, 0));
        Log.d(TAG, "Followers: " + intent.getLongExtra(EXTRA_FOLLOWERS_COUNT, 0));
        Log.d(TAG, "Following: " + intent.getLongExtra(EXTRA_FOLLOWING_COUNT, 0));
        Log.d(TAG, "========================");
    }
}
