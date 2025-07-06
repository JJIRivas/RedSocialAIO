package com.example.redsocialaio;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class PermissionsActivity extends AppCompatActivity{

    @Override
    public void onCreate(Bundle savedInstancesState){
        super.onCreate(savedInstancesState);
        setContentView(R.layout.activity_permissions);

        CheckBox checkBox = findViewById(R.id.checkPermisosAceptados);
        Button continuarBtn = findViewById(R.id.btnContinuarLogin);

        continuarBtn.setEnabled(false);

        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            continuarBtn.setEnabled(isChecked);
        });

        continuarBtn.setOnClickListener(v -> {
            Toast.makeText(this, "Permisos Aceptados", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}
