package com.example.redsocialaio.misskey.core.notes;

import java.util.List;

public class MisskeyNoteDetail extends MisskeyNoteTimeline {
    private int reactionsCount;
    private int clippedCount;
    private boolean Favorited;
    private List<String> mentions;
    private String replyId;
    private String renoteId;
    private boolean hasPoll;

    /*    private MisskeyPoll poll;
    private MisskeyEmojis emojis;
    private MisskeyChannel channel;
        private MisskeyEmojis reactionEmojis;
    private MisskeyReaction reactions;
    private String channelId;*/
}
