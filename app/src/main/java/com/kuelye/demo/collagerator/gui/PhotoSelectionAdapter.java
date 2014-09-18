package com.kuelye.demo.collagerator.gui;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kuelye.demo.collagerator.R;
import com.kuelye.demo.collagerator.instagram.InstagramMedia;
import com.squareup.picasso.Picasso;

import java.util.List;

class PhotoSelectionAdapter extends ArrayAdapter<InstagramMedia> {

    private final Activity                  mActivity;
    private final PhotoSelectionContext     mPhotoSelectionContext;
    private final int                       mRowResource;
    private final List<InstagramMedia>      mPhotos;

    public PhotoSelectionAdapter(Activity activity, PhotoSelectionContext photoSelectionContext
            , int rowResource, List<InstagramMedia> photos) {
        super(activity, rowResource, photos);

        mActivity = activity;
        mPhotoSelectionContext = photoSelectionContext;
        mRowResource = rowResource;
        mPhotos = photos;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View photoView = convertView;
        ViewHolder photoViewHolder;
        final InstagramMedia photo = mPhotos.get(position);

        if (photoView == null) {
            final LayoutInflater inflater = mActivity.getLayoutInflater();
            photoView = inflater.inflate(mRowResource, parent, false);

            photoViewHolder = new ViewHolder();
            photoViewHolder.photoImageView
                    = (ImageView) photoView.findViewById(R.id.photo_image_view);
            photoViewHolder.likesCountTextView
                    = (TextView) photoView.findViewById(R.id.likes_count_text_view);
            photoViewHolder.photoLayout
                    = (LinearLayout) photoView.findViewById(R.id.photo_layout);

            photoView.setTag(photoViewHolder);
        } else {
            photoViewHolder = (ViewHolder) photoView.getTag();
        }

        checkPhotoSelection(photoView, photo);
        mPhotoSelectionContext.checkPosition(position);

        photoViewHolder.likesCountTextView.setText(Integer.toString(photo.getLikesCount()));
        Picasso.with(mActivity)
                .load(photo.getThumbnailImageUrl())
                .into(photoViewHolder.photoImageView);


        return photoView;
    }

    // ------------------- PRIVATE -------------------

    static void checkPhotoSelection(View photoView, InstagramMedia photo) {
        ViewHolder rowViewHolder = (ViewHolder) photoView.getTag();

        if (photo.isSelected()) {
            rowViewHolder.photoLayout.setBackgroundResource(R.color.photo_layout_selected);
        } else {
            rowViewHolder.photoLayout.setBackgroundResource(R.color.photo_layout_unselected);
        }
    }

    // -------------------- INNER --------------------

    private static class ViewHolder {

        public LinearLayout photoLayout;
        public ImageView    photoImageView;
        public TextView     likesCountTextView;

    }

}