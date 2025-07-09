package com.example.redsocialaio.ui.Unified;

import com.example.redsocialaio.misskey.core.notes.MisskeyNoteTimeline;
import com.example.redsocialaio.mastodon.MastodonStatusTimeline;
import com.example.redsocialaio.core.repositories.SocialAccountInfo;

import java.util.Date;

/**
 * Wrapper unificado que puede contener un post de Misskey o Mastodon
 * Permite tratarlos de forma uniforme manteniendo acceso al objeto original
 */
public class UnifiedPost {

    public enum PlatformType {
        MISSKEY, MASTODON
    }

    private final PlatformType platform;
    private final MisskeyNoteTimeline misskeyNote;
    private final MastodonStatusTimeline mastodonStatus;

    // Constructor para Misskey
    public UnifiedPost(MisskeyNoteTimeline misskeyNote) {
        this.platform = PlatformType.MISSKEY;
        this.misskeyNote = misskeyNote;
        this.mastodonStatus = null;
    }

    // Constructor para Mastodon
    public UnifiedPost(MastodonStatusTimeline mastodonStatus) {
        this.platform = PlatformType.MASTODON;
        this.misskeyNote = null;
        this.mastodonStatus = mastodonStatus;
    }

    // Getters
    public PlatformType getPlatform() {
        return platform;
    }

    public MisskeyNoteTimeline getMisskeyNote() {
        return misskeyNote;
    }

    public MastodonStatusTimeline getMastodonStatus() {
        return mastodonStatus;
    }

    public boolean isMisskey() {
        return platform == PlatformType.MISSKEY;
    }

    public boolean isMastodon() {
        return platform == PlatformType.MASTODON;
    }

    // Métodos unificados para acceso común
    public String getId() {
        return isMisskey() ? misskeyNote.getId() : mastodonStatus.getId();
    }

    public String getContent() {
        return isMisskey() ? misskeyNote.getText() : mastodonStatus.getContent();
    }

    public Date getCreatedAt() {
        return isMisskey() ? misskeyNote.getCreatedAt() : mastodonStatus.getCreatedAt();
    }

    public SocialAccountInfo getUser() {
        return isMisskey() ? misskeyNote.getUser() : mastodonStatus.getUser();
    }

    public boolean isRepost() {
        return isMisskey() ? misskeyNote.isRepost() : mastodonStatus.isRepost();
    }

    public boolean hasReacted() {
        return isMisskey() ? misskeyNote.hasReacted() : mastodonStatus.isFavourited();
    }

    public boolean isReblogged() {
        return isMisskey() ? misskeyNote.isRenoted() : mastodonStatus.isReblogged();
    }

    public boolean hasFiles() {
        return isMisskey() ? misskeyNote.hasFiles() : mastodonStatus.hasFiles();
    }

    public boolean hasContentWarning() {
        return isMisskey() ? misskeyNote.hasContentWarning() : mastodonStatus.hasContentWarning();
    }

    // Helper para comparación por fecha
    public long getTimestamp() {
        Date date = getCreatedAt();
        return date != null ? date.getTime() : 0;
    }

    // Helper para debug
    public String getPlatformEmoji() {
        return isMisskey() ? "🗾" : "🐘";
    }

    @Override
    public String toString() {
        return getPlatformEmoji() + " " + getId() + " by @" +
                (getUser() != null ? getUser().getUserName() : "unknown");
    }
}

