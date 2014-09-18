package com.kuelye.demo.collagerator.gui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.net.Uri;
import android.support.v4.content.FileProvider;
import android.util.Log;

import com.kuelye.components.async.RetainedAsyncTaskFragment;
import com.kuelye.components.utils.IOUtils;
import com.kuelye.demo.collagerator.instagram.InstagramMedia;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import static android.graphics.Bitmap.CompressFormat.JPEG;
import static com.kuelye.demo.collagerator.gui.PhotoMergingTaskFragment.ProgressCode.EMPTY;
import static com.kuelye.demo.collagerator.gui.PhotoMergingTaskFragment.ProgressCode.EXCEPTION_CAUGHT;
import static com.kuelye.demo.collagerator.gui.PhotoMergingTaskFragment.ProgressCode.OUT_OF_MEMORY;

public class PhotoMergingTaskFragment extends RetainedAsyncTaskFragment
        <List<InstagramMedia>, PhotoMergingTaskFragment.ProgressHolder, Uri, PhotoMergingTaskFragment.Task> {

    private static final String TAG
            = "com.demo.collagerator.gui.PhotosMergingTaskFragment";

    private static final String     COLLAGE_RELATIVE_FILE_NAME  = "collages/collage.jpg";
    private static final Bitmap.CompressFormat
                                    COLLAGE_COMPRESS_FORMAT     = JPEG;
    private static final int        COLLAGE_QUALITY             = 90;

    private static final String     FILE_PROVIDER_AUTHORITY     = "com.kuelye.demo.collagerator";

    private static final String     PROGRESS_TEXT_TEMPLATE      = "%d/%d";

    @Override
    protected PhotoMergingTaskFragment.Task createTask() {
        return new PhotoMergingTaskFragment.Task(getActivity());
    }

    // -------------------- INNER --------------------

    static interface Handler extends RetainedAsyncTaskFragment.Handler
            <ProgressHolder, Uri> {

    }

    static enum ProgressCode {

        EMPTY,
        OUT_OF_MEMORY,
        EXCEPTION_CAUGHT

    }

    static class ProgressHolder {

        public ProgressHolder(ProgressCode progressCode) {
            this.progressCode = progressCode;
        }

        public ProgressHolder(ProgressCode progressCode, String message) {
            this.progressCode = progressCode;
            this.message = message;
        }

        ProgressCode    progressCode;
        String          message;

    }

    static class Task extends RetainedAsyncTaskFragment.Task
            <List<InstagramMedia>, ProgressHolder, Uri> {

        private Context mContext;

        public Task(Context context) {
            mContext = context;
        }

        @Override
        protected Uri doInBackground(List<InstagramMedia>... params) {
            final List<InstagramMedia> choosenPhotos = params[0];

            try {
                final List<Bitmap> bitmaps = new ArrayList<Bitmap>();
                for (int i = 0; i < choosenPhotos.size(); ++i) {
                    String message = String.format(PROGRESS_TEXT_TEMPLATE, i, choosenPhotos.size());
                    publishProgress(new ProgressHolder(EMPTY, message));

                    InstagramMedia photo = choosenPhotos.get(i);
                    bitmaps.add(Picasso.with(mContext)
                            .load(photo.getStandardResolutionImageUrl())
                            .get());
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
                //noinspection ResultOfMethodCallIgnored
                collageFile.mkdirs();
                if (collageFile.exists()) {
                    //noinspection ResultOfMethodCallIgnored
                    collageFile.delete();
                }
                final OutputStream out = new FileOutputStream(collageFileName);
                IOUtils.writeBitmapAndCloseSilently(out, collage, COLLAGE_COMPRESS_FORMAT, COLLAGE_QUALITY);
                collageFile = new File(collageFileName);

                String message = String.format(PROGRESS_TEXT_TEMPLATE, choosenPhotos.size(), choosenPhotos.size());
                publishProgress(new ProgressHolder(EMPTY, message));
                return FileProvider.getUriForFile(mContext, FILE_PROVIDER_AUTHORITY, collageFile);
            } catch (IOException e) {
                Log.e(TAG, "", e);
                publishProgress(new ProgressHolder(EXCEPTION_CAUGHT));
            } catch (OutOfMemoryError e) {
                Log.e(TAG, "", e);
                publishProgress(new ProgressHolder(OUT_OF_MEMORY));
            }

            return null;
        }

    }

}