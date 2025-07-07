package com.example.redsocialaio;

import android.os.Bundle;
import android.view.View;
import android.view.Menu;


import com.google.android.material.navigation.NavigationView;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import com.example.redsocialaio.databinding.ActivityMainBinding;

/**
 * MainActivity - Actividad principal de la aplicación
 * 
 * Funciones:
 * - Configura la navegación entre fragmentos (Home, Gallery, Slideshow)
 * - Maneja el menú lateral (drawer)
 * - Configura el botón flotante para crear posts
 * - Gestiona las opciones del menú (logout, test)
 */
public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Configurar el layout principal
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Configurar toolbar
        setSupportActionBar(binding.appBarMain.toolbar);
        
        // Configurar botón flotante para crear posts
        binding.appBarMain.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                android.content.Intent intent = new android.content.Intent(MainActivity.this, PostActivity.class);
                startActivity(intent);
            }
        });
        
        // Configurar navegación lateral
        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        
        // Definir fragmentos principales
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home)
                .setOpenableLayout(drawer)
                .build();
                
        // Configurar controlador de navegación
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
        
        // Manejar clicks del menú lateral
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            
            if (id == R.id.nav_home) {
                navController.navigate(R.id.nav_home);
            } else if (id == R.id.nav_post) {
                android.content.Intent intent = new android.content.Intent(this, PostActivity.class);
                startActivity(intent);
            } else if (id == R.id.nav_profile) {
                navController.navigate(R.id.nav_profile);
            } else if (id == R.id.nav_logout) {
                logout();
            }
            
            drawer.closeDrawer(androidx.core.view.GravityCompat.START);
            return true;
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            logout();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    // Cerrar sesión y volver al login
    private void logout() {
        // Limpiar datos guardados
        getSharedPreferences("mastodon_prefs", MODE_PRIVATE)
            .edit()
            .clear()
            .apply();
        
        // Ir a pantalla de login
        android.content.Intent intent = new android.content.Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
}