package com.kuelye.demo.collagerator.gui;

import android.content.Context;
import android.net.Uri;
import android.support.v4.content.AsyncTaskLoader;

public class PhotoMergingTaskLoader extends AsyncTaskLoader<Uri> {

    public PhotoMergingTaskLoader(Context context) {
        super(context);
    }

    @Override
    public Uri loadInBackground() {
        return null;
    }

}
