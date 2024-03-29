package com.eacpay.tools.sqlite;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.eacpay.presenter.entities.CurrencyEntity;
import com.eacpay.tools.util.BRConstants;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import timber.log.Timber;

public class CurrencyDataSource implements BRDataSourceInterface {
    private static final String TAG = CurrencyDataSource.class.getName();

    private AtomicInteger mOpenCounter = new AtomicInteger();

    // Database fields
    private SQLiteDatabase database;
    private final BRSQLiteHelper dbHelper;
    private final String[] allColumns = {
            BRSQLiteHelper.CURRENCY_CODE,
            BRSQLiteHelper.CURRENCY_NAME,
            BRSQLiteHelper.CURRENCY_RATE
    };

    private static CurrencyDataSource instance;

    public static CurrencyDataSource getInstance(Context context) {
        if (instance == null) {
            instance = new CurrencyDataSource(context);
        }
        return instance;
    }

    public CurrencyDataSource(Context context) {
        dbHelper = BRSQLiteHelper.getInstance(context);
    }

    public void putCurrencies(Collection<CurrencyEntity> currencyEntities) {
        if (currencyEntities == null || currencyEntities.size() <= 0) return;

        try {
            database = openDatabase();
            database.beginTransaction();
            for (CurrencyEntity c : currencyEntities) {
                ContentValues values = new ContentValues();
                values.put(BRSQLiteHelper.CURRENCY_CODE, c.code);
                values.put(BRSQLiteHelper.CURRENCY_NAME, c.name);
                values.put(BRSQLiteHelper.CURRENCY_RATE, c.rate);
                database.insertWithOnConflict(BRSQLiteHelper.CURRENCY_TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);

            }

            database.setTransactionSuccessful();
        } catch (Exception ex) {
            Timber.e(ex);
            //Error in between database transaction
        } finally {
            database.endTransaction();
            closeDatabase();
        }

    }

    public void deleteAllCurrencies() {
        try {
            database = openDatabase();
            database.delete(BRSQLiteHelper.CURRENCY_TABLE_NAME, BRSQLiteHelper.PEER_COLUMN_ID + " <> -1", null);
        } finally {
            closeDatabase();
        }
    }

    public List<CurrencyEntity> getAllCurrencies() {

        List<CurrencyEntity> currencies = new ArrayList<>();
        Cursor cursor = null;
        try {
            database = openDatabase();

            cursor = database.query(BRSQLiteHelper.CURRENCY_TABLE_NAME,
                    allColumns, null, null, null, null, "\'" + BRSQLiteHelper.CURRENCY_CODE + "\'");

            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                CurrencyEntity curEntity = cursorToCurrency(cursor);
                currencies.add(curEntity);
                cursor.moveToNext();
            }
            // make sure to close the cursor
        } finally {
            if (cursor != null)
                cursor.close();
            closeDatabase();
        }

        return currencies;
    }

    public List<String> getAllISOs() {
        List<String> ISOs = new ArrayList<>();
        Cursor cursor = null;
        try {
            database = openDatabase();

            cursor = database.query(BRSQLiteHelper.CURRENCY_TABLE_NAME,
                    allColumns, null, null, null, null, null);

            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                CurrencyEntity curEntity = cursorToCurrency(cursor);
                ISOs.add(curEntity.code);
                cursor.moveToNext();
            }
            // make sure to close the cursor
        } finally {
            if (cursor != null)
                cursor.close();
            closeDatabase();
        }

        return ISOs;
    }

    public CurrencyEntity getCurrencyByIso(String iso) {
        Cursor cursor = null;
        try {
            database = openDatabase();

            cursor = database.query(BRSQLiteHelper.CURRENCY_TABLE_NAME,
                    allColumns, BRSQLiteHelper.CURRENCY_CODE + " = ?", new String[]{iso}, null, null, null);

            cursor.moveToFirst();
            if (!cursor.isAfterLast()) {
                return cursorToCurrency(cursor);
            }
        } finally {
            if (cursor != null)
                cursor.close();
            closeDatabase();
        }

        return null;
    }

    private CurrencyEntity cursorToCurrency(Cursor cursor) {
        return new CurrencyEntity(cursor.getString(0), cursor.getString(1), cursor.getFloat(2));
    }

    @Override
    public SQLiteDatabase openDatabase() {
//        if (mOpenCounter.incrementAndGet() == 1) {
        // Opening new database
        if (database == null || !database.isOpen())
            database = dbHelper.getWritableDatabase();
        dbHelper.setWriteAheadLoggingEnabled(BRConstants.WAL);
//        }
        return database;
    }

    @Override
    public void closeDatabase() {
//        if (mOpenCounter.decrementAndGet() == 0) {
//            // Closing database
//        database.close();
//
//        }
    }
}