package com.ipleomax.billshare;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.SimpleDateFormat;

/**
 * Created by iPLEOMAX on 03-Jul-18.
 */

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "billshare.db";
    public static final String TABLE_NAME = "bills"; //was bills
    public static final String[] COLUMNS_NAMES = {"id", "number", "amount", "date"};

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 2);
        SQLiteDatabase db = getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String sql = "CREATE TABLE " + TABLE_NAME + "(" + COLUMNS_NAMES[0] + " INTEGER PRIMARY KEY, ";
        sql += COLUMNS_NAMES[1] + " INTEGER, ";
        sql += COLUMNS_NAMES[2] + " DOUBLE, ";
        sql += COLUMNS_NAMES[3] + " DATE)";

        Log.i("SQL", sql);
        sqLiteDatabase.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE " + TABLE_NAME);
        onCreate(db);
    }

    public long insertBill(Bill bill) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMNS_NAMES[1], bill.number);
        contentValues.put(COLUMNS_NAMES[2], bill.amount);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        contentValues.put(COLUMNS_NAMES[3], dateFormat.format(bill.date));

        return db.insert(TABLE_NAME, null, contentValues);
    }

    public int updateBill(Bill bill) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(COLUMNS_NAMES[1], bill.number);
        contentValues.put(COLUMNS_NAMES[2], bill.amount);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        contentValues.put(COLUMNS_NAMES[3], dateFormat.format(bill.date));

        return db.update(TABLE_NAME, contentValues, COLUMNS_NAMES[0] + " = ?", new String[]{Long.toString(bill.id)});
    }

    public Cursor selectAllBills() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);
        return res;
    }

    public int removeBill(Bill bill) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_NAME, COLUMNS_NAMES[0] + " = ?", new String[] {Long.toString(bill.id)});
    }
}
