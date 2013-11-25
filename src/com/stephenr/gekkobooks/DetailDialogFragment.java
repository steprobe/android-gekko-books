package com.stephenr.gekkobooks;

import java.text.NumberFormat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.stephenr.gekkobooks.DatabaseUpdateService.ServiceCommandExtras;
import com.stephenr.gekkobooks.DatabaseUpdateService.ServiceCommands;
import com.stephenr.gekkobooks.dao.generated.ItemDetails;
import com.stephenr.gekkobooks.dao.generated.ItemDetailsDao;

public class DetailDialogFragment extends DialogFragment {

    private DatabaseDelegate mDatabaseDelegate;

    private static final String BUNDLE_ITEM_ID = "bunitemid";
    private static final String ARG_ITEM_ID = "argitemid";

    private Long mItemId;

    public static DetailDialogFragment newInstance(Long id) {

        Bundle args = new Bundle();
        args.putLong(ARG_ITEM_ID, id);

        DetailDialogFragment fraggle = new DetailDialogFragment();
        fraggle.setArguments(args);

        return fraggle;
    }

    public DetailDialogFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(savedInstanceState != null) {
            mItemId = savedInstanceState.getLong(BUNDLE_ITEM_ID);
        }
        else {
            Bundle args = getArguments();
            mItemId = args.getLong(ARG_ITEM_ID);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        bundle.putLong(BUNDLE_ITEM_ID, mItemId);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.details_dialog, container);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle arg0) {
        super.onActivityCreated(arg0);
        mDatabaseDelegate = new DatabaseDelegate(getActivity());
    }

    @Override
    public void onResume() {
        super.onResume();

        IntentFilter filter = new IntentFilter();
        filter.addAction(DatabaseUpdateService.DB_ITEM_DETAIL_ERROR);
        filter.addAction(DatabaseUpdateService.DB_ITEM_DETAIL_READY);

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mDbBroadcastReceiver, filter);

        requestServiceForData();
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mDbBroadcastReceiver);
    }

    private void requestServiceForData() {

        Intent serviceIntent = new Intent(getActivity(), DatabaseUpdateService.class);
        serviceIntent.setData(Uri.parse(ServiceCommands.GET_ITEM_DETAIL));
        serviceIntent.putExtra(ServiceCommandExtras.ITEM_ID, mItemId);

        getActivity().startService(serviceIntent);

        showProgress(true);
    }

    private final BroadcastReceiver mDbBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            if(intent.getAction().equals(DatabaseUpdateService.DB_ITEM_DETAIL_READY)) {
                new DatabaseLoader().execute();
            }
            else if(intent.getAction().equals(DatabaseUpdateService.DB_ITEM_DETAIL_ERROR)) {
                Toast.makeText(getActivity(), R.string.error_loading, Toast.LENGTH_SHORT).show();
                dismiss();
            }
        }
    };

    private class DatabaseLoader extends AsyncTask<Void, Void, Boolean> {

        private ItemDetails mLoadingResult;

        @Override
        protected Boolean doInBackground(Void... params) {

            ItemDetailsDao listDao = mDatabaseDelegate.getDaoParams().getItemDetail();
            mLoadingResult = listDao.load(mItemId);

            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);

            showProgress(false);

            TextView title = (TextView) getDialog().findViewById(android.R.id.title);
            title.setGravity(Gravity.CENTER);
            title.setSingleLine(false);

            getDialog().setTitle(mLoadingResult.getTitle());

            TextView priceView = (TextView) getView().findViewById(R.id.priceTitleText);

            NumberFormat df = NumberFormat.getCurrencyInstance();
            priceView.setText(df.format(mLoadingResult.getPrice()));

            TextView authorView = (TextView) getView().findViewById(R.id.authorTitleText);
            authorView.setText(mLoadingResult.getAuthor());

            ImageView iv = (ImageView)getView().findViewById(R.id.previewImage);
            String imageFile = mLoadingResult.getImagefile();

            //Could be null if no SD card (emulator for eg)
            if(imageFile != null) {
                iv.setImageURI(Uri.parse(mLoadingResult.getImagefile()));
            }
        }
    }

    private void showProgress(boolean show) {

        View prog = getView().findViewById(R.id.loadingLayer);
        View root = getView().findViewById(R.id.dataLayout);

        if(show) {
            prog.setVisibility(View.VISIBLE);
            root.setVisibility(View.INVISIBLE);
        }
        else {
            prog.setVisibility(View.GONE);
            root.setVisibility(View.VISIBLE);
        }
    }
}
