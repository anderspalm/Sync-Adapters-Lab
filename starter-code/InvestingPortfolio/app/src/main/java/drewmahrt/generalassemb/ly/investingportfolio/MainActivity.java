package drewmahrt.generalassemb.ly.investingportfolio;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
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
import java.util.Calendar;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = "MainActivity";
    public static final Uri CONTENT_URI = StockPortfolioContract.Stocks.CONTENT_URI;
    public static final int LOADER_STOCK = 0;
    public static final String AUTHORITY = StockPortfolioContract.AUTHORITY;

    ListView mPortfolioListView;
    CursorAdapter mCursorAdapter;
    Account mAccount;
    static TextView mTimeView;
    static TextView mText1;
    static TextView mText2;

    public static final String ACCOUNT_TYPE = "example.com";
    // Account
    public static final String ACCOUNT = "default_account";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mTimeView = (TextView) findViewById(R.id.time_now);

        if (MyDBHandler.getInstance(this).getListSymbols() == null ) {
            ContentValues values = new ContentValues();
            values.put("price", "109.35");
            values.put("symbol", "APPL");
            values.put("stockname", "Apple Inc");
            values.put("exchange", "NASDAQ");
            values.put("quantity", 120);
            ContentResolver contentResolver1 = getContentResolver();
            contentResolver1.insert(CONTENT_URI, values);
        }

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, 0);
        mTimeView.setText(cal.getTime().toString());

        mAccount = createSyncAccount(this);

        mPortfolioListView = (ListView) findViewById(R.id.portfolio_list);

        mCursorAdapter = new CursorAdapter(this, null, 0) {
            @Override
            public View newView(Context context, Cursor cursor, ViewGroup parent) {
                return LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_2, parent, false);
            }

            @Override
            public void bindView(View view, Context context, Cursor cursor) {
                mText1 = (TextView) view.findViewById(android.R.id.text1);
                mText2 = (TextView) view.findViewById(android.R.id.text2);
                String name = cursor.getString(cursor.getColumnIndex("stockname"));
                String symbol = cursor.getString(cursor.getColumnIndex("symbol"));
                String price = cursor.getString(cursor.getColumnIndex("price"));
                String index = cursor.getString(cursor.getColumnIndex("exchange"));
                mText1.setText(name + " (" + symbol + ")");
                mText2.setText("Price: $" + price);

                if(index.equals("NASDAQ")) {
                    mText1.setBackgroundColor(getResources().getColor(android.R.color.holo_purple));
                    mText2.setBackgroundColor(getResources().getColor(android.R.color.holo_purple));
                }
            }
        };

        mPortfolioListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                getContentResolver().delete(ContentUris.withAppendedId(CONTENT_URI, id), null, null);
                return false;
            }
        });

        mPortfolioListView.setAdapter(mCursorAdapter);

        getSupportLoaderManager().initLoader(LOADER_STOCK, null, this);

        Bundle settingsBundle = new Bundle();
        settingsBundle.putBoolean(
                ContentResolver.SYNC_EXTRAS_MANUAL, true);
        settingsBundle.putBoolean(
                ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
                /*
                 * Request the sync for the default account, authority, and
                 * manual sync settings
                 */
        ContentResolver.requestSync(mAccount, AUTHORITY, settingsBundle);

        ContentResolver.setSyncAutomatically(mAccount, AUTHORITY, true);
        ContentResolver.addPeriodicSync(mAccount, AUTHORITY, Bundle.EMPTY, 60);
    }


//-------------   END OF ONCREATE   -------------//

    public static Account createSyncAccount(Context context) {
        Account newAccount = new Account(
                ACCOUNT, ACCOUNT_TYPE);
        AccountManager accountManager = (AccountManager) context.getSystemService(ACCOUNT_SERVICE);
        if (accountManager.addAccountExplicitly(newAccount, null, null)) {
        } else {
        }
        return newAccount;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case LOADER_STOCK:
                return new CursorLoader(this,
                        StockPortfolioContract.Stocks.CONTENT_URI,
                        null,
                        null,
                        null,
                        null);
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mCursorAdapter.changeCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursorAdapter.changeCursor(null);
    }

    public static void timeUpdate(String time) {
        mTimeView.setText("Last Updated " + time);

    }


}
//
//    public void retrieveStock(final String symbol, final String quantity){
//
//        RequestQueue queue = Volley.newRequestQueue(this);
//        String stockUrl = "http://dev.markitondemand.com/MODApis/Api/v2/Quote/json?symbol="+symbol;
//
//        JsonObjectRequest stockJsonRequest = new JsonObjectRequest
//                (Request.Method.GET, stockUrl, null, new Response.Listener<JSONObject>() {
//
//                    @Override
//                    public void onResponse(JSONObject response) {
//                        Log.d(MainActivity.class.getName(),"Response: "+response.toString());
//                        try {
//                            if(response.has("Status") && response.getString("Status").equals("SUCCESS")) {
//                                retrieveExchange(symbol,quantity,response.getString("Name"));
//
//                            }else{
//                                Toast.makeText(MainActivity.this,"The stock you entered is invalid",Toast.LENGTH_LONG).show();
//                            }
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }, new Response.ErrorListener() {
//
//                    @Override
//                    public void onErrorResponse(VolleyError error) {
//                        // TODO Auto-generated method stub
//                        Log.d(MainActivity.class.getName(),error.toString());
//                    }
//                });
//
//        queue.add(stockJsonRequest);
//
//    }
//
//    public void retrieveExchange(final String symbol, final String quantity, final String name){
//
//        RequestQueue queue = Volley.newRequestQueue(this);
//        String newExchangeUrl = "http://dev.markitondemand.com/MODApis/Api/v2/Quote/json?symbol=" + symbol;
//
//        Log.d(MainActivity.class.getName(),"Starting exchange request: "+newExchangeUrl);
//
//        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, newExchangeUrl, null, new Response.Listener<JSONObject>() {
//            @Override
//            public void onResponse(JSONObject response) {
//                Log.i(TAG, "onResponse: Request went through");
//                ContentResolver contentResolver = getContentResolver();
//                try {
//                    String price = response.getString("LastPrice");
//                    ContentValues values = new ContentValues();
//                    values.put("symbol", symbol);
//                    values.put("stockname", name);
//                    values.put("quantity", quantity);
//                    values.put("price", price);
//                    Log.i(TAG, "onResponse: " + price);
//                    contentResolver.insert(CONTENT_URI,values);
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            }
//        }, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError error) {
//                Log.i(TAG, "onErrorResponse: an error");
//            }
//        });
//        queue.add(jsonObjectRequest);
//    }
//
//    private void createDialog(){
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        // Get the layout inflater
//        LayoutInflater inflater = this.getLayoutInflater();
//
//        // Inflate and set the layout for the dialog
//        // Pass null as the parent view because its going in the dialog layout
//        builder.setView(inflater.inflate(R.layout.add_stock_dialog, null))
//                // Add action buttons
//                .setPositiveButton("Add", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int id) {
//                        Boolean isValid = true;
//                        EditText symbolText = (EditText)((AlertDialog)dialog).findViewById(R.id.stock_symbol_edittext);
//                        EditText quantityText = (EditText)((AlertDialog)dialog).findViewById(R.id.quantity_edittext);
//                        if(symbolText.getText().toString().length() == 0 || quantityText.getText().toString().length() == 0){
//                            Toast.makeText(MainActivity.this,"You must complete all fields",Toast.LENGTH_LONG).show();
//                            isValid = false;
//                        }else{
//                            symbolText.setError("");
//                        }
//
//                        if(isValid){
//                            retrieveStock(symbolText.getText().toString().toUpperCase(),quantityText.getText().toString());
//                        }
//                    }
//                })
//                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int id) {
//
//                    }
//                });
//        AlertDialog dialog = builder.create();
//        dialog.show();
//    }


