package com.example.redsocialaio.core.checkers;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.redsocialaio.R;

public class InstancePickerActivity extends AppCompatActivity {

    private Button checkMisskey, checkMastodon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instance_picker);

        checkMisskey = findViewById(R.id.checkMisskey);
        checkMastodon = findViewById(R.id.checkMastodon);


        // Mostrar/ocultar campos al marcar
        checkMisskey.setOnClickListener(v -> {
            lanzarValidador(InstanceValidator.PlatformType.MISSKEY);
        });

        checkMastodon.setOnClickListener(v -> {
            lanzarValidador(InstanceValidator.PlatformType.MASTODON);
        });

    }


    private void lanzarValidador(InstanceValidator.PlatformType platform) {
        Intent intent = new Intent(this, InstanceValidation.class);
        intent.putExtra("platform", platform.name());
        startActivity(intent);
    }
}

