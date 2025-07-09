package com.example.redsocialaio.misskey.core.notes;

import android.util.Log;

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

        parseBasicFields(json, note);
        parseUser(json, note);
        parseFiles(json, note);
        parseReactions(json, note);
        parseCustomEmojis(json, note);
        parseRenote(json, note);
        parseTags(json, note);

        return note;
    }

    private static void parseBasicFields(JSONObject json, MisskeyNoteTimeline note) throws JSONException {
        note.id = json.getString("id");
        note.userId = json.getString("userId");
        note.text = json.optString("text", "");
        note.cw = json.optString("cw", null);
        note.createdAt = parseDate(json.getString("createdAt"));
        note.visibility = Visibility.fromApiValue(json.optString("visibility", "public"));

        note.repliesCount = json.optInt("repliesCount", 0);
        note.renoteCount = json.optInt("renoteCount", 0);
        note.myReaction = json.optString("myReaction", null);
        note.isRenoted = json.optBoolean("isRenoted", false);

        Log.d("MisskeyParse", "Parseando note ID: " + note.id);
        Log.d("MisskeyParse", "Text: " + note.text);
    }

    private static void parseUser(JSONObject json, MisskeyNoteTimeline note) {
        JSONObject userJson = json.optJSONObject("user");
        if (userJson != null) {
            note.user = MisskeyAccountMapper.fromJSON(userJson);
        }
    }

    private static void parseFiles(JSONObject json, MisskeyNoteTimeline note) throws JSONException {
        JSONArray filesArray = json.optJSONArray("files");
        if (filesArray != null) {
            for (int i = 0; i < filesArray.length(); i++) {
                note.files.add(MisskeyFile.fromJSON(filesArray.getJSONObject(i)));
            }
        }
    }

    private static void parseReactions(JSONObject json, MisskeyNoteTimeline note) throws JSONException {
        JSONObject reactionsJson = json.optJSONObject("reactions");
        if (reactionsJson != null) {
            note.reactions = new HashMap<>();
            Iterator<String> keys = reactionsJson.keys();
            while (keys.hasNext()) {
                String reaction = keys.next();
                int count = reactionsJson.getInt(reaction);
                note.reactions.put(reaction, count);
            }
        }
    }

    private static void parseCustomEmojis(JSONObject json, MisskeyNoteTimeline note) throws JSONException {
        JSONArray emojisArray = json.optJSONArray("emojis");
        if (emojisArray != null) {
            note.customEmojis = new HashMap<>();
            for (int i = 0; i < emojisArray.length(); i++) {
                JSONObject emojiJson = emojisArray.getJSONObject(i);
                note.customEmojis.put(emojiJson.getString("name"), emojiJson.getString("url"));
            }
        }
    }

    private static void parseRenote(JSONObject json, MisskeyNoteTimeline note) {
        JSONObject renoteJson = json.optJSONObject("renote");
        if (renoteJson != null) {
            Log.d("MisskeyParse", "Parseando renote...");
            try {
                note.renote = MisskeyNoteTimeline.fromJSON(renoteJson);
                Log.d("MisskeyParse", "Renote parseado - Text: " +
                        (note.renote != null ? note.renote.getText() : "null"));
            } catch (Exception e) {
                Log.e("MisskeyParse", "Error parseando renote", e);
            }
        }
    }

    private static void parseTags(JSONObject json, MisskeyNoteTimeline note) throws JSONException {
        JSONArray tagsArray = json.optJSONArray("tags");
        if (tagsArray != null) {
            for (int i = 0; i < tagsArray.length(); i++) {
                note.tags.add(tagsArray.getString(i));
            }
        }
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
        // Misskey usa forma ISO 8601
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


