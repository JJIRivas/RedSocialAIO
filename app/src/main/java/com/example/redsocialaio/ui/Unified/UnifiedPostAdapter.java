package com.example.redsocialaio.ui.Unified;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.redsocialaio.R;
import com.example.redsocialaio.misskey.core.notes.MisskeyNoteInteract;
import com.example.redsocialaio.misskey.core.notes.MisskeyNoteTimeline;
import com.example.redsocialaio.misskey.core.notes.MisskeyFile;
import com.example.redsocialaio.mastodon.MastodonStatusInteract;
import com.example.redsocialaio.mastodon.MastodonStatusTimeline;
import com.example.redsocialaio.mastodon.MastodonFile;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class UnifiedPostAdapter extends RecyclerView.Adapter<UnifiedPostAdapter.UnifiedPostViewHolder> {
    private static final String TAG = "UnifiedPostAdapter";

    public interface OnUnifiedPostInteractionListener {
        void onMisskeyNoteUpdated(MisskeyNoteTimeline note, int position);

        void onMastodonStatusUpdated(MastodonStatusTimeline status, int position);

        void onMisskeyRenoteDone(MisskeyNoteTimeline newNote);

        void onMastodonReblogDone(MastodonStatusTimeline newStatus);
    }

    private List<UnifiedPost> posts = new ArrayList<>();
    private final Context context;
    private final MisskeyNoteInteract misskeyInteract;
    private final MastodonStatusInteract mastodonInteract;
    private final SimpleDateFormat dateFormat;
    private OnUnifiedPostInteractionListener listener;

    public UnifiedPostAdapter(Context context) {
        this.context = context;
        this.misskeyInteract = new MisskeyNoteInteract(context);
        this.mastodonInteract = new MastodonStatusInteract(context);
        this.dateFormat = new SimpleDateFormat("dd/MM HH:mm", Locale.getDefault());
    }

    public void setInteractionListener(OnUnifiedPostInteractionListener listener) {
        this.listener = listener;
    }

    public void setPosts(List<UnifiedPost> posts) {
        this.posts = posts;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UnifiedPostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_post, parent, false);
        return new UnifiedPostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UnifiedPostViewHolder holder, int position) {
        UnifiedPost post = posts.get(position);

        Log.d(TAG, "=== Mostrando post " + position + " ===");
        Log.d(TAG, "Platform: " + post.getPlatform() + " " + post.getPlatformEmoji());
        Log.d(TAG, "ID: " + post.getId());
        Log.d(TAG, "Es repost: " + post.isRepost());

        // Agregar indicador de plataforma al nombre de usuario
        if (post.isMisskey()) {
            bindMisskeyNote(holder, post.getMisskeyNote(), position);
        } else {
            bindMastodonStatus(holder, post.getMastodonStatus(), position);
        }
    }

    private void bindMisskeyNote(UnifiedPostViewHolder holder, MisskeyNoteTimeline note, int position) {
        if (note.isRepost() && note.getRenote() != null) {
            bindRenote(holder, note);
        } else {
            bindRegularMisskeyNote(holder, note);
        }

        setupMisskeyInteractionButtons(holder, note, position);
    }

    private void bindRenote(UnifiedPostViewHolder holder, MisskeyNoteTimeline note) {
        MisskeyNoteTimeline originalNote = note.getRenote();

        if (note.getUser() != null) {
            holder.postUser.setText("🗾🔁 " + note.getUser().getUserName() + " reposteó");
            holder.postUser.setTextSize(12);
            holder.postUser.setTypeface(null, Typeface.ITALIC);
        }

        displayMisskeyContent(holder, originalNote);
        quoteTextMisskey(holder, note, originalNote);
    }

    private void bindRegularMisskeyNote(UnifiedPostViewHolder holder, MisskeyNoteTimeline note) {
        holder.postUser.setTextSize(14);
        holder.postUser.setTypeface(null, Typeface.BOLD);
        displayMisskeyContent(holder, note);
    }

    private void quoteTextMisskey(UnifiedPostViewHolder holder, MisskeyNoteTimeline quote, MisskeyNoteTimeline original) {
        String quotedText = quote.getText();
        if (quotedText == null || quotedText.trim().isEmpty()) {
            holder.postText.setText("💬 ");
            return;
        }

        String originalText = original.getText() != null ? original.getText() : "";

        if (!originalText.isEmpty()) {
            holder.postText.setText(originalText + "\n\n💬 " + quotedText);
        } else {
            holder.postText.setText("💬 " + quotedText);
        }
    }

    private void bindMastodonStatus(UnifiedPostViewHolder holder, MastodonStatusTimeline status, int position) {
        if (status.isRepost() && status.getReblog() != null) {
            bindReblog(holder, status);
        } else {
            bindRegularStatus(holder, status);
        }

        setupMastodonInteractionButtons(holder, status, position);
    }

    private void bindReblog(UnifiedPostViewHolder holder, MastodonStatusTimeline status) {
        MastodonStatusTimeline original = status.getReblog();

        if (status.getUser() != null) {
            holder.postUser.setText("🐘🔁 " + status.getUser().getUserName() + " reblogueó");
            holder.postUser.setTextSize(12);
            holder.postUser.setTypeface(null, Typeface.ITALIC);
        }

        displayMastodonContent(holder, original);
        quotedTextMastodon(holder, status, original);
    }

    private void bindRegularStatus(UnifiedPostViewHolder holder, MastodonStatusTimeline status) {
        holder.postUser.setTextSize(14);
        holder.postUser.setTypeface(null, Typeface.BOLD);
        displayMastodonContent(holder, status);
    }

    private void quotedTextMastodon(UnifiedPostViewHolder holder, MastodonStatusTimeline status, MastodonStatusTimeline original) {
        String quotedText = status.getContent();
        if (quotedText == null || quotedText.trim().isEmpty()) {
            holder.postText.setText("💬 ");
            return;
        }
        String originalText = original.getContent() != null ? original.getContent() : "";

        if (!originalText.isEmpty()) {
            holder.postText.setText(originalText + "\n\n💬 " + quotedText);
        } else {
            holder.postText.setText("💬 " + quotedText);
        }
    }

    private void displayMisskeyContent(UnifiedPostViewHolder holder, MisskeyNoteTimeline note) {
        handleMisskeyTextContent(holder, note);
        handleMisskeyUserDetails(holder, note);
        handleMisskeyPostDate(holder, note);
        handleMisskeyPostImage(holder, note);
    }


    private void displayMastodonContent(UnifiedPostViewHolder holder, MastodonStatusTimeline status) {
        handleMastodonTextContent(holder, status);
        handleMastodonUserDetails(holder, status);
        handleMastodonPostDate(holder, status);
        handleMastodonPostImage(holder, status);
    }


    private void setupMisskeyInteractionButtons(UnifiedPostViewHolder holder, MisskeyNoteTimeline note, int position) {
        setupLikeButton(holder, note, position);
        setupRepostButton(holder, note, position);
    }

    private void setupLikeButton(UnifiedPostViewHolder holder, MisskeyNoteTimeline note, int position) {
        if (holder.likeButton == null) return;

        if (note.hasReacted()) {
            holder.likeButton.setColorFilter(context.getColor(android.R.color.holo_red_dark));
        } else {
            holder.likeButton.clearColorFilter();
        }

        holder.likeButton.setOnClickListener(v -> {
            if (note.hasReacted()) {
                handleMisskeyRemoveReaction(note, position, holder);
            } else {
                handleMisskeyAddReaction(note, position, holder);
            }
        });
    }

    private void setupRepostButton(UnifiedPostViewHolder holder, MisskeyNoteTimeline note, int position) {
        if (holder.repostButton == null) return;

        if (note.isRenoted()) {
            holder.repostButton.setColorFilter(context.getColor(android.R.color.holo_green_dark));
        } else {
            holder.repostButton.clearColorFilter();
        }

        holder.repostButton.setOnClickListener(v -> {
            if (!note.isRenoted()) {
                handleMisskeyRenote(note, position, holder);
            } else {
                Toast.makeText(context, "Ya hiciste renote de este post", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void setupMastodonInteractionButtons(UnifiedPostViewHolder holder, MastodonStatusTimeline status, int position) {
        setupMastodonFavouriteButton(holder, status, position);
        setupMastodonReblogButton(holder, status, position);
        setupMastodonBookmarkButton(holder, status);
    }

    private void setupMastodonFavouriteButton(UnifiedPostViewHolder holder, MastodonStatusTimeline status, int position) {
        if (holder.likeButton == null) return;

        if (status.isFavourited()) {
            holder.likeButton.setColorFilter(context.getColor(android.R.color.holo_red_dark));
        } else {
            holder.likeButton.clearColorFilter();
        }

        holder.likeButton.setOnClickListener(v -> {
            if (status.isFavourited()) {
                handleMastodonRemoveFavourite(status, position, holder);
            } else {
                handleMastodonAddFavourite(status, position, holder);
            }
        });
    }

    private void setupMastodonReblogButton(UnifiedPostViewHolder holder, MastodonStatusTimeline status, int position) {
        if (holder.repostButton == null) return;

        if (status.isReblogged()) {
            holder.repostButton.setColorFilter(context.getColor(android.R.color.holo_green_dark));
        } else {
            holder.repostButton.clearColorFilter();
        }

        holder.repostButton.setOnClickListener(v -> {
            if (!status.isReblogged()) {
                handleMastodonReblog(status, position, holder);
            } else {
                Toast.makeText(context, "Ya hiciste reblog de este post", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupMastodonBookmarkButton(UnifiedPostViewHolder holder, MastodonStatusTimeline status) {
        if (holder.bookmarkButton == null) return;

        if (status.isBookmarked()) {
            holder.bookmarkButton.setColorFilter(context.getColor(android.R.color.holo_blue_dark));
        } else {
            holder.bookmarkButton.clearColorFilter();
        }

        // Puedes agregar lógica aquí si después decides permitir añadir/quitar bookmarks
    }


    // Handlers para Misskey
    private void handleMisskeyAddReaction(MisskeyNoteTimeline note, int position, UnifiedPostViewHolder holder) {
        misskeyInteract.likePost(note.getId())
                .thenAccept(success -> {
                    if (success) {
                        ((UnifiedTimeline) context).runOnUiThread(() -> {
                            holder.likeButton.setColorFilter(context.getColor(android.R.color.holo_red_dark));
                            Toast.makeText(context, "🗾 ¡Like añadido!", Toast.LENGTH_SHORT).show();
                            if (listener != null) {
                                listener.onMisskeyNoteUpdated(note, position);
                            }
                        });
                    }
                });
    }

    private void handleMisskeyRemoveReaction(MisskeyNoteTimeline note, int position, UnifiedPostViewHolder holder) {
        misskeyInteract.removeReaction(note.getId())
                .thenAccept(success -> {
                    if (success) {
                        ((UnifiedTimeline) context).runOnUiThread(() -> {
                            holder.likeButton.clearColorFilter();
                            Toast.makeText(context, "🗾 Like eliminado", Toast.LENGTH_SHORT).show();
                            if (listener != null) {
                                listener.onMisskeyNoteUpdated(note, position);
                            }
                        });
                    }
                });
    }

    private void handleMisskeyRenote(MisskeyNoteTimeline note, int position, UnifiedPostViewHolder holder) {
        misskeyInteract.renote(note.getId())
                .thenAccept(newNote -> {
                    ((UnifiedTimeline) context).runOnUiThread(() -> {
                        holder.repostButton.setColorFilter(context.getColor(android.R.color.holo_green_dark));
                        Toast.makeText(context, "🗾 ¡Renote exitoso!", Toast.LENGTH_SHORT).show();
                        if (listener != null) {
                            listener.onMisskeyRenoteDone(newNote);
                        }
                    });
                });
    }

    // Handlers para Mastodon
    private void handleMastodonAddFavourite(MastodonStatusTimeline status, int position, UnifiedPostViewHolder holder) {
        mastodonInteract.likePost(status.getId())
                .thenAccept(success -> {
                    if (success) {
                        ((UnifiedTimeline) context).runOnUiThread(() -> {
                            holder.likeButton.setColorFilter(context.getColor(android.R.color.holo_red_dark));
                            Toast.makeText(context, "🐘 ¡Favorito añadido!", Toast.LENGTH_SHORT).show();
                            if (listener != null) {
                                listener.onMastodonStatusUpdated(status, position);
                            }
                        });
                    }
                });
    }

    private void handleMastodonRemoveFavourite(MastodonStatusTimeline status, int position, UnifiedPostViewHolder holder) {
        mastodonInteract.removeFavourite(status.getId())
                .thenAccept(success -> {
                    if (success) {
                        ((UnifiedTimeline) context).runOnUiThread(() -> {
                            holder.likeButton.clearColorFilter();
                            Toast.makeText(context, "🐘 Favorito eliminado", Toast.LENGTH_SHORT).show();
                            if (listener != null) {
                                listener.onMastodonStatusUpdated(status, position);
                            }
                        });
                    }
                });
    }

    private void handleMastodonReblog(MastodonStatusTimeline status, int position, UnifiedPostViewHolder holder) {
        mastodonInteract.reblog(status.getId())
                .thenAccept(newStatus -> {
                    ((UnifiedTimeline) context).runOnUiThread(() -> {
                        holder.repostButton.setColorFilter(context.getColor(android.R.color.holo_green_dark));
                        Toast.makeText(context, "🐘 ¡Reblog exitoso!", Toast.LENGTH_SHORT).show();
                        if (listener != null) {
                            listener.onMastodonReblogDone(newStatus);
                        }
                    });
                });
    }


    private void handleMastodonTextContent(UnifiedPostViewHolder holder, MastodonStatusTimeline status) {
        String content = status.getContent();

        if (content == null || content.trim().isEmpty()) {
            holder.postText.setVisibility(status.hasFiles() ? View.GONE : View.VISIBLE);
            holder.postText.setText("");
        } else {
            holder.postText.setVisibility(View.VISIBLE);
            String cleanContent = content.replaceAll("<.*?>", ""); // Eliminar HTML
            holder.postText.setText(cleanContent);
        }
    }

    private void handleMastodonUserDetails(UnifiedPostViewHolder holder, MastodonStatusTimeline status) {
        if (status.getUser() == null) return;

        if (holder.postUser.getTypeface() == null || !holder.postUser.getTypeface().isItalic()) {
            holder.postUser.setText("🐘 @" + status.getUser().getUserName());
        }

        if (holder.userAvatar != null && status.getUser().getAvatarURL() != null) {
            Glide.with(context)
                    .load(status.getUser().getAvatarURL())
                    .circleCrop()
                    .placeholder(R.drawable.ic_profile)
                    .error(R.drawable.ic_profile)
                    .into(holder.userAvatar);
        }

        String instanceUrl = status.getUser().getInstanceUrl();
        if (instanceUrl != null && !instanceUrl.isEmpty()) {
            instanceUrl = instanceUrl.replace("https://", "").replace("http://", "");
            holder.postInstance.setText(instanceUrl);
        } else {
            holder.postInstance.setText("mastodon");
        }
    }

    private void handleMastodonPostDate(UnifiedPostViewHolder holder, MastodonStatusTimeline status) {
        if (status.getCreatedAt() != null) {
            holder.postDate.setText(dateFormat.format(status.getCreatedAt()));
        }
    }

    private void handleMastodonPostImage(UnifiedPostViewHolder holder, MastodonStatusTimeline status) {
        if (status.hasFiles() && !status.getFiles().isEmpty()) {
            holder.postImage.setVisibility(View.VISIBLE);

            MastodonFile firstImage = status.getFiles().get(0);
            Glide.with(context)
                    .load(firstImage.getUrl())
                    .placeholder(R.drawable.ic_image)
                    .error(R.drawable.ic_image)
                    .into(holder.postImage);
        } else {
            holder.postImage.setVisibility(View.GONE);
        }
    }

    private void handleMisskeyTextContent(UnifiedPostViewHolder holder, MisskeyNoteTimeline note) {
        String content = note.getText();

        if (content == null || content.trim().isEmpty()) {
            holder.postText.setVisibility(note.hasFiles() ? View.GONE : View.VISIBLE);
            holder.postText.setText("");
        } else {
            holder.postText.setVisibility(View.VISIBLE);
            holder.postText.setText(content); // Misskey no usa HTML como Mastodon
        }
    }

    private void handleMisskeyUserDetails(UnifiedPostViewHolder holder, MisskeyNoteTimeline note) {
        if (note.getUser() == null) return;

        if (holder.postUser.getTypeface() == null || !holder.postUser.getTypeface().isItalic()) {
            holder.postUser.setText("🗾 @" + note.getUser().getUserName());
        }

        if (holder.userAvatar != null && note.getUser().getAvatarURL() != null) {
            Glide.with(context)
                    .load(note.getUser().getAvatarURL())
                    .circleCrop()
                    .placeholder(R.drawable.ic_profile)
                    .error(R.drawable.ic_profile)
                    .into(holder.userAvatar);
        }

        String instanceUrl = note.getUser().getInstanceUrl();
        if (instanceUrl != null && !instanceUrl.isEmpty()) {
            instanceUrl = instanceUrl.replace("https://", "").replace("http://", "");
            holder.postInstance.setText(instanceUrl);
        } else {
            holder.postInstance.setText("misskey");
        }
    }

    private void handleMisskeyPostDate(UnifiedPostViewHolder holder, MisskeyNoteTimeline note) {
        if (note.getCreatedAt() != null) {
            holder.postDate.setText(dateFormat.format(note.getCreatedAt()));
        }
    }

    private void handleMisskeyPostImage(UnifiedPostViewHolder holder, MisskeyNoteTimeline note) {
        if (note.hasFiles() && !note.getFiles().isEmpty()) {
            holder.postImage.setVisibility(View.VISIBLE);
            MisskeyFile firstImage = note.getFiles().get(0);

            Glide.with(context)
                    .load(firstImage.getUrl())
                    .placeholder(R.drawable.ic_image)
                    .error(R.drawable.ic_image)
                    .into(holder.postImage);

            holder.postImage.setOnClickListener(v -> {
                Toast.makeText(context, "Imagen Misskey: " + firstImage.getUrl(), Toast.LENGTH_SHORT).show();
            });
        } else {
            holder.postImage.setVisibility(View.GONE);
        }
    }


    @Override
    public int getItemCount() {
        return posts.size();
    }

    static class UnifiedPostViewHolder extends RecyclerView.ViewHolder {
        TextView postUser, postInstance, postDate, postText;
        ImageView postImage;
        ImageView userAvatar;

        // Botones de interacción
        ImageView likeButton;
        ImageView repostButton;
        ImageView replyButton;
        ImageView bookmarkButton;
        ImageView moreButton;

        public UnifiedPostViewHolder(@NonNull View itemView) {
            super(itemView);
            postUser = itemView.findViewById(R.id.postUser);
            postInstance = itemView.findViewById(R.id.postInstance);
            postDate = itemView.findViewById(R.id.postDate);
            postText = itemView.findViewById(R.id.postText);
            postImage = itemView.findViewById(R.id.postImage);
            userAvatar = itemView.findViewById(R.id.userAvatar);

            likeButton = itemView.findViewById(R.id.likeButton);
            replyButton = itemView.findViewById(R.id.replyButton);
            repostButton = itemView.findViewById(R.id.repostButton);
            bookmarkButton = itemView.findViewById(R.id.bookmarkButton);
            moreButton = itemView.findViewById(R.id.moreButton);
        }
    }

    //Que horrible es todo esto :sob:
}