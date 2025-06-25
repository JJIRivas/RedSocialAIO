package com.example.redsocialaio.mastodon;

import androidx.appcompat.app.AppCompatActivity;

import social.bigbone.MastodonClient;
import social.bigbone.api.Scope;
import social.bigbone.api.entity.Application;
import social.bigbone.api.exception.BigBoneRequestException;

/*Clase hecha al principio del proyecto, no deberia ser necesaria ahora, pero la dejo por si el Mati
puede ocuparla de algo si es que.*/
public class MastodonInstanceCreator extends AppCompatActivity {
    private static final Scope fullScope = new Scope(Scope.READ.ALL, Scope.WRITE.ALL, Scope.PUSH.ALL);

    public static MastodonClient ClientCreator(String InstanceDomain) {
        try {
            MastodonClient client = new MastodonClient.Builder(InstanceDomain).build();


            Application appRegistration = client.apps().createApp(
                    "RedSocialAIO",
                    "redsocialaio://auth/callback",
                    "",
                    fullScope
            ).execute();

            return client;
        } catch (BigBoneRequestException e) {
            throw new RuntimeException("fuck");
        }
    }
}

