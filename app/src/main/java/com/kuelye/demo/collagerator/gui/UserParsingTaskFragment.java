package com.kuelye.demo.collagerator.gui;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.kuelye.components.async.RetainedAsyncTaskFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import static com.kuelye.components.utils.NetworkUtils.getResponse;
import static com.kuelye.demo.collagerator.gui.UserParsingTaskFragment.ProgressCode.EXCEPTION_CATCHED;
import static com.kuelye.demo.collagerator.gui.UserParsingTaskFragment.ProgressCode.USER_NOT_FOUND;

public class UserParsingTaskFragment extends RetainedAsyncTaskFragment
        <String, UserParsingTaskFragment.ProgressCode, UserParsingTaskFragment.ResultHolder> {

    private static final String TAG
            = "com.demo.collagerator.gui.UserParsingTaskFragment";

    private static final String REQUEST_USER_SEARCH_TEMPLATE
            = "https://api.instagram.com/v1/users/search?q=%s&count=1&client_id=%s";
    private static final String REQUEST_USER_INFO_TEMPLATE
            = "https://api.instagram.com/v1/users/%d/?client_id=%s";
    private static final String CLIENT_ID
            = "427b4d3cf46d4ca69af14ab604dd2d05";

    private static final String RESPONSE_DATA_FIELD_NAME                    = "data";
    private static final String RESPONSE_USER_DATA_COUNTS_FIELD_NAME        = "counts";
    private static final String RESPONSE_USER_DATA_COUNTS_MEDIA_FIELD_NAME  = "media";
    private static final String RESPONSE_USER_DATA_ID_FIELD_NAME            = "id";
    private static final String RESPONSE_USER_DATA_USERNAME_FIELD_NAME      = "username";

    private Handler     mHandler;
    private UserIdParsingTask mTask;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        mHandler = (Handler) activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);
    }

    @Override
    public void onDetach() {
        super.onDetach();

        mHandler = null;
    }

    public void startTask(String username) {
        cancelTask();

        mTask = new UserIdParsingTask();
        mTask.execute(username);
    }

    public void cancelTask() {
        if (mTask != null) {
            mTask.cancel(true);
        }
    }

    public boolean isExecuted() {
        return mTask != null;
    }

    // -------------------- INNER --------------------

    static interface Handler {
        void onPreExecute();
        void onProgressUpdate(ProgressCode progressCode);
        void onCancelled();
        void onPostExecute(ResultHolder resultHolder);
    }

    static enum ProgressCode {

        EMPTY,
        USER_NOT_FOUND,
        EXCEPTION_CATCHED

    }

    static class ResultHolder {

        public int userId;
        public int mediaCount;

    }

    private class UserIdParsingTask extends AsyncTask<String, ProgressCode, ResultHolder> {

        @Override
        protected void onPreExecute() {
            if (mHandler != null) {
                mHandler.onPreExecute();
            }
        }

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
                JSONObject dataObject = response.getJSONObject(RESPONSE_DATA_FIELD_NAME);
                result.mediaCount = dataObject.getJSONObject(RESPONSE_USER_DATA_COUNTS_FIELD_NAME)
                        .getInt(RESPONSE_USER_DATA_COUNTS_MEDIA_FIELD_NAME);

                return result;
            } catch (IOException e) {
                publishProgress(EXCEPTION_CATCHED);
                Log.e(TAG, "", e);
            } catch (JSONException e) {
                publishProgress(EXCEPTION_CATCHED);
                Log.e(TAG, "", e);
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(ProgressCode... values) {
            if (mHandler != null) {
                final ProgressCode progressCode = values[0];

                mHandler.onProgressUpdate(progressCode);
            }
        }

        @Override
        protected void onCancelled() {
            mTask = null;

            if (mHandler != null) {
                mHandler.onCancelled();
            }
        }

        @Override
        protected void onPostExecute(ResultHolder resultHolder) {
            mTask = null;

            if (mHandler != null) {
                mHandler.onPostExecute(resultHolder);
            }
        }

    }

}