package com.kuelye.demo.collagerator.gui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.kuelye.components.async.RetainedAsyncTaskFragmentActivity;
import com.kuelye.demo.collagerator.R;
import com.kuelye.demo.collagerator.instagram.InstagramMedia;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.kuelye.demo.collagerator.gui.PhotoSelectionAdapter.checkPhotoSelection;
import static com.kuelye.demo.collagerator.gui.SendCollageActivity.EXTRA_CHOOSEN_PHOTOS;

public class PhotoSelectionActivity
        extends RetainedAsyncTaskFragmentActivity<PhotoParsingTaskFragment>
        implements View.OnClickListener, AdapterView.OnItemClickListener
        , PhotoParsingTaskFragment.Handler, PhotoSelectionContext {

    public static final String  EXTRA_USER_ID                   = "USER_ID";
    public static final String  EXTRA_MEDIA_COUNT               = "MEDIA_COUNT";

    private static final String STATE_USER_ID                   = "USER_ID";
    private static final String STATE_MEDIA_COUNT               = "MEDIA_COUNT";
    private static final String STATE_PHOTOS                    = "PHOTOS";
    private static final String STATE_DISPLAYED_PAGES_COUNT     = "DISPLAYED_PAGES_COUNT";
    private static final String STATE_PROCESSED_COUNT           = "PROCESSED_COUNT";
    private static final String STATE_PAGINATION_NEXT_MAX_ID    = "PAGINATION_NEXT_MAX_ID";

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

    private TextView                    mProcessedTextView;
    private PhotoSelectionAdapter       mAdapter;

    @Override
    protected PhotoParsingTaskFragment createTaskFragment() {
        return new PhotoParsingTaskFragment();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.photo_selection_activity);

        if (savedInstanceState != null && savedInstanceState.containsKey(STATE_USER_ID)) {
            mUserId = savedInstanceState.getInt(STATE_USER_ID);
            mMediaCount = savedInstanceState.getInt(STATE_MEDIA_COUNT, mMediaCount);
            mPhotos = savedInstanceState.getParcelableArrayList(STATE_PHOTOS);
            mDisplayedPagesCount = savedInstanceState.getInt(STATE_DISPLAYED_PAGES_COUNT);
            mProcessedCount = savedInstanceState.getInt(STATE_PROCESSED_COUNT);
            mPaginationNextMaxId = savedInstanceState.getString(STATE_PAGINATION_NEXT_MAX_ID);
        } else {
            Bundle extras = getIntent().getExtras();
            mUserId = extras.getInt(EXTRA_USER_ID);
            mMediaCount = extras.getInt(EXTRA_MEDIA_COUNT);
            mPhotos = new ArrayList<InstagramMedia>();
            mDisplayedPagesCount = DISPLAYED_PAGES_COUNT_DEFAULT;
            mPaginationNextMaxId = PAGINATION_NEXT_MAX_ID_MAX;
        }

        updateDisplayedPhotos(true);

        mProcessedTextView = (TextView) findViewById(R.id.processed_text_view);
        setProcessedText();
        final GridView gridView = (GridView) findViewById(R.id.photos_grid_view);
        mAdapter = new PhotoSelectionAdapter(this, this, R.layout.photo_selection_row, mDisplayedPhotos);
        gridView.setAdapter(mAdapter);
        gridView.setOnItemClickListener(this);
        Button photoSelectButton = (Button) findViewById(R.id.photo_select_button);
        photoSelectButton.setOnClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();

        final PhotoParsingTaskFragment taskFragment = getTaskFragment();
        if (!taskFragment.isExecuted()) {
            taskFragment.startTask(
                    new PhotoParsingTaskFragment.ParamsHolder(mUserId, mPaginationNextMaxId));
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(STATE_USER_ID, mUserId);
        outState.putInt(STATE_MEDIA_COUNT, mMediaCount);
        outState.putParcelableArrayList(STATE_PHOTOS, (ArrayList<InstagramMedia>) mPhotos);
        outState.putInt(STATE_DISPLAYED_PAGES_COUNT, mDisplayedPagesCount);
        outState.putInt(STATE_PROCESSED_COUNT, mProcessedCount);
        outState.putString(STATE_PAGINATION_NEXT_MAX_ID, mPaginationNextMaxId);
    }

    // --- View.OnClickListener ---

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.photo_select_button:
                List<InstagramMedia> choosenPhotos = new ArrayList<InstagramMedia>();
                for (InstagramMedia photo : mDisplayedPhotos) {
                    if (photo.isSelected()) {
                        choosenPhotos.add(photo);
                    }
                }

                if (choosenPhotos.size() == 0) {
                    Toast.makeText(this, R.string.error_no_photo_chose_toast_text, Toast.LENGTH_SHORT).show();
                }

                Intent intent = new Intent(PhotoSelectionActivity.this, SendCollageActivity.class);
                Bundle extras = new Bundle();
                extras.putParcelableArrayList(EXTRA_CHOOSEN_PHOTOS, (ArrayList<InstagramMedia>) choosenPhotos);
                intent.putExtras(extras);
                startActivity(intent);
                break;
        }
    }

    // --- AdapterView.OnItemClickListener ---

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final InstagramMedia photo = mPhotos.get(position);
        photo.setSelected(!photo.isSelected());
        checkPhotoSelection(view, photo);
    }

    // --- PhotoParsingTaskFragment.Handler ---

    @Override
    public void onProgressUpdate(PhotoParsingTaskFragment.ProgressCode... values) {
        PhotoParsingTaskFragment.ProgressCode progressCode = values[0];

        int toastMessageResId = -1;
        switch (progressCode) {
            case EXCEPTION_CAUGHT:
                toastMessageResId = R.string.error_exception_catched_toast_text;
                break;
            case EMPTY:
        }

        if (toastMessageResId != -1) {
            Toast.makeText(this, toastMessageResId, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onCancelled(PhotoParsingTaskFragment.ResultHolder resultHolder) {
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
            getTaskFragment().startTask(
                    new PhotoParsingTaskFragment.ParamsHolder(mUserId, mPaginationNextMaxId));
        }
    }

    // --- PhotoSelectionContext ---

    @Override
    public void checkPosition(int position) {
        if (position == mDisplayedPagesCount * PHOTOS_PER_PAGE - 1) {
            mDisplayedPagesCount++;
            updateDisplayedPhotos(false);
        }
    }

    // ------------------- PRIVATE -------------------

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
