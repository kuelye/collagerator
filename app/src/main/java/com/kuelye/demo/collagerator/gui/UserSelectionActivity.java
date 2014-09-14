package com.kuelye.demo.collagerator.gui;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

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

import static com.kuelye.components.utils.IOUtils.readFullyAndClose;

public class UserSelectionActivity extends Activity implements View.OnClickListener {

    private static final String TAG = "com.demo.collagerator.gui.UserSelectionActivity";

    private static final String USER_SEARCH_REQUEST
            = "https://api.instagram.com/v1/users/search?q=%s&count=1&client_id=%s";
    private static final String USER_MEDIA_RECENT_REQUEST
            = "https://api.instagram.com/v1/users/%d/media/recent/?client_id=%s";
    private static final String CLIENT_ID
            = "427b4d3cf46d4ca69af14ab604dd2d05";

    private static final String RESPONSE_ENCODING                               = "UTF-8";
    private static final String RESPONSE_DATA_FIELD_NAME                        = "data";
    private static final String RESPONSE_USER_ID_FIELD_NAME                     = "id";
    private static final String RESPONSE_MEDIA_TYPE_FIELD_NAME                  = "type";
    private static final String RESPONSE_MEDIA_TYPE_IMAGE                       = "image";
    private static final String RESPONSE_MEDIA_IMAGES_FIELD_NAME                = "images";
    private static final String RESPONSE_MEDIA_IMAGES_STANDARD_FIELD_NAME
            = "standard_resolution";
    private static final String RESPONSE_MEDIA_IMAGES_STANDARD_URL_FIELD_NAME   = "url";
    private static final String RESPONSE_MEDIA_LIKES_FIELD_NAME                 = "likes";
    private static final String RESPONSE_MEDIA_LIKES_COUNT_FIELD_NAME           = "count";

    private Button mMakeCollageButton;
    private EditText mUserIdEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_selection_activity);

        mMakeCollageButton = (Button) findViewById(R.id.make_collage_button);
        mMakeCollageButton.setOnClickListener(this);
        mUserIdEditText = (EditText) findViewById(R.id.user_id_edit_text);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.make_collage_button: {
                new GetPhotosTask().execute(mUserIdEditText.getText().toString());
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

    private class GetPhotosTask extends AsyncTask<String, Void, List<InstagramMedia>> {

        @Override
        protected List<InstagramMedia> doInBackground(String... params) {
            final String userName = params[0];

            try {
                String request = String.format(USER_SEARCH_REQUEST, userName, CLIENT_ID);
                JSONObject response = getResponse(request);
                JSONArray data = response.getJSONArray(RESPONSE_DATA_FIELD_NAME);

                if (data.length() == 0) {
                    // TODO [F] no user with the specific user name case
                } else {
                    int userId = ((JSONObject) data.get(0)).getInt(RESPONSE_USER_ID_FIELD_NAME);

                    request = String.format(USER_MEDIA_RECENT_REQUEST, userId, CLIENT_ID);
                    response = getResponse(request);
                    data = response.getJSONArray(RESPONSE_DATA_FIELD_NAME);

                    return parseMediaData(data);
                }
            } catch (IOException e) {
                Log.e(TAG, "", e);
            } catch (JSONException e) {
                Log.e(TAG, "", e);
            }

            return null;
        }

        @Override
        protected void onPostExecute(List<InstagramMedia> photosLinks) {

        }

        // ------------------- PRIVATE -------------------

        private JSONObject getResponse(String request) throws IOException, JSONException {
            final URL url = new URL(request);
            final InputStream in = url.openConnection().getInputStream();
            final String response = readFullyAndClose(in, RESPONSE_ENCODING);

            return new JSONObject(response);
        }

        private List<InstagramMedia> parseMediaData(JSONArray data) throws JSONException {
            final List<InstagramMedia> result = new ArrayList<InstagramMedia>();

            for (int i = 0, l = data.length(); i < l; ++i) {
                JSONObject media = (JSONObject) data.get(i);
                if (media.getString(RESPONSE_MEDIA_TYPE_FIELD_NAME)
                        .equals(RESPONSE_MEDIA_TYPE_IMAGE)) {
                    String image = media.getJSONObject(RESPONSE_MEDIA_IMAGES_FIELD_NAME)
                            .getJSONObject(RESPONSE_MEDIA_IMAGES_STANDARD_FIELD_NAME)
                            .getString(RESPONSE_MEDIA_IMAGES_STANDARD_URL_FIELD_NAME);
                    int likes = media.getJSONObject(RESPONSE_MEDIA_LIKES_FIELD_NAME)
                            .getInt(RESPONSE_MEDIA_LIKES_COUNT_FIELD_NAME);
                    result.add(new InstagramMedia(image, likes));
                } else {
                    Log.d("GUB", "VIDEO CATCHED");
                }
            }

            Collections.sort(result, new LikesCountDescendingComparator());
            Log.d("GUB", result.toString());

            return result;
        }

    }

}
