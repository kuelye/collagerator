package com.kuelye.demo.collagerator.gui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kuelye.demo.collagerator.R;
import com.kuelye.demo.collagerator.instagram.InstagramMedia;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.kuelye.demo.collagerator.gui.SendCollageActivity.EXTRA_COLLAGE;

public class PhotoSelectionActivity extends Activity implements View.OnClickListener {

    private static final String TAG = "com.demo.collagerator.gui.PhotoSelectionActivity";

    public static final String EXTRA_PHOTOS = "EXTRA_PHOTOS";

    private List<InstagramMedia> mPhotos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.photo_selection_activity);

        Bundle extras = getIntent().getExtras();
        mPhotos = extras.getParcelableArrayList(EXTRA_PHOTOS);

        final GridView gridView = (GridView) findViewById(R.id.photos_grid_view);
        final InstagramMediaAdapter adapter = new InstagramMediaAdapter(
                this, R.layout.photo_selection_activity_row, mPhotos);
        gridView.setAdapter(adapter);

        final OnPhotoClickListener onItemClickListener = new OnPhotoClickListener();
        gridView.setOnItemClickListener(onItemClickListener);

        final Button photoSelectButton = (Button) findViewById(R.id.photo_select_button);
        photoSelectButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.photo_select_button: {
                Log.d("GUB", "!");
                new MergePhotosTask(this).execute(mPhotos);
            }
        }
    }

    private void checkPhotoSelection(View photoView, InstagramMedia photo) {
        ViewHolder rowViewHolder = (ViewHolder) photoView.getTag();

        if (photo.isSelected()) {
            rowViewHolder.photoLayout.setBackgroundResource(R.color.photo_layout_selected);
        } else {
            rowViewHolder.photoLayout.setBackgroundResource(R.color.photo_layout_unselected);
        }
    }

    // -------------------- INNER --------------------

    private class InstagramMediaAdapter extends ArrayAdapter<InstagramMedia> {

        private final Activity              mContext;
        private final int                   mRowResource;
        private final List<InstagramMedia>  mObjects;

        public InstagramMediaAdapter(Activity context, int rowResource, List<InstagramMedia> objects) {
            super(context, rowResource, objects);

            mContext = context;
            mRowResource = rowResource;
            mObjects = objects;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View imageView = convertView;
            ViewHolder rowViewHolder;
            final InstagramMedia photo = mObjects.get(position);

            if (imageView == null) {
                final LayoutInflater inflater = mContext.getLayoutInflater();
                imageView = inflater.inflate(mRowResource, parent, false);

                rowViewHolder = new ViewHolder();
                rowViewHolder.photoImageView
                        = (ImageView) imageView.findViewById(R.id.photo_image_view);
                rowViewHolder.likesCountTextView
                        = (TextView) imageView.findViewById(R.id.likes_count_text_view);
                rowViewHolder.photoLayout
                        = (LinearLayout) imageView.findViewById(R.id.photo_layout);

                imageView.setTag(rowViewHolder);
            } else {
                rowViewHolder = (ViewHolder) imageView.getTag();
            }
            checkPhotoSelection(imageView, photo);

            rowViewHolder.likesCountTextView.setText(Integer.toString(photo.getLikesCount()));
            Picasso.with(mContext)
                    .load(photo.getThumbnailImageUrl())
                    .into(rowViewHolder.photoImageView);

            return imageView;
        }

    }

    private class OnPhotoClickListener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            final InstagramMedia photo = mPhotos.get(position);
            photo.setSelected(!photo.isSelected());
            checkPhotoSelection(view, photo);
        }

    }

    private class ViewHolder {

        public LinearLayout photoLayout;
        public ImageView    photoImageView;
        public TextView     likesCountTextView;

    }

    private class MergePhotosTask extends AsyncTask<List<InstagramMedia>, Void, Bitmap> {

        private final Context mContext;

        public MergePhotosTask(Context context) {
            mContext = context;
        }

        @Override
        protected Bitmap doInBackground(List<InstagramMedia>... params) {
            final List<InstagramMedia> photos = params[0];

            try {
                List<Bitmap> bitmaps = new ArrayList<Bitmap>();
                for (InstagramMedia photo : photos) {
                    if (photo.isSelected()) {
                        bitmaps.add(Picasso.with(mContext)
                                .load(photo.getStandardResolutionImageUrl())
                                .get()
                        );
                    }
                }

                int size = bitmaps.get(0).getWidth();
                Bitmap collage = Bitmap.createBitmap(size * bitmaps.size()
                        , size, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(collage);
                Paint paint = new Paint();

                for (int i = 0, l = bitmaps.size(); i < l; ++i) {
                    canvas.drawBitmap(bitmaps.get(i), size * i, 0, paint);
                }

                return collage;
            } catch (IOException e) {
                Log.e(TAG, "", e);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Bitmap collage) {
            Intent intent = new Intent(PhotoSelectionActivity.this, SendCollageActivity.class);
            Bundle extras = new Bundle();
            extras.putParcelable(EXTRA_COLLAGE, collage);
            intent.putExtras(extras);
            startActivity(intent);
        }
    }



}
