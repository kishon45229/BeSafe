package com.project.BeSafe;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.project.BeSafe.GlideApp;
import com.project.BeSafe.R;

import java.util.List;

public class MediaAdapter extends RecyclerView.Adapter<MediaAdapter.MediaViewHolder> {

    private final Context context;
    private final List<String> mediaUrls;
    private static final String TAG = "MediaAdapter";

    public MediaAdapter(Context context, List<String> mediaUrls) {
        this.context = context;
        this.mediaUrls = mediaUrls;
    }

    @NonNull
    @Override
    public MediaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_media, parent, false);
        return new MediaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MediaViewHolder holder, int position) {
        String mediaUrl = mediaUrls.get(position);

        Log.d(TAG, "Loading media URL: " + mediaUrl);

        // Ensure mediaUrl is not null or empty
        if (mediaUrl != null && !mediaUrl.isEmpty()) {
            Log.d(TAG, "Media URL is valid");

            StorageReference storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(mediaUrl);
            storageRef.getMetadata().addOnSuccessListener(metadata -> {
                String contentType = metadata.getContentType();
                if (contentType != null && contentType.startsWith("video/")) {
                    Log.d(TAG, "Media is a video");

                    // Load video thumbnail and show play button
                    GlideApp.with(context)
                            .load(mediaUrl)
                            .thumbnail(0.1f)
                            .into(holder.imageView);

                    holder.playButton.setVisibility(View.VISIBLE);
                    holder.playButton.setOnClickListener(v -> {
                        Intent intent = new Intent(context, VideoPlayerActivity.class);
                        intent.putExtra("videoUrl", mediaUrl);
                        context.startActivity(intent);
                    });
                } else {
                    Log.d(TAG, "Media is an image");

                    // Load image
                    GlideApp.with(context)
                            .load(mediaUrl)
                            .into(holder.imageView);

                    holder.playButton.setVisibility(View.GONE);
                }
            }).addOnFailureListener(e -> {
                Log.e(TAG, "Failed to get metadata for URL: " + mediaUrl, e);
                holder.imageView.setImageDrawable(null);
                holder.playButton.setVisibility(View.GONE);
            });
        } else {
            Log.e(TAG, "Invalid media URL at position: " + position);

            holder.imageView.setImageDrawable(null);
            holder.playButton.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return mediaUrls.size();
    }

    static class MediaViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        ImageView playButton;

        public MediaViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageViewMedia);
            playButton = itemView.findViewById(R.id.playButton);
        }
    }
}
