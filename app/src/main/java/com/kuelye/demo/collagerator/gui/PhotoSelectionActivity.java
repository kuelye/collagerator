package com.kuelye.demo.collagerator.gui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.FileProvider;
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
import android.widget.Toast;

import com.kuelye.components.utils.IOUtils;
import com.kuelye.demo.collagerator.R;
import com.kuelye.demo.collagerator.instagram.InstagramMedia;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import static android.graphics.Bitmap.CompressFormat.JPEG;
import static com.kuelye.demo.collagerator.gui.SendCollageActivity.EXTRA_COLLAGE_FILE_URI;

public class PhotoSelectionActivity extends Activity implements View.OnClickListener, AdapterView.OnItemClickListener {

    public static final String EXTRA_PHOTOS = "EXTRA_PHOTOS";

    private static final String TAG = "com.demo.collagerator.gui.PhotoSelectionActivity";

    private static final String     COLLAGE_RELATIVE_FILE_NAME  = "collages/collage.jpg";
    private static final Bitmap.CompressFormat
                                    COLLAGE_COMPRESS_FORMAT     = JPEG;
    private static final int        COLLAGE_QUALITY             = 90;

    private static final String     FILE_PROVIDER_AUTHORITY     = "com.kuelye.demo.collagerator";

    private static final int        ERROR_EXCEPTION_CODE        = 1;

    private List<InstagramMedia>    mPhotos;
    private Button                  mPhotoSelectButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.photo_selection_activity);

        Bundle extras = getIntent().getExtras();
        mPhotos = extras.getParcelableArrayList(EXTRA_PHOTOS);

        final GridView gridView = (GridView) findViewById(R.id.photos_grid_view);
        final InstagramMediaAdapter adapter = new InstagramMediaAdapter(
                this, R.layout.photo_selection_row, mPhotos);
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(this);

        mPhotoSelectButton = (Button) findViewById(R.id.photo_select_button);
        mPhotoSelectButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.photo_select_button: {
                new MergePhotosTask(this).execute(mPhotos);
                break;
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final InstagramMedia photo = mPhotos.get(position);
        photo.setSelected(!photo.isSelected());
        checkPhotoSelection(view, photo);
    }

    @Override
    public void onStart() {
        super.onStart();

        mPhotoSelectButton.setEnabled(true);
    }

    // ------------------- PRIVATE -------------------

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

    private class ViewHolder {

        public LinearLayout photoLayout;
        public ImageView    photoImageView;
        public TextView     likesCountTextView;

    }

    private class MergePhotosTask extends AsyncTask<List<InstagramMedia>, Integer, Uri> {

        private final Context mContext;

        public MergePhotosTask(Context context) {
            mContext = context;
        }

        @Override
        protected void onPreExecute() {
            mPhotoSelectButton.setEnabled(false);
        }

        @SuppressWarnings("ResultOfMethodCallIgnored")
        @Override
        protected Uri doInBackground(List<InstagramMedia>... params) {
            final List<InstagramMedia> photos = params[0];

            try {
                final List<Bitmap> bitmaps = new ArrayList<Bitmap>();
                for (InstagramMedia photo : photos) {
                    if (photo.isSelected()) {
                        bitmaps.add(Picasso.with(mContext)
                                .load(photo.getStandardResolutionImageUrl())
                                .get()
                        );
                    }
                }

                final int size = bitmaps.get(0).getWidth();
                final Bitmap collage = Bitmap.createBitmap(size * bitmaps.size()
                        , size, Bitmap.Config.ARGB_8888);
                final Canvas canvas = new Canvas(collage);
                final Paint paint = new Paint();

                for (int i = 0, l = bitmaps.size(); i < l; ++i) {
                    canvas.drawBitmap(bitmaps.get(i), size * i, 0, paint);
                }

                final String collageFileName = mContext.getFilesDir() + File.separator + COLLAGE_RELATIVE_FILE_NAME;
                File collageFile = new File(collageFileName);
                collageFile.mkdirs();
                if (collageFile.exists()) {
                    collageFile.delete();
                }
                final OutputStream out = new FileOutputStream(collageFileName);
                IOUtils.writeBitmapAndCloseSilently(out, collage, COLLAGE_COMPRESS_FORMAT, COLLAGE_QUALITY);
                collageFile = new File(collageFileName);

                return FileProvider.getUriForFile(mContext, FILE_PROVIDER_AUTHORITY, collageFile);
            } catch (IOException e) {
                Log.e(TAG, "", e);
                publishProgress(ERROR_EXCEPTION_CODE);
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            final int code = progress[0];

            int toastMessageResId = -1;
            switch (code) {
                case ERROR_EXCEPTION_CODE: {
                    toastMessageResId = R.string.toast_error_exception_text;
                    break;
                }
            }

            if (toastMessageResId != -1) {
                Toast.makeText(mContext, toastMessageResId, Toast.LENGTH_SHORT).show();
                mPhotoSelectButton.setEnabled(true);
            }
        }

        @Override
        protected void onPostExecute(Uri collageFileUri) {
            if (collageFileUri != null) {
                Intent intent = new Intent(PhotoSelectionActivity.this, SendCollageActivity.class);
                Bundle extras = new Bundle();
                extras.putParcelable(EXTRA_COLLAGE_FILE_URI, collageFileUri);
                intent.putExtras(extras);
                startActivity(intent);
            } else {
                mPhotoSelectButton.setEnabled(true);
            }
        }
    }



}
