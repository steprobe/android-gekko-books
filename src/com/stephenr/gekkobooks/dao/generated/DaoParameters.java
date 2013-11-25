package com.stephenr.gekkobooks.dao.generated;

import android.database.sqlite.SQLiteDatabase;

public class DaoParameters {

    private final DaoMaster mMaster;
    private final DaoSession mSession;
    private final ListItemDao mListItems;
    private final ItemDetailsDao mItemDetails;

    public DaoParameters(SQLiteDatabase dataBase) {
        mMaster = new DaoMaster(dataBase);
        mSession = mMaster.newSession();
        mListItems = mSession.getListItemDao();
        mItemDetails = mSession.getItemDetailsDao();
    }

    public ListItemDao getListItem() {
        return mListItems;
    }

    public ItemDetailsDao getItemDetail() {
        return mItemDetails;
    }
}
