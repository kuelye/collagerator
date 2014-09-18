package com.kuelye.components.async;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;

public class RetainedAsyncTaskFragment<Param, Progress, Result> extends Fragment {

    private Handler<Param, Result>          mHandler;
    private Task<Param, Progress, Result>   mTask;

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

    public void setTask(Task<Param, Progress, Result> task) {
        mTask = task;
    }

    public void startTask(Param... params) {
        cancelTask();

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

    public static interface Handler<Progress, Result> {

        void onPreExecute();
        void onProgressUpdate(Progress progress);
        void onCancelled();
        void onPostExecute(Result result);

    }

    public abstract class Task<Param, Progress, Result>
            extends AsyncTask<Param, Progress, Result> {

        private Handler<Progress, Result> mHandler;

        public void setHandler(Handler<Progress, Result> handler) {
            mHandler = handler;
        }

        public Handler<Progress, Result> getHandler() {
            return mHandler;
        }

    }

}
