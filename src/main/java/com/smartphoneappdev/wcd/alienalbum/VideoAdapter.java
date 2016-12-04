package com.smartphoneappdev.wcd.alienalbum;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

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
    public View getView(int position, View convertView, ViewGroup parent) {
        // 1
        final AlienVideo video = videos[position];

        // 2
        if (convertView == null) {
            final LayoutInflater layoutInflater = LayoutInflater.from(mContext);
            convertView = layoutInflater.inflate(R.layout.linearlayout_video, null);
            // 3
            final ImageView imageView = (ImageView) convertView.findViewById(R.id.imageview_thumb);
            final TextView commentTextView = (TextView) convertView.findViewById(R.id.textview_comment);
            final ImageView imageViewFavorite = (ImageView) convertView.findViewById(R.id.imageview_favorite);
            // 4
            final ViewHolder viewHolder = new ViewHolder(commentTextView, imageView, imageViewFavorite);
            convertView.setTag(viewHolder);
        }


        final ViewHolder viewHolder = (ViewHolder) convertView.getTag();
        //Picasso.with(mContext).load(mContext.getString(R.string.server_dir)+ video.getThumbnailRef() + ".jpg").into(viewHolder.imageView);
        viewHolder.commentTextView.setText(video.getComment());
        viewHolder.imageViewFavorite.setImageResource(
                video.getIsFavourite() ? R.drawable.star_enabled : R.drawable.star_disabled);
        return convertView;

    }

    private class ViewHolder {
        private final TextView commentTextView;
        private final ImageView imageView;
        private final ImageView imageViewFavorite;

        public ViewHolder(TextView commentTextView, ImageView imageView, ImageView imageViewFavorite) {
            this.commentTextView = commentTextView;
            this.imageView = imageView;
            this.imageViewFavorite = imageViewFavorite;
        }
    }
}