package com.kuelye.demo.collagerator.gui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
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

import java.util.List;

public class SendCollageActivity extends Activity {

    public static final String EXTRA_COLLAGE = "EXTRA_COLLAGE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.result_collage_activity);

        Bundle extras = getIntent().getExtras();
        Bitmap collage = extras.getParcelable(EXTRA_COLLAGE);

        ImageView collageImageView = (ImageView) findViewById(R.id.collage_image_view);
        collageImageView.setImageBitmap(collage);
    }

}
