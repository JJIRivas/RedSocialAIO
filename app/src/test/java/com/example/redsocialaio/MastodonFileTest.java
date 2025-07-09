package com.example.redsocialaio;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import static org.junit.Assert.*;

import com.example.redsocialaio.mastodon.MastodonFile;

public class MastodonFileTest {

    //"camino feliz" con todos los datos
    @Test
    public void fromJSONOK() throws JSONException {
        // Arrange: Un JSON completo
        String jsonString = "{"
                + "\"id\": \"file123\","
                + "\"type\": \"image\","
                + "\"url\": \"https://instance.com/full.png\","
                + "\"preview_url\": \"https://instance.com/preview.png\","
                + "\"remote_url\": \"https://another.instance.com/full.png\","
                + "\"sensitive\": true,"
                + "\"description\": \"A beautiful sunset\","
                + "\"blurhash\": \"L6Pj0^jE.AyE_3t7t7R*soflWCj@\""
                + "}";
        JSONObject json = new JSONObject(jsonString);

        MastodonFile file = MastodonFile.fromJSON(json);


        assertEquals("file123", file.getId());
        assertEquals("image", file.getType());
        assertEquals("https://instance.com/full.png", file.getUrl());
        assertEquals("https://instance.com/preview.png", file.getPreviewUrl());
        assertTrue(file.isSensitive());
        assertEquals("A beautiful sunset", file.getDescription()); // Accedemos directamente o con getter si lo creas
        assertEquals("L6Pj0^jE.AyE_3t7t7R*soflWCj@", file.getBlurhash());
        assertTrue(file.isImage());
        assertTrue(file.hasDescription());
    }

    //Datos mínimos y valores por defecto
    @Test
    public void fromJSONMissingData() throws JSONException {

        String jsonString = "{"
                + "\"id\": \"file456\","
                + "\"type\": \"video\","
                + "\"url\": \"https://instance.com/video.mp4\""
                + "}";
        JSONObject json = new JSONObject(jsonString);

        MastodonFile file = MastodonFile.fromJSON(json);

        assertEquals("file456", file.getId());
        assertEquals("video", file.getType());
        assertEquals("https://instance.com/video.mp4", file.getUrl());

        // Caso especial: previewUrl debe tomar el valor de url si no está presente
        assertEquals("https://instance.com/video.mp4", file.getPreviewUrl());


        assertEquals("", file.getUrl());
        assertFalse(file.isSensitive());
        assertEquals("", file.getDescription());
        assertEquals("", file.getBlurhash());

        assertTrue(file.isVideo());
        assertFalse(file.hasDescription());
    }

    //Campo obligatorio faltante
    @Test
    public void fromJSONNoId() {
        //A este JSON le falta "id"
        String jsonString = "{"
                + "\"type\": \"audio\","
                + "\"url\": \"https://instance.com/audio.mp3\""
                + "}";

        // Verificamos que se lanza la excepción esperada
        assertThrows(JSONException.class, () -> {
            JSONObject json = new JSONObject(jsonString);
            MastodonFile.fromJSON(json);
        });
    }

    //Métodos de ayuda
    @Test
    public void helperMethods() {

        MastodonFile imageFile = new MastodonFile();
        imageFile.setType("image");
        imageFile.setDescription("Alt text");

        MastodonFile audioFile = new MastodonFile();
        audioFile.setType("audio");
        audioFile.setDescription("");


        assertTrue(imageFile.isImage());
        assertFalse(imageFile.isVideo());
        assertTrue(imageFile.hasDescription());

        assertTrue(audioFile.isAudio());
        assertFalse(audioFile.isImage());
        assertFalse(audioFile.hasDescription());
    }
}
