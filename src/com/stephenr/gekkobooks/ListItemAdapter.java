package com.stephenr.gekkobooks;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.stephenr.gekkobooks.dao.generated.ListItem;

public class ListItemAdapter extends BaseAdapter {

    private final List<ListItem> mGroupedObjects;
    private final Context mContext;

    public ListItemAdapter(Context ctx, List<ListItem> items) {
        mContext = ctx;
        mGroupedObjects = items;
    }

    @Override
    public int getCount() {
        return mGroupedObjects.size();
    }

    @Override
    public ListItem getItem(int position) {
        return mGroupedObjects.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView,
            final ViewGroup parent) {

        final ListItem entry = getItem(position);

        View item = convertView;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            item = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
        }

        TextView et = (TextView) item;
        et.setText(entry.getTitle());

        return item;
    }
}