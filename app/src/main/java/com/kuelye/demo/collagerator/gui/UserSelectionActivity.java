package com.kuelye.demo.collagerator.gui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.kuelye.demo.collagerator.R;

import static com.kuelye.demo.collagerator.gui.PhotoSelectionActivity.EXTRA_MEDIA_COUNT;
import static com.kuelye.demo.collagerator.gui.PhotoSelectionActivity.EXTRA_USER_ID;

public class UserSelectionActivity extends FragmentActivity
        implements View.OnClickListener, UserParsingTaskFragment.Handler {

    private static final String TAG_USER_ID_TASK_FRAGMENT = "USER_ID_TASK_FRAGMENT";

    private EditText                    mUserIdEditText;
    private Button                      mUserSelectButton;
    private UserParsingTaskFragment     mTaskFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_selection_activity);

        mUserSelectButton = (Button) findViewById(R.id.user_select_button);
        mUserSelectButton.setOnClickListener(this);
        mUserIdEditText = (EditText) findViewById(R.id.user_name_edit_text);

        final FragmentManager fragmentManager = getSupportFragmentManager();
        mTaskFragment = (UserParsingTaskFragment) fragmentManager
                .findFragmentByTag(TAG_USER_ID_TASK_FRAGMENT);
        if (mTaskFragment == null) {
            mTaskFragment = new UserParsingTaskFragment();
            fragmentManager.beginTransaction()
                    .add(mTaskFragment, TAG_USER_ID_TASK_FRAGMENT).commit();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        setUiEnabled(!mTaskFragment.isExecuted());
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.user_select_button:
                mTaskFragment.startTask(mUserIdEditText.getText().toString());
                break;
        }
    }

    @Override
    public void onPreExecute() {
        setUiEnabled(false);
    }

    @Override
    public void onProgressUpdate(UserParsingTaskFragment.ProgressCode progressCode) {
        int toastMessageResId = -1;
        switch (progressCode) {
            case USER_NOT_FOUND:
                toastMessageResId = R.string.error_toast_user_not_fount_text;
                break;
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
        setUiEnabled(true);
    }

    @Override
    public void onPostExecute(UserParsingTaskFragment.ResultHolder resultHolder) {
        if (resultHolder != null) {
            Intent intent = new Intent(this, PhotoSelectionActivity.class);
            Bundle extras = new Bundle();
            extras.putInt(EXTRA_USER_ID, resultHolder.userId);
            extras.putInt(EXTRA_MEDIA_COUNT, resultHolder.mediaCount);
            intent.putExtras(extras);
            startActivity(intent);
        } else {
            setUiEnabled(true);
        }
    }

    // ------------------- PRIVATE -------------------

    private void setUiEnabled(boolean enabled) {
        mUserIdEditText.setEnabled(enabled);
        mUserSelectButton.setEnabled(enabled);
    }

}