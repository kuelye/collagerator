package com.kuelye.demo.collagerator.gui;

import android.util.Log;

import com.kuelye.components.async.RetainedAsyncTaskFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import static com.kuelye.components.utils.NetworkUtils.getResponse;
import static com.kuelye.demo.collagerator.gui.UserParsingTaskFragment.ProgressCode.API_NOT_ALLOWED;
import static com.kuelye.demo.collagerator.gui.UserParsingTaskFragment.ProgressCode.EXCEPTION_CAUGHT;
import static com.kuelye.demo.collagerator.gui.UserParsingTaskFragment.ProgressCode.USER_NOT_FOUND;

public class UserParsingTaskFragment extends RetainedAsyncTaskFragment
        <String, UserParsingTaskFragment.ProgressCode
                , UserParsingTaskFragment.ResultHolder, UserParsingTaskFragment.Task> {

    public static final String TAG
            = "com.demo.collagerator.gui.UserParsingTaskFragment";

    private static final String REQUEST_USER_SEARCH_TEMPLATE
            = "https://api.instagram.com/v1/users/search?q=%s&count=1&client_id=%s";
    private static final String REQUEST_USER_INFO_TEMPLATE
            = "https://api.instagram.com/v1/users/%d/?client_id=%s";
    private static final String CLIENT_ID
            = "427b4d3cf46d4ca69af14ab604dd2d05";

    private static final String RESPONSE_DATA_FIELD_NAME                    = "data";
    private static final String RESPONSE_META_FIELD_NAME                    = "meta";
    private static final String RESPONSE_META_ERROR_TYPE_FIELD_NAME         = "error_type";
    private static final String RESPONSE_META_ERROR_TYPE_API_NOT_ALLOWED_ERROR
                                                                            = "APINotAllowedError";
    private static final String RESPONSE_USER_DATA_COUNTS_FIELD_NAME        = "counts";
    private static final String RESPONSE_USER_DATA_COUNTS_MEDIA_FIELD_NAME  = "media";
    private static final String RESPONSE_USER_DATA_ID_FIELD_NAME            = "id";
    private static final String RESPONSE_USER_DATA_USERNAME_FIELD_NAME      = "username";

    @Override
    protected UserParsingTaskFragment.Task createTask() {
        return new UserParsingTaskFragment.Task();
    }

    // -------------------- INNER --------------------

    static enum ProgressCode {

        EMPTY,
        USER_NOT_FOUND,
        API_NOT_ALLOWED,
        EXCEPTION_CAUGHT

    }

    static interface Handler extends RetainedAsyncTaskFragment.Handler
            <ProgressCode, ResultHolder> {

    }

    static class ResultHolder {

        public int userId;
        public int mediaCount;

    }

    static class Task extends RetainedAsyncTaskFragment.Task
            <String, ProgressCode, ResultHolder> {

        @Override
        protected ResultHolder doInBackground(String... params) {
            final String username = params[0];

            try {
                final ResultHolder result = new ResultHolder();

                String request = String.format(REQUEST_USER_SEARCH_TEMPLATE, username, CLIENT_ID);
                JSONObject response = new JSONObject(getResponse(request));
                JSONArray dataArray = response.getJSONArray(RESPONSE_DATA_FIELD_NAME);
                result.userId = -1;

                if (dataArray.length() != 0) {
                    final JSONObject firstUserData = (JSONObject) dataArray.get(0);
                    final String firstUserName = firstUserData.getString(RESPONSE_USER_DATA_USERNAME_FIELD_NAME);
                    if (username.equals(firstUserName)) {
                        result.userId = firstUserData.getInt(RESPONSE_USER_DATA_ID_FIELD_NAME);
                    }
                }

                if (result.userId == -1) {
                    publishProgress(USER_NOT_FOUND);
                    return null;
                }

                request = String.format(REQUEST_USER_INFO_TEMPLATE, result.userId, CLIENT_ID);
                response = new JSONObject(getResponse(request));
                final JSONObject metaObject = response.getJSONObject(RESPONSE_META_FIELD_NAME);
                if (metaObject.has(RESPONSE_META_ERROR_TYPE_FIELD_NAME) &&
                        metaObject.getString(RESPONSE_META_ERROR_TYPE_FIELD_NAME)
                                .equals(RESPONSE_META_ERROR_TYPE_API_NOT_ALLOWED_ERROR)) {
                    publishProgress(API_NOT_ALLOWED);
                }
                JSONObject dataObject = response.getJSONObject(RESPONSE_DATA_FIELD_NAME);
                result.mediaCount = dataObject.getJSONObject(RESPONSE_USER_DATA_COUNTS_FIELD_NAME)
                        .getInt(RESPONSE_USER_DATA_COUNTS_MEDIA_FIELD_NAME);

                return result;
            } catch (IOException e) {
                publishProgress(EXCEPTION_CAUGHT);
                Log.e(TAG, "", e);
            } catch (JSONException e) {
                publishProgress(EXCEPTION_CAUGHT);
                Log.e(TAG, "", e);
            }

            return null;
        }

    }

}