package drewmahrt.generalassemb.ly.investingportfolio;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProvider;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SyncResult;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by ander on 8/22/2016.
 */
public class SyncAdapter extends AbstractThreadedSyncAdapter {
    private static final String TAG = "SyncAdapter";

    ContentResolver mContentResolver;
    String mUrl;

    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        mContentResolver = context.getContentResolver();
    }

    public SyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        mContentResolver = context.getContentResolver();
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        Log.i(TAG, "onPerformSync: Inside the onPerformSync");
        // make the call
        // get the json items
        // add to database

        // retrieving all the symbols of each row so all are effected
        final ArrayList<String> arrayList = MyDBHandler.getInstance(getContext()).getListSymbols();

        for (final String symbol : arrayList) {

            // creating the request
            RequestQueue queue = Volley.newRequestQueue(getContext());
            if (symbol == null) {
                mUrl = "http://dev.markitondemand.com/MODApis/Api/v2/Quote/json?symbol=ETSY";
            } else {
                mUrl = "http://dev.markitondemand.com/MODApis/Api/v2/Quote/json?symbol=" + symbol;
            }
            Log.d(MainActivity.class.getName(), "Starting exchange request: " + mUrl);

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, mUrl, null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Log.i(TAG, "onResponse: Request went through");
                    ContentResolver contentResolver = getContext().getContentResolver();
                    try {
                        // collect the value of the json object from the parent
                        String price = response.getString("LastPrice");
                        ContentValues values = new ContentValues();
                        values.put("price", price);
                        Log.i(TAG, "onResponse: $" + price);

                        // add new price to table column where symbol = given symbol
                        contentResolver.update(StockPortfolioContract.Stocks.CONTENT_URI, values,
                                StockPortfolioContract.Stocks.COLUMN_STOCK_SYMBOL + " = ?",
                                new String[]{symbol}
                        );

                        // set date of this object request to the time Textview in Main activity
                        Calendar cal = Calendar.getInstance();
                        cal.add(Calendar.DATE, 1);
                        SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
                        String formatted = format1.format(cal.getTime());
                        MainActivity.timeUpdate(formatted);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.i(TAG, "onErrorResponse: an error");
                }
            });
            queue.add(jsonObjectRequest);
        }
    }
}
