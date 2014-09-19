package com.kuelye.components.async;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;

public abstract class RetainedAsyncTaskFragment<Param, Progress, Result, Task
        extends RetainedAsyncTaskFragment.Task<Param, Progress, Result>> extends Fragment {

    private Handler<Progress, Result>   mHandler;
    private Task                        mTask;

    protected abstract Task createTask();

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        //noinspection unchecked
        mHandler = (Handler) activity;
        if (mTask != null) {
            mTask.setHandler(mHandler);
        }
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

    public void startTask(Param... params) {
        cancelTask();

        mTask = createTask();
        mTask.setHandler(mHandler);
        mTask.execute(params);
    }

    public void cancelTask() {
        if (mTask != null) {
            mTask.cancel(true);
        }
    }

    public boolean isExecuted() {
        return mTask != null && mTask.getStatus() == AsyncTask.Status.RUNNING ;
    }

    // -------------------- INNER --------------------

    public static interface Handler<Progress, Result> {

        void onProgressUpdate(Progress... values);
        void onCancelled(Result result);
        void onPostExecute(Result result);

    }

    public static abstract class Task<Param, Progress, Result>
            extends AsyncTask<Param, Progress, Result> {

        private Handler<Progress, Result> mHandler;

        public void setHandler(Handler<Progress, Result> handler) {
            mHandler = handler;
        }

        @Override
        protected void onProgressUpdate(Progress... values) {
            if (mHandler != null) {
                mHandler.onProgressUpdate(values);
            }
        }

        @Override
        protected void onCancelled(Result result) {
            if (mHandler != null) {
                mHandler.onCancelled(result);
            }
        }

        @Override
        protected void onPostExecute(Result result) {
            if (mHandler != null) {
                mHandler.onPostExecute(result);
            }
        }

    }

}
