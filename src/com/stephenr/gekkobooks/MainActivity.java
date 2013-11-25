package com.stephenr.gekkobooks;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.view.Window;

public class MainActivity extends ActionBarActivity {

    private static final String BUNDLE_LIST_UP = "listup";

    private boolean mListReady;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        supportRequestWindowFeature(Window.FEATURE_PROGRESS);
        supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        setContentView(R.layout.activity_main);

        if(savedInstanceState != null) {
            mListReady = savedInstanceState.getBoolean(BUNDLE_LIST_UP);
        }

        if(mListReady) {
            return;
        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        ListFragment fragment = new ListFragment();

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.rootView, fragment);
        fragmentTransaction.commit();

        mListReady = true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(BUNDLE_LIST_UP, mListReady);
    }

    public void showItemDetail(Long itemId) {

        FragmentManager fm = getSupportFragmentManager();
        DetailDialogFragment detailsDialog = DetailDialogFragment.newInstance(itemId);
        detailsDialog.show(fm, DetailDialogFragment.class.getSimpleName());
    }
}
