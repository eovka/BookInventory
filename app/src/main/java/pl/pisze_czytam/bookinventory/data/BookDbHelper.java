package pl.pisze_czytam.bookinventory.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static pl.pisze_czytam.bookinventory.data.BookContract.*;

public class BookDbHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "bookstore.db";
    private static final int DATABASE_VERSION = 1;

    public BookDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String TEXT_TYPE = " TEXT";
        String INTEGER_TYPE = " INTEGER";
        String REAL_TYPE = " REAL";
        String COMMA_SEP = ", ";

        String SQL_CREATE_BOOKS = "CREATE TABLE " + BookEntry.TABLE_NAME + " ("
                + BookEntry.ID + INTEGER_TYPE + " PRIMARY KEY" + " AUTOINCREMENT" + COMMA_SEP
                + BookEntry.COLUMN_TITLE + TEXT_TYPE + " NOT NULL" + COMMA_SEP
                + BookEntry.COLUMN_AUTHOR + TEXT_TYPE + COMMA_SEP
                + BookEntry.COLUMN_PRICE + REAL_TYPE + COMMA_SEP
                + BookEntry.COLUMN_QUANTITY + INTEGER_TYPE + COMMA_SEP
                + BookEntry.COLUMN_SUPPLIER + TEXT_TYPE + ");";

        String SQL_CREATE_SUPPLIERS = "CREATE TABLE " + SupplierEntry.TABLE_NAME + " ("
                + SupplierEntry.ID + INTEGER_TYPE + " PRIMARY KEY" + " AUTOINCREMENT" + COMMA_SEP
                + SupplierEntry.COLUMN_NAME + TEXT_TYPE + COMMA_SEP
                + SupplierEntry.COLUMN_ADDRESS + TEXT_TYPE + COMMA_SEP
                + SupplierEntry.COLUMN_PHONE + TEXT_TYPE + ");";

        db.execSQL(SQL_CREATE_BOOKS);
        db.execSQL(SQL_CREATE_SUPPLIERS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}