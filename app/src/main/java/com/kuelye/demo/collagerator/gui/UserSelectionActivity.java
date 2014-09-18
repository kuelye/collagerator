package com.kuelye.demo.collagerator.gui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.kuelye.components.async.RetainedAsyncTaskFragmentActivity;
import com.kuelye.demo.collagerator.R;

import static com.kuelye.demo.collagerator.gui.PhotoSelectionActivity.EXTRA_MEDIA_COUNT;
import static com.kuelye.demo.collagerator.gui.PhotoSelectionActivity.EXTRA_USER_ID;

public class UserSelectionActivity
        extends RetainedAsyncTaskFragmentActivity<UserParsingTaskFragment>
        implements View.OnClickListener, UserParsingTaskFragment.Handler {

    private EditText                    mUserIdEditText;
    private Button                      mUserSelectButton;

    @Override
    protected UserParsingTaskFragment createTaskFragment() {
        return new UserParsingTaskFragment();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_selection_activity);

        mUserSelectButton = (Button) findViewById(R.id.user_select_button);
        mUserSelectButton.setOnClickListener(this);
        mUserIdEditText = (EditText) findViewById(R.id.user_name_edit_text);
    }

    @Override
    public void onResume() {
        super.onResume();

        setUiEnabled(!getTaskFragment().isExecuted());
    }

    // --- View.OnClickListener ---

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.user_select_button:
                final String username = mUserIdEditText.getText().toString();
                getTaskFragment().startTask(username);
                setUiEnabled(false);
                break;
        }
    }

    // --- UserParsingHandler ---

    @Override
    public void onProgressUpdate(UserParsingTaskFragment.ProgressCode... values) {
        final UserParsingTaskFragment.ProgressCode progressCode = values[0];

        int toastMessageResId = -1;
        switch (progressCode) {
            case USER_NOT_FOUND:
                toastMessageResId = R.string.error_user_not_found_toast_text;
                break;
            case EXCEPTION_CAUGHT:
                toastMessageResId = R.string.error_exception_catched_toast_text;
                break;
            case API_NOT_ALLOWED:
                toastMessageResId = R.string.error_api_not_allowed_toast_text;
                break;
            case EMPTY:
        }

        if (toastMessageResId != -1) {
            Toast.makeText(this, toastMessageResId, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onCancelled(UserParsingTaskFragment.ResultHolder resultHolder) {
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