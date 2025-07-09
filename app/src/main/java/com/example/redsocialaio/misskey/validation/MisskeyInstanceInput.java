package com.example.redsocialaio.misskey.validation;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.redsocialaio.R;
import com.example.redsocialaio.misskey.auth.MisskeyAuthManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONObject;

/*Clase que se encarga de recibir el input del usuario para ver si la instancia que quiere ocupar
  es valida.*/

public class MisskeyInstanceInput extends AppCompatActivity {

    private EditText instanceURL;
    private FloatingActionButton sendButton;
    private String instanceToCheck; //Instancia que escribe el usuario.
    private TextView validInstance, InvalidInstance;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ask_instance);


        instanceURL = findViewById(R.id.editInstance);
        sendButton = findViewById(R.id.proceedButton);
        validInstance = findViewById(R.id.todoOk);
        InvalidInstance = findViewById(R.id.AlgoMal);


        sendButton.setOnClickListener(v -> {

            //asignamos a la variable el String que ingreso el usuario con la caja de texto.
            instanceToCheck = instanceURL.getText().toString().trim();

            //Si esta vacio... entonces avisamos  con una notificaion y se retorna.
            //TODO - La app deberia hacer otra cosa en vez de retornar simplemente.... no estoy seguro que todavia si.
            if (instanceToCheck.isEmpty()) {
                Toast.makeText(MisskeyInstanceInput.this, "empty field", Toast.LENGTH_SHORT).show();
                return;
            }


            MisskeyInstanceValidator.validateInstance(instanceToCheck, new ValidationCallBack() {


                public void onValidInstance(JSONObject meta) {
                    runOnUiThread(() -> {

                        //TODO - quitar texto debug y reemplazar por GUI como tal o texto mas descriptivo.
                        validInstance.setVisibility(View.VISIBLE);
                        InvalidInstance.setVisibility(View.GONE);

                        MisskeyAuthManager misskeyAuthManager = new MisskeyAuthManager(MisskeyInstanceInput.this);


                        misskeyAuthManager.startAuthFlow(instanceToCheck);

                    });
                }

                @Override
                public void onInvalidInstance(String reason) {
                    runOnUiThread(() -> {
                        InvalidInstance.setVisibility(View.VISIBLE);
                        validInstance.setVisibility(View.GONE);
                        Toast.makeText(MisskeyInstanceInput.this, "Error: " + reason, Toast.LENGTH_SHORT).show();
                    });
                }
            });

        });
    }
}
