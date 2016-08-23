package drewmahrt.generalassemb.ly.investingportfolio;

import android.animation.ObjectAnimator;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.support.design.widget.TabLayout;

import java.util.ArrayList;

public class MyDBHandler extends SQLiteOpenHelper {
    private static final Object mLock = new Object();
    private static final int DATABASE_VERSION = 3;
    private static final String DATABASE_NAME = "portfolioDB.db";
    public static final String TABLE_STOCKS = StockPortfolioContract.Stocks.TABLE_STOCKS;

    public static final String COLUMN_ID = BaseColumns._ID;
    public static final String COLUMN_STOCK_SYMBOL = StockPortfolioContract.Stocks.COLUMN_STOCK_SYMBOL;
    public static final String COLUMN_STOCKNAME = StockPortfolioContract.Stocks.COLUMN_STOCKNAME;
    public static final String COLUMN_QUANTITY = StockPortfolioContract.Stocks.COLUMN_QUANTITY;
    public static final String COLUMN_EXCHANGE = StockPortfolioContract.Stocks.COLUMN_EXCHANGE;
    public static final String COLUMN_PRICE = StockPortfolioContract.Stocks.COLUMN_PRICE;

    private static MyDBHandler mInstance;

    public static MyDBHandler getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new MyDBHandler(context);
        }
        return mInstance;
    }

    private MyDBHandler(Context context) {
        super(context.getApplicationContext(), DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_PRODUCTS_TABLE = "CREATE TABLE " +
                TABLE_STOCKS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY,"
                + COLUMN_STOCKNAME + " TEXT,"
                + COLUMN_EXCHANGE + " TEXT,"
                + COLUMN_STOCK_SYMBOL + " TEXT,"
                + COLUMN_PRICE + " TEXT,"
                + COLUMN_QUANTITY + " INTEGER" + ")";
        db.execSQL(CREATE_PRODUCTS_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_STOCKS);
        onCreate(db);
    }

    public long addStock(ContentValues values) {
        SQLiteDatabase db = getWritableDatabase();
        long insertedRow = db.insert(TABLE_STOCKS, null, values);
        db.close();
        return insertedRow;
    }

    public Cursor getStockSymbols(String selection) {
        String[] projection = {COLUMN_STOCK_SYMBOL};
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_STOCKS, projection, selection, null, null, null, null);
        return cursor;
    }

    public Cursor getStocks(String selection) {
        String[] projection = {COLUMN_ID, COLUMN_PRICE, COLUMN_STOCK_SYMBOL, COLUMN_STOCKNAME, COLUMN_EXCHANGE, COLUMN_QUANTITY};

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_STOCKS, projection, selection, null, null, null, null);

        return cursor;
    }

    public int deleteStockById(String id) {
        SQLiteDatabase db = getWritableDatabase();
        int rowsDeleted = db.delete(TABLE_STOCKS, COLUMN_ID + "=?", new String[]{id});
        db.close();
        return rowsDeleted;
    }

    public int updateStock(ContentValues values, String selection, String[] selectionArgs) {
        synchronized (mLock) {
            SQLiteDatabase db = getWritableDatabase();
            int rowsUpdated = 0;

            db.update(TABLE_STOCKS,
                    values,
                    selection,
                    selectionArgs);
            return rowsUpdated;
        }
    }

    public ArrayList<String> getListSymbols() {
        SQLiteDatabase db = getReadableDatabase();
        ArrayList<String> num = null;

        Cursor cursor = db.query(TABLE_STOCKS,
                new String[]{COLUMN_STOCK_SYMBOL},
                null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            num = new ArrayList<>();

            while (!cursor.isAfterLast()) {
                num.add(cursor.getString(cursor.getColumnIndex(COLUMN_STOCK_SYMBOL)));
                cursor.moveToNext();
            }
        }
        db.close();
        return num;
    }
}
