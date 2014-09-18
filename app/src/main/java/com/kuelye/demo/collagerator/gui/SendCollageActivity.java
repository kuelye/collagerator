package com.kuelye.demo.collagerator.gui;

import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.kuelye.components.async.RetainedAsyncTaskFragmentActivity;
import com.kuelye.demo.collagerator.R;
import com.kuelye.demo.collagerator.instagram.InstagramMedia;
import com.squareup.picasso.Picasso;

import java.util.List;

import static com.kuelye.demo.collagerator.gui.PhotoMergingTaskFragment.ProgressCode.EMPTY;

public class SendCollageActivity
        extends RetainedAsyncTaskFragmentActivity<PhotoMergingTaskFragment>
        implements View.OnClickListener, PhotoMergingTaskFragment.Handler {

    public static final String EXTRA_CHOOSEN_PHOTOS     = "EXTRA_CHOOSEN_PHOTOS";

    private static final String STATE_COLLAGE_FILE_URI  = "COLLAGE_FILE_URI";

    private static final String EMAIL_INTENT_TYPE       = "application/image";

    private List<InstagramMedia>    mChoosenPhotos;
    private Uri                     mCollageFileUri;

    private Button                  mSendCollageButton;
    private TextView                mProcessedTextView;

    @Override
    protected PhotoMergingTaskFragment createTaskFragment() {
        return new PhotoMergingTaskFragment();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.send_collage_activity);

        if (savedInstanceState != null && savedInstanceState.containsKey(STATE_COLLAGE_FILE_URI)) {
            mCollageFileUri = savedInstanceState.getParcelable(STATE_COLLAGE_FILE_URI);
        } else {
            final Bundle extras = getIntent().getExtras();
            mChoosenPhotos = extras.getParcelableArrayList(EXTRA_CHOOSEN_PHOTOS);
        }

        mSendCollageButton = (Button) findViewById(R.id.send_collage_button);
        mSendCollageButton.setOnClickListener(this);
        mProcessedTextView = (TextView) findViewById(R.id.processed_text_view);

        setUiEnabled(mCollageFileUri != null);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (!getTaskFragment().isExecuted() && mChoosenPhotos != null) {
            getTaskFragment().startTask(mChoosenPhotos);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable(STATE_COLLAGE_FILE_URI, mCollageFileUri);
    }

    // --- View.OnClickListener ---

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

    // --- PhotoMergingTaskFragment.Handler ---

    @Override
    public void onProgressUpdate(PhotoMergingTaskFragment.ProgressHolder... values) {
        PhotoMergingTaskFragment.ProgressHolder progressHolder = values[0];

        int toastMessageResId = -1;
        int duration = Toast.LENGTH_SHORT;
        switch (progressHolder.progressCode) {
            case EXCEPTION_CAUGHT:
                toastMessageResId = R.string.error_exception_catched_toast_text;
                break;
            case OUT_OF_MEMORY:
                toastMessageResId = R.string.error_out_of_memory_toast_text;
                duration = Toast.LENGTH_LONG;
                break;
            case EMPTY:
        }

        if (toastMessageResId != -1) {
            Toast.makeText(this, toastMessageResId, duration).show();
            if (progressHolder.progressCode != EMPTY) {
                finish();
            }
        }

        if (progressHolder.message != null) {
            mProcessedTextView.setText(progressHolder.message);
        }
    }

    @Override
    public void onCancelled(Uri uri) {
        // stub
    }

    @Override
    public void onPostExecute(Uri uri) {
        mCollageFileUri = uri;
        mChoosenPhotos = null;

        ImageView collageImageView = (ImageView) findViewById(R.id.collage_image_view);
        Picasso.with(this)
                .load(mCollageFileUri)
                .skipMemoryCache()
                .into(collageImageView);

        setUiEnabled(true);
    }

    // ------------------- PRIVATE -------------------

    private void setUiEnabled(boolean enabled) {
        mSendCollageButton.setEnabled(enabled);
    }

}
