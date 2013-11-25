package com.stephenr.gekkobooks;

import java.util.ArrayList;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.stephenr.gekkobooks.DatabaseUpdateService.ServiceCommandExtras;
import com.stephenr.gekkobooks.DatabaseUpdateService.ServiceCommands;
import com.stephenr.gekkobooks.dao.generated.ListItem;
import com.stephenr.gekkobooks.dao.generated.ListItemDao;

public class ListFragment extends Fragment implements OnScrollListener{

    private boolean mRequestOutstanding;
    private DatabaseDelegate mDatabaseDelegate;

    private static final int PAGING_SIZE = 50;
    private int mOffset;

    private ListItemAdapter mAdapter;
    private ListView mListView;

    private final List<ListItem> mItems = new ArrayList<ListItem>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
        Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.list_fragment, container, false);

        mListView = (ListView) view.findViewById(R.id.listView);
        mListView.setOnScrollListener(this);
        mListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapter, View view, int position,
                    long id) {
                ((MainActivity)getActivity()).showItemDetail(mItems.get(position).getId());
            }

        });

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mAdapter = new ListItemAdapter(getActivity(), mItems);
        mListView.setAdapter(mAdapter);

        mDatabaseDelegate = new DatabaseDelegate(getActivity());

        if(!mRequestOutstanding) {
            requestServiceForData(PAGING_SIZE, mOffset);
        }
    }

    private void requestServiceForData(int count, int offset) {

        mRequestOutstanding = true;

        ActionBarActivity act = (ActionBarActivity)getActivity();
        act.setSupportProgressBarIndeterminateVisibility(true);

        Intent serviceIntent = new Intent(getActivity(), DatabaseUpdateService.class);
        serviceIntent.setData(Uri.parse(ServiceCommands.GET_LIST_ITEMS));

        serviceIntent.putExtra(ServiceCommandExtras.COUNT, count);
        serviceIntent.putExtra(ServiceCommandExtras.OFFSET, offset);

        getActivity().startService(serviceIntent);
    }

    @Override
    public void onResume() {
        super.onResume();

        IntentFilter filter = new IntentFilter();
        filter.addAction(DatabaseUpdateService.DB_LIST_ITEMS_READY);
        filter.addAction(DatabaseUpdateService.DB_LIST_ITEMS_UPDATE_ERROR);

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mDbBroadcastReceiver, filter);
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mDbBroadcastReceiver);
    }

    private final BroadcastReceiver mDbBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            mRequestOutstanding = false;

            ActionBarActivity act = (ActionBarActivity)getActivity();
            act.setSupportProgressBarIndeterminateVisibility(false);

            if(intent.getAction().equals(DatabaseUpdateService.DB_LIST_ITEMS_READY)) {
                new DatabaseLoader().execute();
            }
            else if(intent.getAction().equals(DatabaseUpdateService.DB_LIST_ITEMS_UPDATE_ERROR)) {
                Toast.makeText(getActivity(), R.string.error_loading, Toast.LENGTH_SHORT).show();
            }
        }
    };

    private class DatabaseLoader extends AsyncTask<Void, Void, Boolean> {

        private List<ListItem> mLoadingResults;

        @Override
        protected Boolean doInBackground(Void... params) {

            ListItemDao listDao = mDatabaseDelegate.getDaoParams().getListItem();
            mLoadingResults = listDao.queryBuilder().offset(mOffset).limit(PAGING_SIZE).list();

            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);

            for(ListItem item : mLoadingResults) {
                mItems.add(item);
            }

            if(mLoadingResults.size() != 0) {
                mOffset += PAGING_SIZE;
                mAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId() == R.id.action_refresh) {

            //Delete items from database for a full refresh
            mDatabaseDelegate.getDaoParams().getListItem().deleteAll();
            mDatabaseDelegate.getDaoParams().getItemDetail().deleteAll();

            mOffset = 0;

            mItems.clear();
            mAdapter.notifyDataSetChanged();

            requestServiceForData(PAGING_SIZE, mOffset);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem,
            int visibleItemCount, int totalItemCount) {

        if(firstVisibleItem + visibleItemCount >= mOffset) {
            //Load some more data
            if(!mRequestOutstanding) {
                requestServiceForData(PAGING_SIZE, mOffset);
            }
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) { }
}
