package com.kuelye.demo.collagerator.gui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.kuelye.demo.collagerator.R;
import com.kuelye.demo.collagerator.instagram.InstagramMedia;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.kuelye.components.utils.IOUtils.readFullyAndCloseSilently;
import static com.kuelye.demo.collagerator.gui.PhotoSelectionActivity.EXTRA_PHOTOS;

public class UserSelectionActivity extends Activity implements View.OnClickListener {

    private static final String TAG = "com.demo.collagerator.gui.UserSelectionActivity";

    private static final String REQUEST_USER_SEARCH
            = "https://api.instagram.com/v1/users/search?q=%s&count=1&client_id=%s";
    private static final String REQUEST_USER_MEDIA_RECENT
            = "https://api.instagram.com/v1/users/%d/media/recent/?count=%d&max_id=%s&client_id=%s";
    private static final String CLIENT_ID
            = "427b4d3cf46d4ca69af14ab604dd2d05";
    private static final int    MEDIA_COUNT_PER_REQUEST
            = 10;

    private static final String RESPONSE_ENCODING                               = "UTF-8";
    private static final String RESPONSE_PAGINATION_FIELD_NAME                  = "pagination";
    private static final String RESPONSE_PAGINATION_NEXT_MAX_ID_FIELD_NAME      = "next_max_id";
    private static final String RESPONSE_DATA_FIELD_NAME                        = "data";
    private static final String RESPONSE_USER_ID_FIELD_NAME                     = "id";
    private static final String RESPONSE_USER_NAME_FIELD_NAME                   = "username";
    private static final String RESPONSE_MEDIA_TYPE_FIELD_NAME                  = "type";
    private static final String RESPONSE_MEDIA_TYPE_IMAGE                       = "image";
    private static final String RESPONSE_MEDIA_IMAGES_FIELD_NAME                = "images";
    private static final String RESPONSE_MEDIA_IMAGES_THUMBNAIL_FIELD_NAME      = "thumbnail";
    private static final String RESPONSE_MEDIA_IMAGES_STANDARD_RESOLUTION_FIELD_NAME
            = "standard_resolution";
    private static final String RESPONSE_MEDIA_IMAGES_URL_FIELD_NAME            = "url";
    private static final String RESPONSE_MEDIA_LIKES_FIELD_NAME                 = "likes";
    private static final String RESPONSE_MEDIA_LIKES_COUNT_FIELD_NAME           = "count";

    private static final int    ERROR_CAN_NOT_FIND_USER_CODE                    = 1;
    private static final int    ERROR_EXCEPTION_CODE                            = 2;
    private static final int    ERROR_NO_PHOTOS_CODE                            = 3;

    private EditText    mUserIdEditText;
    private Button      mUserSelectButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_selection_activity);

        mUserSelectButton = (Button) findViewById(R.id.user_select_button);
        mUserSelectButton.setOnClickListener(this);
        mUserIdEditText = (EditText) findViewById(R.id.user_name_edit_text);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.user_select_button: {
                new GetPhotosTask(this).execute(mUserIdEditText.getText().toString());
                break;
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        mUserSelectButton.setEnabled(true);
    }

    // -------------------- INNER --------------------

    private class GetPhotosTask extends AsyncTask<String, Integer, List<InstagramMedia>> {

        private final Context mContext;

        public GetPhotosTask(Context context) {
            mContext = context;
        }

        @Override
        protected void onPreExecute() {
            mUserSelectButton.setEnabled(false);
        }

        @Override
        protected List<InstagramMedia> doInBackground(String... params) {
            final String userName = params[0];

            try {
                String request = String.format(REQUEST_USER_SEARCH, userName, CLIENT_ID);
                JSONObject response = getResponse(request);
                JSONArray data = response.getJSONArray(RESPONSE_DATA_FIELD_NAME);
                int userId = -1;

                if (data.length() != 0) {
                    final JSONObject firstUserData = (JSONObject) data.get(0);
                    final String firstUserName = firstUserData.getString(RESPONSE_USER_NAME_FIELD_NAME);
                    if (userName.equals(firstUserName)) {
                        userId = firstUserData.getInt(RESPONSE_USER_ID_FIELD_NAME);
                    }
                }

                if (userId == -1) {
                    publishProgress(ERROR_CAN_NOT_FIND_USER_CODE);
                    return null;
                }

                String nextMaxId = "";
                final List<InstagramMedia> photos = new ArrayList<InstagramMedia>();
                do {
                    request = String.format(REQUEST_USER_MEDIA_RECENT, userId
                            , MEDIA_COUNT_PER_REQUEST, nextMaxId, CLIENT_ID);
                    response = getResponse(request);
                    data = response.getJSONArray(RESPONSE_DATA_FIELD_NAME);
                    parseMediaData(photos, data);

                    JSONObject pagination = response.getJSONObject(RESPONSE_PAGINATION_FIELD_NAME);
                    if (pagination != null) {
                        nextMaxId = pagination.getString(RESPONSE_PAGINATION_NEXT_MAX_ID_FIELD_NAME);
                    } else {
                        nextMaxId = "";
                    }
                    Log.d("GUB", nextMaxId);
                } while (data.length() != 0);

                if (photos.size() == 0) {
                    publishProgress(ERROR_NO_PHOTOS_CODE);
                    return null;
                }

                Collections.sort(photos, new LikesCountDescendingComparator());

                return photos;
            } catch (IOException e) {
                publishProgress(ERROR_EXCEPTION_CODE);
                Log.e(TAG, "", e);
            } catch (JSONException e) {
                publishProgress(ERROR_EXCEPTION_CODE);
                Log.e(TAG, "", e);
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            final int code = progress[0];

            int toastMessageResId = -1;
            switch (code) {
                case ERROR_CAN_NOT_FIND_USER_CODE: {
                    toastMessageResId = R.string.toast_error_can_not_find_user_text;
                    break;
                }
                case ERROR_EXCEPTION_CODE: {
                    toastMessageResId = R.string.toast_error_exception_text;
                    break;
                }
                case ERROR_NO_PHOTOS_CODE: {
                    toastMessageResId = R.string.toast_error_no_photos_text;
                    break;
                }
            }

            if (toastMessageResId != -1) {
                Toast.makeText(mContext, toastMessageResId, Toast.LENGTH_SHORT).show();
                mUserSelectButton.setEnabled(true);
            }
        }

        @Override
        protected void onPostExecute(List<InstagramMedia> photos) {
            if (photos != null) {
                Intent intent = new Intent(mContext, PhotoSelectionActivity.class);
                Bundle extras = new Bundle();
                extras.putParcelableArrayList(EXTRA_PHOTOS, (ArrayList<InstagramMedia>) photos);
                intent.putExtras(extras);
                startActivity(intent);
            } else {
                mUserSelectButton.setEnabled(true);
            }
        }

        // ------------------- PRIVATE -------------------

        private JSONObject getResponse(String request) throws IOException, JSONException {
            final URL url = new URL(request);
            final InputStream in = url.openConnection().getInputStream();
            final String response = readFullyAndCloseSilently(in, RESPONSE_ENCODING);

            return new JSONObject(response);
        }

        private void parseMediaData(List<InstagramMedia> photos, JSONArray data) throws JSONException {
            for (int i = 0, l = data.length(); i < l; ++i) {
                JSONObject media = (JSONObject) data.get(i);
                if (media.getString(RESPONSE_MEDIA_TYPE_FIELD_NAME)
                        .equals(RESPONSE_MEDIA_TYPE_IMAGE)) {
                    JSONObject images = media.getJSONObject(RESPONSE_MEDIA_IMAGES_FIELD_NAME);
                    String thumbnailImageUrl = images
                            .getJSONObject(RESPONSE_MEDIA_IMAGES_THUMBNAIL_FIELD_NAME)
                            .getString(RESPONSE_MEDIA_IMAGES_URL_FIELD_NAME);
                    String standardResolutionImageUrl = images
                            .getJSONObject(RESPONSE_MEDIA_IMAGES_STANDARD_RESOLUTION_FIELD_NAME)
                            .getString(RESPONSE_MEDIA_IMAGES_URL_FIELD_NAME);
                    int likesCount = media.getJSONObject(RESPONSE_MEDIA_LIKES_FIELD_NAME)
                            .getInt(RESPONSE_MEDIA_LIKES_COUNT_FIELD_NAME);
                    photos.add(new InstagramMedia(thumbnailImageUrl, standardResolutionImageUrl, likesCount));
                }
            }
        }

        // -------------------- INNER --------------------

        private class LikesCountDescendingComparator implements Comparator<InstagramMedia> {

            @Override
            public int compare(InstagramMedia oA, InstagramMedia oB) {
                return oA.getLikesCount() < oB.getLikesCount() ? 1 :
                        oA.getLikesCount() == oB.getLikesCount() ? 0 : -1;
            }
        }

    }

}
