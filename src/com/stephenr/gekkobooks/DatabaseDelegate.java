package com.stephenr.gekkobooks;

import java.util.List;

import junit.framework.Assert;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.stephenr.gekkobooks.dao.generated.DaoMaster;
import com.stephenr.gekkobooks.dao.generated.DaoParameters;
import com.stephenr.gekkobooks.dao.generated.ItemDetails;
import com.stephenr.gekkobooks.dao.generated.ItemDetailsDao;
import com.stephenr.gekkobooks.dao.generated.ListItem;
import com.stephenr.gekkobooks.dao.generated.ListItemDao;

public class DatabaseDelegate {

    private DaoParameters mDaoParams;
    private DaoMaster.DevOpenHelper mHelper;
    private SQLiteDatabase mDb;

    public static final String GB_DB = "gb-db.db";

    public DatabaseDelegate(Context ctx) {
        initializeDb(ctx);
    }

    public void closeDatabase() {
        mHelper.close();
        mDb.close();
    }

    public DaoParameters getDaoParams() {
        return mDaoParams;
    }

    private void initializeDb(Context ctx) {
        mHelper = new DaoMaster.DevOpenHelper(ctx, GB_DB, null);
        mDb = mHelper.getWritableDatabase();
        mDaoParams = new DaoParameters(mDb);
    }

    //one hour
    private static final Long UPDATE_INTERVAL = 3600000L;

    public boolean isListItemsUpdateNeeded(int offset, int count) {

        Assert.assertNotNull("Must initialize delegate first", mDaoParams);

        ListItemDao listDao = mDaoParams.getListItem();
        List<ListItem> items = listDao.queryBuilder().offset(offset).limit(count).list();

        Long currentTime = System.currentTimeMillis();
        for(ListItem item : items) {
            if(currentTime - item.getTimestamp() > UPDATE_INTERVAL) {
                return true;
            }
        }

        //Assuming that if there are any cached items, thats what we will show
        //If more items are added, a force refresh will be required
        return items.size() == 0;
    }

    public boolean isItemDetailUpdateNeeded(Long id) {

        ItemDetailsDao itemDao = mDaoParams.getItemDetail();
        ItemDetails detail = itemDao.load(id);
        if(detail == null) {
            return true;
        }

        Long currentTime = System.currentTimeMillis();
        return currentTime - detail.getTimestamp() > UPDATE_INTERVAL;
    }

    public ListItem getListItem(Long id) {
        ListItemDao listDao = mDaoParams.getListItem();
        return listDao.load(id);
    }
}
