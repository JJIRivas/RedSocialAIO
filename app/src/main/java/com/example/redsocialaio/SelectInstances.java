package com.example.redsocialaio;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SelectInstances extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_instances);

        Toast.makeText(this, "Selecciona las instancias", Toast.LENGTH_SHORT).show();
    }
}
