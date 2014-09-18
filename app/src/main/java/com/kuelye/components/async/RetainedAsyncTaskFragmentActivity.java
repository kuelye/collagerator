package com.kuelye.components.async;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

import com.kuelye.demo.collagerator.gui.UserParsingTaskFragment;

public abstract class RetainedAsyncTaskFragmentActivity<TaskFragment extends RetainedAsyncTaskFragment> extends FragmentActivity {

    private static final String TAG_USER_ID_TASK_FRAGMENT = "USER_ID_TASK_FRAGMENT";

    private TaskFragment mTaskFragment;

    protected abstract TaskFragment createTaskFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final FragmentManager fragmentManager = getSupportFragmentManager();
        //noinspection unchecked
        mTaskFragment = (TaskFragment) fragmentManager
                .findFragmentByTag(TAG_USER_ID_TASK_FRAGMENT);
        if (mTaskFragment == null) {
            mTaskFragment = createTaskFragment();
            fragmentManager.beginTransaction()
                    .add(mTaskFragment, TAG_USER_ID_TASK_FRAGMENT).commit();
        }
    }

    protected TaskFragment getTaskFragment() {
        return mTaskFragment;
    }

}
