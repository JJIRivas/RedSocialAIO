package com.example.redsocialaio.misskey;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.redsocialaio.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONObject;

import java.util.Objects;

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
        setContentView(R.layout.activity_misskey_ask_instance);

        //asignamos que es cada boton, texto y etc. Ubicados en el xml activity_misskey_ask_instance por ahora.
        instanceURL = findViewById(R.id.editInstance);
        sendButton = findViewById(R.id.proceedButton);
        validInstance = findViewById(R.id.todoOk);
        InvalidInstance = findViewById(R.id.AlgoMal);


        //Si se oprime el boton para enviar- pasa la magia (aqui esta el codigo de esta clase casi).
        sendButton.setOnClickListener(v -> {

            //asignamos a la variable el String que ingreso el usuario con la caja de texto.
            instanceToCheck = instanceURL.getText().toString().trim();

            //Si esta vacio... entonces avisamos  con una notificaion y se retorna.
            //TODO - La app deberia hacer otra cosa en vez de retornar simplemente.... no estoy seguro que todavia si.
            if (instanceToCheck.isEmpty()) {
                Toast.makeText(MisskeyInstanceInput.this, "empty field", Toast.LENGTH_SHORT).show();
                return;
            }

            /*Ocupamos la clase auxiliar de MisskeyInstanceValidator, pordemos llamarla sin crear
            un nuevo objeto porque el metodo validateInstance es static. Se ocupa para validar como
            tal, luego enviando al navegador para que el usuario inicie sesion, despues guardando
            los datos.*/

            /*Nota: runOnUiThread es una manera de ejecutar algo en el proceso principal de la
            aplicacion, es decir, donde se ejecutan la mayoria de las cosas (NO la clase mainActivity,
            sino el proceso que ocupa el programa en si) Esto se hace porque el proceso principal es
            el unico que puede modificar el GUI de manera mas drasticas.*/
            MisskeyInstanceValidator.validateInstance(instanceToCheck, new ValidationCallBack() {

                /*Sorpresa- aqui se aplica el onValidInstance de MisskeyInstanceValidator. Revisa esa
                clase si no tienes idea de lo que estoy diciendo aqui.*/
                @Override
                public void onValidInstance(JSONObject meta) {
                    runOnUiThread(() -> {

                        //Esto de VISIBLE y GONE es solo un texto en el xml que avisa si se ejecuto bien, solo para debug.
                        //TODO - quitar texto debug y reemplazar por GUI como tal o texto mas descriptivo.
                        validInstance.setVisibility(View.VISIBLE);
                        InvalidInstance.setVisibility(View.GONE);

                        //Creamos un objeto de MisskeyAuthManager, que se especifica que fue desde aqui (eso indica el Context), junto la instancia que tenemos.
                        MisskeyAuthManager misskeyAuthManager = new MisskeyAuthManager(MisskeyInstanceInput.this, instanceToCheck);

                        /*Se ejecuta el metodo startSession, el cual genera un UID aleatorio que
                        sirve como token *temporario*, solo para armorar el link donde se pedira al
                        usuario iniciar sesion.*/
                        misskeyAuthManager.startSession();
                        String authUrl = misskeyAuthManager.generateAuthUrl(); //se asigna el url para iniciar sesion a este String.

                        //Se guarda mediante sharedPreferences el token temporal y el Url para iniciar sesion.
                        SharedPreferences prefs = getSharedPreferences("misskey_auth", MODE_PRIVATE);
                        prefs.edit()
                                .putString("session_token", misskeyAuthManager.getSessionToken())
                                .putString("instance_url", instanceToCheck)
                                .apply();

                        /*Aqui declaramos e iniciamos un Intent que abre el navegador y
                        redirecciona a la url. Esto se logra con el Intent.ACTION_VIEW el cual ejecuta
                        una accion relevante para mostrar informacion segun el segundo parametro, ya que
                        le pasamos un Uri- abre una ventana del navegador... y ya que ese Uri tiene
                        el url, se abre el navegador en el url que queremos.*/

                        //Ver clase MisskeyCallbackActivity para lo que pasa despues de esto.
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(authUrl));
                        startActivity(browserIntent);


                    });
                }

                /*Si pasa algun error al validar la instancia, se muestra un texto y una notificacion
                ...Esto esta mas que nada porque el ide se quejaba de que ocupaba el onValidInstance
                sin ocupar el onInvalidInstance- se podria reemplezar por una accion mas relevante.*/
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
