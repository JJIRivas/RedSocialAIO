package com.example.redsocialaio.misskey.core.notes;

import com.example.redsocialaio.misskey.conversion.MisskeyAccountMapper;
import com.example.redsocialaio.misskey.core.MisskeyAccount;

import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class MisskeyNoteTimeline extends MisskeyNoteBase {
    // INFO DEL AUTOR (completa para mostrar)
    private MisskeyAccount user;
    private String channelId;
    private int limit;
    private String sinceId;
    private String untilId;
    private int sinceDate;
    private int untilDate;
    private boolean allowPartial;
    private boolean includeMyRentoes;
    private boolean includeRenotedMyNotes;
    private boolean includeLocalRenotes;
    private boolean withFiles;
    private boolean withRenotes;

    // MULTIMEDIA
    @Nullable
    private final List<MisskeyFile> files;

    // ESTADÍSTICAS
    private int repliesCount = 0;
    private int renoteCount = 0;

    // REACCIONES
    @Nullable
    private Map<String, Integer> reactions;
    @Nullable
    private String myReaction; // Tu reacción si existe
    @Nullable
    private Map<String, String> customEmojis;

    // ESTADO DE INTERACCIÓN
    private boolean isRenoted = false;

    // CONTEXTO
    @Nullable
    private MisskeyNoteTimeline renote; // Si es repost
    @Nullable
    private MisskeyNoteTimeline reply;  // Si es respuesta

    // METADATOS
    @Nullable
    private final List<String> tags;

    public MisskeyNoteTimeline() {
        this.files = new ArrayList<>();
        this.tags = new ArrayList<>();
        this.customEmojis = new HashMap<>();
    }

    public static MisskeyNoteTimeline fromJSON(JSONObject json) throws JSONException {
        MisskeyNoteTimeline note = new MisskeyNoteTimeline();

        // Campos básicos
        note.id = json.getString("id");
        note.userId = json.getString("userId");
        note.text = json.optString("text", "");
        note.cw = json.optString("cw", null);

        // Parsear fecha
        String createdAtStr = json.getString("createdAt");
        note.createdAt = parseDate(createdAtStr);

        // Visibilidad
        String visibilityStr = json.optString("visibility", "public");
        note.visibility = Visibility.fromApiValue(visibilityStr);

        // Usuario
        JSONObject userJson = json.optJSONObject("user");
        if (userJson != null) {
            note.user = MisskeyAccountMapper.fromJSON(userJson);
        }

        // Estadísticas
        note.repliesCount = json.optInt("repliesCount", 0);
        note.renoteCount = json.optInt("renoteCount", 0);

        // Archivos
        JSONArray filesArray = json.optJSONArray("files");
        if (filesArray != null) {
            for (int i = 0; i < filesArray.length(); i++) {
                JSONObject fileJson = filesArray.getJSONObject(i);
                note.files.add(MisskeyFile.fromJSON(fileJson));
            }
        }

        // Reacciones (pueden ser Unicode o custom emojis :nombre:)
        JSONObject reactionsJson = json.optJSONObject("reactions");
        if (reactionsJson != null) {
            note.reactions = new HashMap<>();
            Iterator<String> keys = reactionsJson.keys();
            while (keys.hasNext()) {
                String reaction = keys.next(); // Puede ser "❤️" o ":blob_cat:"
                int count = reactionsJson.getInt(reaction);
                note.reactions.put(reaction, count);
            }
        }

        // Lista de emojis custom disponibles en la instancia
        JSONArray emojisArray = json.optJSONArray("emojis");
        if (emojisArray != null) {
            note.customEmojis = new HashMap<>();
            for (int i = 0; i < emojisArray.length(); i++) {
                JSONObject emojiJson = emojisArray.getJSONObject(i);
                String name = emojiJson.getString("name");
                String url = emojiJson.getString("url");
                note.customEmojis.put(name, url);
            }
        }

        // Estado de renote
        note.isRenoted = json.optBoolean("isRenoted", false);

        // Si es un renote
        JSONObject renoteJson = json.optJSONObject("renote");
        if (renoteJson != null) {
            note.renote = MisskeyNoteTimeline.fromJSON(renoteJson);
        }

        // Tags
        JSONArray tagsArray = json.optJSONArray("tags");
        if (tagsArray != null) {
            for (int i = 0; i < tagsArray.length(); i++) {
                note.tags.add(tagsArray.getString(i));
            }
        }

        return note;
    }

    // Getters
    public MisskeyAccount getUser() {
        return user;
    }

    public List<MisskeyFile> getFiles() {
        return files != null ? files : new ArrayList<>();
    }

    public int getRepliesCount() {
        return repliesCount;
    }

    public int getRenoteCount() {
        return renoteCount;
    }

    public Map<String, Integer> getReactions() {
        return reactions;
    }

    public String getMyReaction() {
        return myReaction;
    }

    public Map<String, String> getCustomEmojis() {
        return customEmojis;
    }

    public boolean isRenoted() {
        return isRenoted;
    }

    public MisskeyNoteTimeline getRenote() {
        return renote;
    }

    public List<String> getTags() {
        return tags != null ? tags : new ArrayList<>();
    }

    // Helper para parsear fecha
    private static Date parseDate(String dateStr) {
        // Misskey usa formato ISO 8601
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
        format.setTimeZone(TimeZone.getTimeZone("UTC"));

        try {
            return format.parse(dateStr);
        } catch (ParseException e) {
            // Fallback
            return new Date();
        }
    }

    // Helpers útiles
    public boolean hasFiles() {
        return files != null && !files.isEmpty();
    }

    public boolean hasContentWarning() {
        return cw != null && !cw.isEmpty();
    }

    public boolean isRepost() {
        return renote != null;
    }

    public boolean hasReacted() {
        return myReaction != null;
    }

    // Métodos específicos del timeline
    public int getTotalReactions() {
        if (reactions == null) return 0;
        return reactions.values().stream()
                .mapToInt(Integer::intValue)
                .sum();
    }

    // Helper para obtener URL de emoji custom
    public String getCustomEmojiUrl(String emojiName) {
        if (customEmojis == null) return null;
        // Si viene con :, quitarlos
        String cleanName = emojiName.replace(":", "");
        return customEmojis.get(cleanName);
    }

    // Helper para saber si una reacción es custom
    public boolean isCustomEmoji(String reaction) {
        return reaction != null && reaction.startsWith(":") && reaction.endsWith(":");
    }

    public String getContent() {
        return getText();
    }

}


