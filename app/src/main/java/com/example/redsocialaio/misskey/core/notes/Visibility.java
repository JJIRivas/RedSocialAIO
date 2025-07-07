package com.example.redsocialaio.misskey.core.notes;

public enum Visibility {
    PUBLIC("public"),
    HOME("home"),
    FOLLOWERS("followers"),
    SPECIFIED("specified");

    private final String apiValue;

    Visibility(String apiValue) {
        this.apiValue = apiValue;
    }

    // Para enviar a la API
    public String toApiValue() {
        return apiValue;
    }

    // Para parsear desde la API
    public static Visibility fromApiValue(String value) {
        for (Visibility v : values()) {
            if (v.apiValue.equals(value)) {
                return v;
            }
        }
        return PUBLIC; // default
    }
}
