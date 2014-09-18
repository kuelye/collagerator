package com.kuelye.demo.collagerator.gui;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.kuelye.demo.collagerator.R;
import com.squareup.picasso.Picasso;

public class SendCollageActivity extends Activity implements View.OnClickListener {

    public static final String EXTRA_COLLAGE_FILE_URI = "EXTRA_COLLAGE_FILE_URI";

    private static final String EMAIL_INTENT_TYPE = "application/image";

    private ImageView   mCollageImageView;
    private Uri         mCollageFileUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.send_collage_activity);
        Log.d("GUB", "onCreate");

        final Bundle extras = getIntent().getExtras();
        mCollageFileUri = extras.getParcelable(EXTRA_COLLAGE_FILE_URI);

        mCollageImageView = (ImageView) findViewById(R.id.collage_image_view);
        Picasso.with(this)
                .load(mCollageFileUri)
                .skipMemoryCache()
                .into(mCollageImageView);

        final Button sendCollageButton = (Button) findViewById(R.id.send_collage_button);
        sendCollageButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.send_collage_button:
                Intent intent = new Intent(android.content.Intent.ACTION_SEND);
                intent.setType(EMAIL_INTENT_TYPE);
                final Resources resources = getResources();
                final String subject =  resources.getString(R.string.mail_subject);
                intent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);
                final String text =  resources.getString(R.string.mail_text);
                intent.putExtra(android.content.Intent.EXTRA_TEXT, text);
                intent.putExtra(Intent.EXTRA_STREAM, mCollageFileUri);
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                final String intentTitle =  resources.getString(R.string.mail_intent_title);
                startActivity(Intent.createChooser(intent, intentTitle));
        }
    }

}
