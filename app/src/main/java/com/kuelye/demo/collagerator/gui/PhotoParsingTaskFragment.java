package com.kuelye.demo.collagerator.gui;

import android.util.Log;

import com.kuelye.components.async.RetainedAsyncTaskFragment;
import com.kuelye.demo.collagerator.instagram.InstagramMedia;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.kuelye.components.utils.NetworkUtils.getResponse;
import static com.kuelye.demo.collagerator.gui.PhotoParsingTaskFragment.ProgressCode.EXCEPTION_CAUGHT;

public class PhotoParsingTaskFragment extends RetainedAsyncTaskFragment
        <PhotoParsingTaskFragment.ParamsHolder, PhotoParsingTaskFragment.ProgressCode
                , PhotoParsingTaskFragment.ResultHolder, PhotoParsingTaskFragment.Task> {

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

    @Override
    protected PhotoParsingTaskFragment.Task createTask() {
        return new PhotoParsingTaskFragment.Task();
    }

    // -------------------- INNER --------------------

    static interface Handler extends RetainedAsyncTaskFragment.Handler
            <ProgressCode, ResultHolder> {

    }

    static class ParamsHolder {

        public ParamsHolder(int userId, String nextMaxId) {
            this.userId = userId;
            this.nextMaxId = nextMaxId;
        }

        public int      userId;
        public String   nextMaxId;

    }

    static enum ProgressCode {

        EMPTY,
        EXCEPTION_CAUGHT

    }

    static class ResultHolder {

        public List<InstagramMedia> nextPhotos;
        public String               nextMaxId;
        public int                  processedCount;

    }

    static class Task extends RetainedAsyncTaskFragment.Task
            <ParamsHolder, ProgressCode, ResultHolder> {

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
                publishProgress(EXCEPTION_CAUGHT);
                Log.e(TAG, "", e);
            } catch (JSONException e) {
                publishProgress(EXCEPTION_CAUGHT);
                Log.e(TAG, "", e);
            }

            return null;
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