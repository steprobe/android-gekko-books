package com.stephenr.gekkobooks;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.message.BasicNameValuePair;

import android.app.IntentService;
import android.content.Intent;
import android.net.Uri;
import android.net.Uri.Builder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.gson.stream.JsonReader;
import com.stephenr.gekkobooks.dao.generated.ItemDetails;
import com.stephenr.gekkobooks.dao.generated.ListItem;

public class DatabaseUpdateService extends IntentService {

    public static final String DB_LIST_ITEMS_READY = "dblistitemsupdated";
    public static final String DB_LIST_ITEMS_UPDATE_ERROR = "dblistitemsupdatederror";
    public static final String DB_ITEM_DETAIL_READY = "listitemready";
    public static final String DB_ITEM_DETAIL_ERROR = "listitemrettrievalerror";

    private static final String LOG_TAG = DatabaseUpdateService.class.getSimpleName();

    private static final String SCHEME = "http";
    private static final String SERVER = "assignment.golgek.mobi";
    private static final String ITEMS_ENTRY_POINT = "/api/v10/items";

    private static final String ENCODING = "utf-8";

    private DatabaseDelegate mDatabase;

    private static final int READ_TIMEOUT = 20000;
    private static final int CONNECT_TIMEOUT = 20000;

    public static class ServiceCommands {
        public static final String GET_LIST_ITEMS = "getlistitems";
        public static final String GET_ITEM_DETAIL = "getitemdetail";
    }

    public static class ServiceCommandExtras {
        public static final String OFFSET = "offset";
        public static final String COUNT = "count";
        public static final String ITEM_ID = "id";
    }

    public DatabaseUpdateService() {
        super(DatabaseUpdateService.class.getSimpleName());
    }

    public DatabaseUpdateService(String name) {
        super(name);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mDatabase = new DatabaseDelegate(getApplicationContext());
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mDatabase.closeDatabase();
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        String command = intent.getData().toString();
        if(command.equals(ServiceCommands.GET_LIST_ITEMS)) {
            handleGetListItems(intent);
        }
        else if(command.equals(ServiceCommands.GET_ITEM_DETAIL)) {
            handleGetItemDetail(intent);
        }
    }

    private static final String PARAM_OFFSET = "offset";
    private static final String PARAM_COUNT = "count";

    private void handleGetItemDetail(Intent intent) {

        Intent broadcast = null;

        Bundle extras = intent.getExtras();
        Long itemId = extras.getLong(ServiceCommandExtras.ITEM_ID, -1);

        //This is already on a worker thread, so no async task required
        if(retrieveItemDetail(itemId)) {
            broadcast = new Intent(DB_ITEM_DETAIL_READY);
        }
        else {
            broadcast = new Intent(DB_ITEM_DETAIL_ERROR);
        }

        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);
    }

    private void handleGetListItems(Intent intent) {

        Intent broadcast = null;

        Bundle extras = intent.getExtras();
        int count = extras.getInt(ServiceCommandExtras.COUNT, -1);
        int offset = extras.getInt(ServiceCommandExtras.OFFSET, -1);

        //This is already on a worker thread, so no async task required
        if(retrieveListItems(offset, count)) {
            broadcast = new Intent(DB_LIST_ITEMS_READY);
        }
        else {
            broadcast = new Intent(DB_LIST_ITEMS_UPDATE_ERROR);
        }

        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);
    }

    private boolean retrieveItemDetail(Long itemId) {

        try {
            ListItem item = mDatabase.getListItem(itemId);

            if(!mDatabase.isItemDetailUpdateNeeded(itemId)) {
                Log.i(LOG_TAG, "Server request for " + itemId + " not needed - database item is fresh");
                return true;
            }

            Log.i(LOG_TAG, "Loading item details for item " + itemId);

            InputStream data = getJSONStream(item.getLink(), null);
            if(data == null) {
                return false;
            }

            JsonReader reader = new JsonReader(new InputStreamReader(data, ENCODING));
            ItemDetails details = JsonUtils.detailsFromJson(reader);

            File targetFile = getExternalFilesDir(null);
            if(targetFile != null) {
                String localName = nameFromUrl(details.getImage());
                String fullName = targetFile.getAbsolutePath() + "/" + localName;

                try {
                    downloadFile(details.getImage(), fullName);
                    details.setImagefile(fullName);
                }
                catch(IOException ex) {
                    //Continue anyway, just this item wont have an image
                    ex.printStackTrace();
                }
            }

            mDatabase.getDaoParams().getItemDetail().insertOrReplaceInTx(details);
        }
        catch(IOException ex) {
            ex.printStackTrace();
            return false;
        }

        return true;
    }

    private boolean retrieveListItems(int offset, int count) {

        if(!mDatabase.isListItemsUpdateNeeded(offset, count)) {
            return true;
        }

        try {

            List<NameValuePair> params = new ArrayList<NameValuePair>();
            if(offset >= 0) {
                params.add(new BasicNameValuePair(PARAM_OFFSET, Integer.toString(offset)));
            }

            if(count > 0) {
                params.add(new BasicNameValuePair(PARAM_COUNT, Integer.toString(count)));
            }

            Log.i(LOG_TAG, "Connecting to server to load list " + count + " items at offset " + offset);

            InputStream data = getJSONStream(ITEMS_ENTRY_POINT, params);
            if(data == null) {
                return false;
            }

            JsonReader reader = new JsonReader(new InputStreamReader(data, ENCODING));

            List<ListItem> items = new ArrayList<ListItem>();

            reader.beginArray();
            while(reader.hasNext()) {
                items.add(JsonUtils.listItemFromJson(reader));
            }
            reader.endArray();

            Log.v(LOG_TAG, "Loading " + items.size() + " items from the server");

            mDatabase.getDaoParams().getListItem().insertOrReplaceInTx(items);
        }
        catch(IOException ex) {
            ex.printStackTrace();
            return false;
        }

        return true;
    }

    public InputStream getJSONStream(String path, List<NameValuePair> params) {

        try {

            Uri uri = addParamsToUrl(path, params);
            URL url = new URL(uri.toString());
            HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();

            httpCon.setReadTimeout(READ_TIMEOUT);
            httpCon.setConnectTimeout(CONNECT_TIMEOUT);
            httpCon.setRequestMethod(HttpGet.METHOD_NAME);
            httpCon.setDoInput(true);

            httpCon.connect();

            int responseCode = httpCon.getResponseCode();
            if(responseCode != HttpStatus.SC_OK) {
                Log.e(LOG_TAG, "Unable to connect to server: Response code - " + responseCode);
                return null;
            }

            return httpCon.getInputStream();
        }
        catch(IOException ex) {
            Log.e(LOG_TAG, "Unable to connect to server");
            ex.printStackTrace();
            return null;
        }
    }

    protected static Uri addParamsToUrl(String path, List<NameValuePair> params){

        Builder builder = new Uri.Builder().scheme(SCHEME).authority(SERVER).path(path);
        if(params != null) {
            for(NameValuePair param : params) {
                builder = builder.appendQueryParameter(param.getName(), param.getValue());
            }
        }

        return builder.build();
    }

    private void downloadFile(String targetUrl, String localPath) throws IOException {

        File targetFile = new File(localPath);

        URL url = new URL(targetUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setReadTimeout(15000);
        conn.setConnectTimeout(20000);
        conn.setRequestMethod(HttpGet.METHOD_NAME);
        conn.setDoInput(true);

        conn.connect();

        FileOutputStream fos = new FileOutputStream(targetFile);

        InputStream in = conn.getInputStream();

        byte[] buffer = new byte[1024];
        int len = 0;
        while ((len = in.read(buffer)) > 0) {
            fos.write(buffer, 0, len);
        }

        fos.close();
    }

    private String nameFromUrl(String url) {
        File file = new File(url);
        return file.getName();
    }
}
