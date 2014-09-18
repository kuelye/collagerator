package com.kuelye.demo.collagerator.gui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.FileProvider;
import android.util.Log;

import com.kuelye.components.utils.IOUtils;
import com.kuelye.demo.collagerator.instagram.InstagramMedia;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import static android.graphics.Bitmap.Config.ARGB_8888;
import static android.graphics.Bitmap.CompressFormat.JPEG;

public class PhotoMergingTaskLoader extends AsyncTaskLoader<Uri> {

    public static final String          ARGS_DISPLAYED_PHOTOS       = "DISPLAYED_PHOTOS";

    private static final String         TAG
            = "com.demo.collagerator.gui.PhotoMergingTaskLoader";

    private static final Bitmap.Config  COLLAGE_BITMAP_CONFIG       = ARGB_8888;
    private static final String         COLLAGE_RELATIVE_FILE_NAME  = "collages/collage.jpg";
    private static final Bitmap.CompressFormat
                                        COLLAGE_COMPRESS_FORMAT     = JPEG;
    private static final int            COLLAGE_QUALITY             = 90;

    private static final String         FILE_PROVIDER_AUTHORITY     = "com.kuelye.demo.collagerator";

    private final List<InstagramMedia> mChoosenPhotos;

    public PhotoMergingTaskLoader(Context context, Bundle args) {
        super(context);

        Log.d("GUB", "S");
        if (args != null) {
            mChoosenPhotos = args.getParcelableArrayList(ARGS_DISPLAYED_PHOTOS);
        } else {
            mChoosenPhotos = new ArrayList<InstagramMedia>();
        }
    }

    @Override
    public Uri loadInBackground() {
        Log.d("GUB", "1");
        try {
            final List<Bitmap> bitmaps = new ArrayList<Bitmap>();
            for (InstagramMedia photo : mChoosenPhotos) {
                if (photo.isSelected()) {
                    bitmaps.add(Picasso.with(getContext())
                            .load(photo.getStandardResolutionImageUrl())
                            .get()
                    );
                }
            }

            final int size = bitmaps.get(0).getWidth();
            final Bitmap collage = Bitmap.createBitmap(size * bitmaps.size()
                    , size, COLLAGE_BITMAP_CONFIG);
            final Canvas canvas = new Canvas(collage);
            final Paint paint = new Paint();

            for (int i = 0, l = bitmaps.size(); i < l; ++i) {
                canvas.drawBitmap(bitmaps.get(i), size * i, 0, paint);
            }

            Log.d("GUB", "2");
            final String collageFileName = getContext().getFilesDir()
                    + File.separator
                    + COLLAGE_RELATIVE_FILE_NAME;
            File collageFile = new File(collageFileName);
            //noinspection ResultOfMethodCallIgnored
            collageFile.mkdirs();
            if (collageFile.exists()) {
                //noinspection ResultOfMethodCallIgnored
                collageFile.delete();
            }
            final OutputStream out = new FileOutputStream(collageFileName);
            IOUtils.writeBitmapAndCloseSilently(out, collage
                    , COLLAGE_COMPRESS_FORMAT, COLLAGE_QUALITY);
            collageFile = new File(collageFileName);

            Log.d("GUB", "3");
            return FileProvider.getUriForFile(getContext()
                    , FILE_PROVIDER_AUTHORITY, collageFile);
        } catch (IOException e) {
            Log.e(TAG, "", e);
        }

        return null;
    }

}
