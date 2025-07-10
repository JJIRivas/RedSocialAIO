package com.example.redsocialaio;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.widget.Button;
import android.widget.TextView;

import com.example.redsocialaio.core.checkers.InstancePickerActivity;
import com.example.redsocialaio.firebase.auth.AccountCreation;
import com.example.redsocialaio.firebase.auth.AccountLogin;
import com.example.redsocialaio.ui.Unified.UnifiedTimeline;
import com.google.android.material.navigation.NavigationView;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import com.example.redsocialaio.databinding.ActivityMainBinding;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) throws NullPointerException {
        super.onCreate(savedInstanceState);


        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            startActivity(new Intent(this, UnifiedTimeline.class));
            finish(); // Mata MainActivity para que no vuelva atrás
            return;
        } else {
            startActivity(new Intent(MainActivity.this, AccountLogin.class));
        }

        binding = ActivityMainBinding.inflate(getLayoutInflater());

////        setSupportActionBar(binding.appBarMain.toolbar);
//        binding.appBarMain.fab.setOnClickListener(view -> {
//            startActivity(new Intent(MainActivity.this, AccountLogin.class));
//
//        });

//        TextView create = findViewById(R.id.createAccountButton);
//        create.setOnClickListener(v -> {
//            startActivity(new Intent(MainActivity.this, AccountCreation.class));
//        });
//        TextView recover = findViewById(R.id.resetPasswordButton);
//        recover.setOnClickListener(v -> {
//            startActivity(new Intent(MainActivity.this, UnifiedTimeline.class));
//        });


        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;

        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow)
                .setOpenableLayout(drawer)
                .build();
        //NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
//        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
//        NavigationUI.setupWithNavController(navigationView, navController);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
}