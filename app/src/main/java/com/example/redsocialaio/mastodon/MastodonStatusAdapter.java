package com.example.redsocialaio.mastodon;

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
import com.example.redsocialaio.ui.MissUI;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MastodonStatusAdapter extends RecyclerView.Adapter<MastodonStatusAdapter.StatusViewHolder> {
    private static final String TAG = "MastodonStatusAdapter";

    public interface OnStatusInteractionListener {
        void onStatusUpdated(MastodonStatusTimeline status, int position);

        void onReblogDone(MastodonStatusTimeline newStatus);
    }

    private List<MastodonStatusTimeline> statuses = new ArrayList<>();
    private final Context context;
    private final MastodonStatusInteract statusInteract;
    private final SimpleDateFormat dateFormat;

    public MastodonStatusAdapter(Context context) {
        this.context = context;
        this.statusInteract = new MastodonStatusInteract(context);
        this.dateFormat = new SimpleDateFormat("dd/MM HH:mm", Locale.getDefault());
    }

    private OnStatusInteractionListener listener;

    public void setInteractionListener(OnStatusInteractionListener listener) {
        this.listener = listener;
    }

    public void setStatuses(List<MastodonStatusTimeline> statuses) {
        this.statuses = statuses;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public StatusViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_post, parent, false);
        return new StatusViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StatusViewHolder holder, int position) {
        MastodonStatusTimeline status = statuses.get(position);

        Log.d(TAG, "=== Mostrando status " + position + " ===");
        Log.d(TAG, "ID: " + status.getId());
        Log.d(TAG, "Es reblog: " + status.isRepost());
        Log.d(TAG, "Tiene reblog: " + (status.getReblog() != null));

        if (isReblog(status)) {
            bindReblog(holder, status);
        } else {
            bindNormalStatus(holder, status);
        }

        setupInteractionButtons(holder, status, position);
    }


    private boolean isReblog(MastodonStatusTimeline status) {
        return status.isRepost() && status.getReblog() != null;
    }

    private void bindReblog(StatusViewHolder holder, MastodonStatusTimeline status) {
        MastodonStatusTimeline original = status.getReblog();

        if (status.getUser() != null) {
            String reblogText = "🔁 " + status.getUser().getUserName() + " reblogueó";
            holder.postUser.setText(reblogText);
            holder.postUser.setTextSize(12);
            holder.postUser.setTypeface(null, Typeface.ITALIC);
        }

        displayStatusContent(holder, original);

        String quotedText = safe(status.getContent());
        String originalText = safe(original.getContent());

        if (originalText.isEmpty() && original.hasFiles()) {
            originalText = "";
        }

        if (!originalText.isEmpty() && !quotedText.isEmpty()) {
            holder.postText.setText(originalText + "\n\n💬 " + quotedText);
        } else if (!quotedText.isEmpty()) {
            holder.postText.setText("💬 " + quotedText);
        }
    }

    private void bindNormalStatus(StatusViewHolder holder, MastodonStatusTimeline status) {
        holder.postUser.setTextSize(14);
        holder.postUser.setTypeface(null, Typeface.BOLD);
        displayStatusContent(holder, status);
    }

    private String safe(String text) {
        return text != null ? text.trim() : "";
    }


    private void displayStatusContent(StatusViewHolder holder, MastodonStatusTimeline status) {
        logStatusInfo(status);
        displayText(holder, status);
        displayUserInfo(holder, (MastodonAccount) status.getUser());
        displayDate(holder, status.getCreatedAt());
        displayImage(holder, status.getFiles());
    }


    private void logStatusInfo(MastodonStatusTimeline status) {
        Log.d(TAG, "Mostrando ID: " + status.getId());
        Log.d(TAG, "Content: " + status.getContent());
        Log.d(TAG, "User: " + (status.getUser() != null ? status.getUser().getUserName() : "null"));
    }

    private void displayText(StatusViewHolder holder, MastodonStatusTimeline status) {
        String content = status.getContent();
        if (content == null || content.trim().isEmpty()) {
            if (status.hasFiles()) {
                holder.postText.setVisibility(View.GONE);
            } else {
                holder.postText.setVisibility(View.VISIBLE);
                holder.postText.setText("");
            }
        } else {
            holder.postText.setVisibility(View.VISIBLE);
            String cleanContent = content.replaceAll("<.*?>", "");
            holder.postText.setText(cleanContent);
        }
    }

    private void displayUserInfo(StatusViewHolder holder, MastodonAccount user) {
        if (user == null) return;

        if (holder.postUser.getTypeface() == null || !holder.postUser.getTypeface().isItalic()) {
            holder.postUser.setText("@" + user.getUserName());
        }

        if (holder.userAvatar != null && user.getAvatarURL() != null) {
            Glide.with(context)
                    .load(user.getAvatarURL())
                    .circleCrop()
                    .placeholder(R.drawable.ic_profile)
                    .error(R.drawable.ic_profile)
                    .into(holder.userAvatar);
        }

        String instance = user.getInstanceUrl();
        if (instance != null && !instance.isEmpty()) {
            instance = instance.replace("https://", "").replace("http://", "");
            holder.postInstance.setText(instance);
        } else {
            holder.postInstance.setText("mastodon");
        }
    }

    private void displayDate(StatusViewHolder holder, Date date) {
        holder.postDate.setText(date != null ? dateFormat.format(date) : "");
    }

    private void displayImage(StatusViewHolder holder, List<MastodonFile> files) {
        if (files == null || files.isEmpty()) {
            holder.postImage.setVisibility(View.GONE);
            return;
        }

        holder.postImage.setVisibility(View.VISIBLE);
        MastodonFile file = files.get(0);
        String imageUrl = file.getUrl();

        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_image)
                    .error(R.drawable.ic_image)
                    .into(holder.postImage);

            holder.postImage.setOnClickListener(v ->
                    Toast.makeText(context, "Imagen: " + imageUrl, Toast.LENGTH_SHORT).show()
            );
        }
    }


    private void setupInteractionButtons(StatusViewHolder holder, MastodonStatusTimeline status, int position) {
        setupButton(holder.likeButton, status.isFavourited(),
                android.R.color.holo_red_dark,
                () -> handleRemoveFavourite(status, position, holder),
                () -> handleAddFavourite(status, position, holder));

        setupButton(holder.repostButton, status.isReblogged(),
                android.R.color.holo_green_dark,
                () -> Toast.makeText(context, "Ya hiciste reblog de este post", Toast.LENGTH_SHORT).show(),
                () -> handleReblog(status, position, holder));

        setupButton(holder.bookmarkButton, status.isBookmarked(),
                android.R.color.holo_blue_dark,
                () -> Toast.makeText(context, "Bookmark (por implementar)", Toast.LENGTH_SHORT).show(),
                null); // No hay acción inversa implementada aún
    }

    private void setupButton(ImageView button, boolean isActive, int activeColorRes,
                             Runnable onAlreadyActive, Runnable onActivate) {
        if (button == null) return;

        if (isActive) {
            button.setColorFilter(context.getColor(activeColorRes));
        } else {
            button.clearColorFilter();
        }

        button.setOnClickListener(v -> {
            if (isActive) {
                if (onAlreadyActive != null) onAlreadyActive.run();
            } else {
                if (onActivate != null) onActivate.run();
            }
        });
    }


    private void handleAddFavourite(MastodonStatusTimeline status, int position, StatusViewHolder holder) {
        Log.d(TAG, "Añadiendo favourite a status: " + status.getId());

        statusInteract.likePost(status.getId())
                .thenAccept(success -> {
                    if (success) {
                        ((MissUI) context).runOnUiThread(() -> {
                            holder.likeButton.setColorFilter(context.getColor(android.R.color.holo_red_dark));
                            Toast.makeText(context, "¡Favorito añadido!", Toast.LENGTH_SHORT).show();
                            if (listener != null) {
                                listener.onStatusUpdated(status, position);
                            }
                        });
                    }
                })
                .exceptionally(e -> {
                    Log.e(TAG, "Error al añadir favourite", e);
                    ((MissUI) context).runOnUiThread(() -> {
                        Toast.makeText(context, "Error al dar favorito", Toast.LENGTH_SHORT).show();
                    });
                    return null;
                });
    }

    private void handleRemoveFavourite(MastodonStatusTimeline status, int position, StatusViewHolder holder) {
        Log.d(TAG, "Eliminando favourite de status: " + status.getId());

        statusInteract.removeFavourite(status.getId())
                .thenAccept(success -> {
                    if (success) {
                        ((MissUI) context).runOnUiThread(() -> {
                            holder.likeButton.clearColorFilter();
                            Toast.makeText(context, "Favorito eliminado", Toast.LENGTH_SHORT).show();
                            if (listener != null) {
                                listener.onStatusUpdated(status, position);
                            }
                        });
                    }
                })
                .exceptionally(e -> {
                    Log.e(TAG, "Error al eliminar favourite", e);
                    return null;
                });
    }

    private void handleReblog(MastodonStatusTimeline status, int position, StatusViewHolder holder) {
        Log.d(TAG, "Haciendo reblog de status: " + status.getId());

        statusInteract.reblog(status.getId())
                .thenAccept(newStatus -> {
                    ((MissUI) context).runOnUiThread(() -> {
                        holder.repostButton.setColorFilter(context.getColor(android.R.color.holo_green_dark));
                        Toast.makeText(context, "¡Reblog exitoso!", Toast.LENGTH_SHORT).show();
                        if (listener != null) {
                            listener.onReblogDone(newStatus);
                        }
                    });
                })
                .exceptionally(e -> {
                    Log.e(TAG, "Error al hacer reblog", e);
                    ((MissUI) context).runOnUiThread(() -> {
                        Toast.makeText(context, "Error al rebloguear", Toast.LENGTH_SHORT).show();
                    });
                    return null;
                });
    }

    @Override
    public int getItemCount() {
        return statuses.size();
    }

    static class StatusViewHolder extends RecyclerView.ViewHolder {
        TextView postUser, postInstance, postDate, postText;
        ImageView postImage;
        ImageView userAvatar; // El avatar en tu layout

        // Botones de interacción - usando los mismos IDs que Misskey
        ImageView likeButton;      // favourite en Mastodon
        ImageView repostButton;    // reblog en Mastodon
        ImageView replyButton;     // reply en Mastodon
        ImageView bookmarkButton;  // bookmark en Mastodon
        ImageView moreButton;

        public StatusViewHolder(@NonNull View itemView) {
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
}
