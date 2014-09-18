package com.kuelye.demo.collagerator.gui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.kuelye.demo.collagerator.R;
import com.kuelye.demo.collagerator.instagram.InstagramMedia;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.kuelye.demo.collagerator.gui.PhotoMergingTaskLoader.ARGS_DISPLAYED_PHOTOS;
import static com.kuelye.demo.collagerator.gui.PhotoSelectionAdapter.checkPhotoSelection;
import static com.kuelye.demo.collagerator.gui.SendCollageActivity.EXTRA_COLLAGE_FILE_URI;

public class PhotoSelectionActivity extends FragmentActivity
        implements View.OnClickListener, AdapterView.OnItemClickListener
        , PhotoParsingTaskFragment.Handler, PhotoSelectionContext
        , LoaderManager.LoaderCallbacks<Uri> {

    public static final String  EXTRA_USER_ID                   = "USER_ID";
    public static final String  EXTRA_MEDIA_COUNT               = "MEDIA_COUNT";

    private static final int ID_PHOTO_MERGING_TASK_LOADER       = 1;

    private static final String TAG_PHOTO_PARSING_TASK_FRAGMENT
            = "PHOTO_PARSING_TASK_FRAGMENT";

    private static final String PAGINATION_NEXT_MAX_ID_MAX      = "";
    private static final int    PHOTOS_PER_PAGE                 = 16;
    private static final int    DISPLAYED_PAGES_COUNT_DEFAULT   = 1;

    private static final String PROCESSED_TEXT_TEMPLATE         = "%d/%d";

    private int                         mUserId;
    private int                         mMediaCount;
    private List<InstagramMedia>        mPhotos;
    private List<InstagramMedia>        mDisplayedPhotos;
    private int                         mDisplayedPagesCount;
    private int                         mProcessedCount;
    private String                      mPaginationNextMaxId;

    private GridView                    mGridView;
    private TextView                    mProcessedTextView;
    private PhotoSelectionAdapter       mAdapter;
    private Button                      mPhotoSelectButton;
    private PhotoParsingTaskFragment    mTaskFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.photo_selection_activity);

        Bundle extras = getIntent().getExtras();
        mUserId = extras.getInt(EXTRA_USER_ID);
        mMediaCount = extras.getInt(EXTRA_MEDIA_COUNT);
        mPhotos = new ArrayList<InstagramMedia>();
        mDisplayedPagesCount = DISPLAYED_PAGES_COUNT_DEFAULT;
        updateDisplayedPhotos(true);

        mProcessedTextView = (TextView) findViewById(R.id.processed_text_view);
        setProcessedText();
        mGridView = (GridView) findViewById(R.id.photos_grid_view);
        mAdapter = new PhotoSelectionAdapter(this, this, R.layout.photo_selection_row, mDisplayedPhotos);
        mGridView.setAdapter(mAdapter);
        mGridView.setOnItemClickListener(this);
        mPhotoSelectButton = (Button) findViewById(R.id.photo_select_button);
        mPhotoSelectButton.setOnClickListener(this);

        final FragmentManager fragmentManager = getSupportFragmentManager();
        mTaskFragment = (PhotoParsingTaskFragment) fragmentManager
                .findFragmentByTag(TAG_PHOTO_PARSING_TASK_FRAGMENT);
        if (mTaskFragment == null) {
            mTaskFragment = new PhotoParsingTaskFragment();
            fragmentManager.beginTransaction()
                    .add(mTaskFragment, TAG_PHOTO_PARSING_TASK_FRAGMENT).commit();
        }
        mPaginationNextMaxId = PAGINATION_NEXT_MAX_ID_MAX;
        mTaskFragment.startTask(mUserId, mPaginationNextMaxId);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.photo_select_button:
                setUiEnabled(false);
                Bundle args = new Bundle();
                args.putParcelableArrayList(ARGS_DISPLAYED_PHOTOS
                        , (ArrayList<InstagramMedia>) mDisplayedPhotos);
                Loader<Uri> loader = getSupportLoaderManager()
                        .initLoader(ID_PHOTO_MERGING_TASK_LOADER, args, this);
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final InstagramMedia photo = mPhotos.get(position);
        photo.setSelected(!photo.isSelected());
        checkPhotoSelection(view, photo);
    }

    @Override
    public void onPreExecute() {
        // stub
    }

    @Override
    public void onProgressUpdate(PhotoParsingTaskFragment.ProgressCode progressCode) {
        int toastMessageResId = -1;
        switch (progressCode) {
            case EXCEPTION_CATCHED:
                toastMessageResId = R.string.error_toast_exception_catched_text;
                break;
            case EMPTY:
        }

        if (toastMessageResId != -1) {
            Toast.makeText(this, toastMessageResId, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onCancelled() {
        // stub
    }

    @Override
    public void onPostExecute(PhotoParsingTaskFragment.ResultHolder resultHolder) {
        mPhotos.addAll(resultHolder.nextPhotos);
        Collections.sort(mPhotos, new InstagramMedia.LikesCountDescendingComparator());
        updateDisplayedPhotos(true);
        mProcessedCount += resultHolder.processedCount;
        mPaginationNextMaxId = resultHolder.nextMaxId;

        setProcessedText();

        if (resultHolder.processedCount != 0) {
            mTaskFragment.startTask(mUserId, mPaginationNextMaxId);
        }
    }

    @Override
    public void checkPosition(int position) {
        if (position == mDisplayedPagesCount * PHOTOS_PER_PAGE - 1) {
            mDisplayedPagesCount++;
            updateDisplayedPhotos(false);
        }
    }

    @Override
    public Loader<Uri> onCreateLoader(int id, Bundle args) {
        Loader<Uri> loader = null;

        if (id == ID_PHOTO_MERGING_TASK_LOADER) {
            loader = new PhotoMergingTaskLoader(this, args);
        }

        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Uri> uriLoader, Uri uri) {
        if (uri != null) {
            Intent intent = new Intent(PhotoSelectionActivity.this, SendCollageActivity.class);
            Bundle extras = new Bundle();
            extras.putParcelable(EXTRA_COLLAGE_FILE_URI, uri);
            intent.putExtras(extras);
            startActivity(intent);
        } else {
            setUiEnabled(true);
        }
    }

    @Override
    public void onLoaderReset(Loader<Uri> uriLoader) {
        setUiEnabled(true);
    }

    // ------------------- PRIVATE -------------------

    private void setUiEnabled(boolean enabled) {
        mPhotoSelectButton.setEnabled(enabled);
        mGridView.setEnabled(enabled);
    }

    private void setProcessedText() {
        mProcessedTextView.setText(
                String.format(PROCESSED_TEXT_TEMPLATE, mProcessedCount, mMediaCount));
    }

    private void updateDisplayedPhotos(boolean full) {
        int iMin = 0;
        final int iMax = Math.min(mDisplayedPagesCount * PHOTOS_PER_PAGE, mPhotos.size());

        if (full) {
            if (mDisplayedPhotos == null) {
                mDisplayedPhotos = new ArrayList<InstagramMedia>();
            } else {
                mDisplayedPhotos.clear();
            }
        } else {
            iMin = mDisplayedPhotos.size();
        }

        for (int i = iMin; i < iMax; ++i) {
            mDisplayedPhotos.add(mPhotos.get(i));
        }

        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
    }

    // -------------------- INNER --------------------

}
