package com.kuelye.demo.collagerator.gui;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.FileProvider;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.kuelye.demo.collagerator.R;
import com.squareup.picasso.Picasso;

import java.io.File;

public class SendCollageActivity extends Activity implements View.OnClickListener {

    public static final String EXTRA_COLLAGE_FILE_URI = "EXTRA_COLLAGE_FILE_URI";

    private static final String EMAIL_INTENT_TYPE = "application/image";

    private Uri mCollageFileUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.result_collage_activity);

        Bundle extras = getIntent().getExtras();
        mCollageFileUri = extras.getParcelable(EXTRA_COLLAGE_FILE_URI);

        final ImageView collageImageView = (ImageView) findViewById(R.id.collage_image_view);
        Picasso.with(this)
                .load(mCollageFileUri)
                .into(collageImageView);

        final Button sendCollageButton = (Button) findViewById(R.id.send_collage_button);
        sendCollageButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.send_collage_button: {
                Intent intent = new Intent(android.content.Intent.ACTION_SEND);
                intent.setType(EMAIL_INTENT_TYPE);
                intent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{"kuelye@gmail.com"});
                intent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Test Subject");
                intent.putExtra(android.content.Intent.EXTRA_TEXT, "From My App");
                intent.putExtra(Intent.EXTRA_STREAM, mCollageFileUri);
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(Intent.createChooser(intent, "Send mail..."));
            }
        }
    }

}
