package com.smartphoneappdev.wcd.alienalbum;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Arrays;


public class VideoAdapter extends BaseAdapter {

    private final Context mContext;
    private final AlienVideo[] videos;

    // 1
    public VideoAdapter(Context context, AlienVideo[] videos) {
        this.mContext = context;
        this.videos = videos;
    }

    // 2
    @Override
    public int getCount() {
        return videos.length;
    }

    // 3
    @Override
    public long getItemId(int position) {
        return 0;
    }

    // 4
    @Override
    public Object getItem(int position) {
        return null;
    }

    // 5
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // 1
        final AlienVideo video = videos[position];

        // 2
        if (convertView == null) {
            final LayoutInflater layoutInflater = LayoutInflater.from(mContext);
            convertView = layoutInflater.inflate(R.layout.linearlayout_video, null);
            // 3
            final ImageView imageView = (ImageView) convertView.findViewById(R.id.imageview_thumb);
            //final TextView commentTextView = (TextView) convertView.findViewById(R.id.textview_comment);
            final ImageView imageViewFavorite = (ImageView) convertView.findViewById(R.id.imageview_favorite);
            final ImageView imageViewPlay = (ImageView) convertView.findViewById(R.id.imageview_play);
            // 4
            //final ViewHolder viewHolder = new ViewHolder(commentTextView, imageView, imageViewFavorite, imageViewPlay);
            final ViewHolder viewHolder = new ViewHolder(imageView, imageViewFavorite, imageViewPlay);
            convertView.setTag(viewHolder);
        }

        final ViewHolder viewHolder = (ViewHolder) convertView.getTag();

        final String imageUrl = mContext.getString(R.string.server_dir)+ video.getThumbnailRef();
        Log.d("Video Adapter", "thumb URL: " + imageUrl);
        Picasso.with(mContext)
                .load(imageUrl)
                .networkPolicy(NetworkPolicy.OFFLINE)
                .into(viewHolder.imageView, new Callback() {
                    @Override
                    public void onSuccess() {
                        Log.v("Picasso","fetch image success in first time.");
                    }

                    @Override
                    public void onError() {
                        //Try again online if cache failed
                        Log.v("Picasso","Could not fetch image in first time...");
                        Picasso.with(mContext).load(imageUrl).networkPolicy(NetworkPolicy.NO_CACHE)
                                .error( R.drawable.image_not_found_icon )
                                .placeholder( R.drawable.progress_animation )
                                .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE).error(R.drawable.image_not_found_icon)
                                .into(viewHolder.imageView, new Callback() {

                                    @Override
                                    public void onSuccess() {
                                        Log.v("Picasso","fetch image success in try again.");
                                    }

                                    @Override
                                    public void onError() {
                                        Log.v("Picasso","Could not fetch image again...");
                                    }

                                });
                    }
                });

        //viewHolder.commentTextView.setText(video.getComment());
        viewHolder.imageViewFavorite.setImageResource(
                video.getIsFavourite() ? R.drawable.star_enabled : R.drawable.star_disabled);
        viewHolder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int[] intUsers = new int[videos.length];
                String[] stringArray = new String[videos.length];
                String[] stringComment = new String[videos.length];
                for(int i = 0; i<stringArray.length; i++){
                    stringArray[i] = videos[i].getVideoRef();
                    stringComment[i] = videos[i].getComment();
                    intUsers[i] = videos[i].getUserID();
                }
                Intent intent = new Intent(mContext, PlayVideo.class);
                intent.putExtra("CurrentVid", position);
                intent.putExtra("AllUsers", intUsers);
                intent.putExtra("AllVideos", stringArray);
                intent.putExtra("AllComments", stringComment);
                mContext.startActivity(intent);

            }
        });

        return convertView;

    }

    private class ViewHolder {
        //private final TextView commentTextView;
        private final ImageView imageView;
        private final ImageView imageViewPlay;
        private final ImageView imageViewFavorite;

        //public ViewHolder(TextView commentTextView, ImageView imageView, ImageView imageViewFavorite, ImageView imageViewPlay) {
        public ViewHolder(ImageView imageView, ImageView imageViewFavorite, ImageView imageViewPlay) {
            //this.commentTextView = commentTextView;
            this.imageView = imageView;
            this.imageViewPlay = imageViewPlay;
            this.imageViewFavorite = imageViewFavorite;
        }
    }

}