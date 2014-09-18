package com.kuelye.demo.collagerator.gui;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.kuelye.demo.collagerator.instagram.InstagramMedia;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.kuelye.components.utils.NetworkUtils.getResponse;
import static com.kuelye.demo.collagerator.gui.PhotoParsingTaskFragment.ProgressCode.EXCEPTION_CATCHED;

public class PhotoParsingTaskFragment extends Fragment {

    private static final String TAG
            = "com.demo.collagerator.gui.PhotosParsingTaskFragment";

    private static final String REQUEST_USER_MEDIA_RECENT_TEMPLATE
            = "https://api.instagram.com/v1/users/%d/media/recent/?count=%d&max_id=%s&client_id=%s";
    private static final String CLIENT_ID
            = "427b4d3cf46d4ca69af14ab604dd2d05";
    private static final int    MEDIA_COUNT_PER_REQUEST
            = 100;

    private static final String RESPONSE_PAGINATION_FIELD_NAME                  = "pagination";
    private static final String RESPONSE_PAGINATION_NEXT_MAX_ID_FIELD_NAME      = "next_max_id";
    private static final String RESPONSE_DATA_FIELD_NAME                        = "data";
    private static final String RESPONSE_MEDIA_DATA_TYPE_FIELD_NAME             = "type";
    private static final String RESPONSE_MEDIA_DATA_IMAGES_FIELD_NAME           = "images";
    private static final String RESPONSE_MEDIA_DATA_IMAGES_THUMBNAIL_FIELD_NAME = "thumbnail";
    private static final String RESPONSE_MEDIA_DATA_IMAGES_STANDARD_RESOLUTION_FIELD_NAME
            = "standard_resolution";
    private static final String RESPONSE_MEDIA_DATA_IMAGES_URL_FIELD_NAME       = "url";
    private static final String RESPONSE_MEDIA_DATA_LIKES_FIELD_NAME            = "likes";
    private static final String RESPONSE_MEDIA_DATA_LIKES_COUNT_FIELD_NAME      = "count";

    private static final String RESPONSE_MEDIA_TYPE_IMAGE                       = "image";
    private static final String RESPONSE_PAGINATION_NEXT_MAX_ID_MIN             = "0";

    private Handler             mHandler;
    private PhotoParsingTask    mTask;

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

    public void startTask(Integer userId, String nextMaxId) {
        cancelTask();

        mTask = new PhotoParsingTask();
        final ParamsHolder params = new ParamsHolder();
        params.userId = userId;
        params.nextMaxId = nextMaxId;
        mTask.execute(params);
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

    static class ParamsHolder {

        public int      userId;
        public String   nextMaxId;

    }

    static enum ProgressCode {

        EMPTY,
        EXCEPTION_CATCHED

    }

    static class ResultHolder {

        public List<InstagramMedia> nextPhotos;
        public String               nextMaxId;
        public int                  processedCount;

    }

    private class PhotoParsingTask extends AsyncTask<ParamsHolder, ProgressCode, ResultHolder> {

        @Override
        protected void onPreExecute() {
            if (mHandler != null) {
                mHandler.onPreExecute();
            }
        }

        @Override
        protected ResultHolder doInBackground(ParamsHolder... params) {
            final ParamsHolder paramsHolder = params[0];

            try {
                final ResultHolder resultHolder = new ResultHolder();

                final String request = String.format(REQUEST_USER_MEDIA_RECENT_TEMPLATE, paramsHolder.userId
                        , MEDIA_COUNT_PER_REQUEST, paramsHolder.nextMaxId, CLIENT_ID);
                final JSONObject response = new JSONObject(getResponse(request));
                final JSONArray dataArray = response.getJSONArray(RESPONSE_DATA_FIELD_NAME);

                resultHolder.nextPhotos = parseMediaData(dataArray);
                resultHolder.processedCount = dataArray.length();

                JSONObject pagination = response.getJSONObject(RESPONSE_PAGINATION_FIELD_NAME);
                if (pagination.has(RESPONSE_PAGINATION_NEXT_MAX_ID_FIELD_NAME)) {
                    resultHolder.nextMaxId = pagination.getString(RESPONSE_PAGINATION_NEXT_MAX_ID_FIELD_NAME);
                } else {
                    resultHolder.nextMaxId = RESPONSE_PAGINATION_NEXT_MAX_ID_MIN;
                }

                return resultHolder;
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

        // ------------------- PRIVATE -------------------

        private List<InstagramMedia> parseMediaData(JSONArray data) throws JSONException {
            final List<InstagramMedia> result = new ArrayList<InstagramMedia>();

            for (int i = 0, l = data.length(); i < l; ++i) {
                JSONObject media = (JSONObject) data.get(i);
                if (media.getString(RESPONSE_MEDIA_DATA_TYPE_FIELD_NAME)
                        .equals(RESPONSE_MEDIA_TYPE_IMAGE)) {
                    JSONObject images = media.getJSONObject(RESPONSE_MEDIA_DATA_IMAGES_FIELD_NAME);
                    String thumbnailImageUrl = images
                            .getJSONObject(RESPONSE_MEDIA_DATA_IMAGES_THUMBNAIL_FIELD_NAME)
                            .getString(RESPONSE_MEDIA_DATA_IMAGES_URL_FIELD_NAME);
                    String standardResolutionImageUrl = images
                            .getJSONObject(RESPONSE_MEDIA_DATA_IMAGES_STANDARD_RESOLUTION_FIELD_NAME)
                            .getString(RESPONSE_MEDIA_DATA_IMAGES_URL_FIELD_NAME);
                    int likesCount = media.getJSONObject(RESPONSE_MEDIA_DATA_LIKES_FIELD_NAME)
                            .getInt(RESPONSE_MEDIA_DATA_LIKES_COUNT_FIELD_NAME);
                    result.add(new InstagramMedia(thumbnailImageUrl, standardResolutionImageUrl, likesCount));
                }
            }

            return result;
        }

    }

}