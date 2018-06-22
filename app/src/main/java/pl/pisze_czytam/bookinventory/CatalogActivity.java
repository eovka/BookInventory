package pl.pisze_czytam.bookinventory;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import pl.pisze_czytam.bookinventory.data.BookContract.*;
import pl.pisze_czytam.bookinventory.data.BookstoreDbHelper;

public class CatalogActivity extends AppCompatActivity {
    private BookstoreDbHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        databaseHelper = new BookstoreDbHelper(this);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(CatalogActivity.this, BooksEditor.class));
            }
        });
        displayDatabaseInfo();
    }

    private void displayDatabaseInfo() {
        String[] bookProjection = {BookEntry.ID, BookEntry.COLUMN_TITLE, BookEntry.COLUMN_AUTHOR,
                BookEntry.COLUMN_PRICE, BookEntry.COLUMN_QUANTITY, BookEntry.COLUMN_SUPPLIER,
                BookEntry.COLUMN_SUP_PHONE, BookEntry.COLUMN_SUP_ADDRESS};
        Cursor bookCursor = getContentResolver().query(BookEntry.BOOKS_URI, bookProjection, null,
                null, null);
        ListView listView = findViewById(R.id.listview);
        BookCursorAdapter bookAdapter = new BookCursorAdapter(this, bookCursor);
        listView.setAdapter(bookAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.catalog_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.dummy_book:
                 insertDummyBook();
                 displayDatabaseInfo();
                return true;
            case R.id.dummy_supplier:
                insertDummySupplier();
                displayDatabaseInfo();
                return true;
            case R.id.add_books:
                startActivity(new Intent(CatalogActivity.this, BooksEditor.class));
                return true;
            case R.id.add_supplier:
                startActivity(new Intent(CatalogActivity.this, SupplierEditor.class));
                return true;
            case R.id.delete_data:
                showDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void insertDummyBook() {
        ContentValues values = new ContentValues();
        values.put(BookEntry.COLUMN_TITLE, "Mistrz i Małgorzata");
        values.put(BookEntry.COLUMN_AUTHOR, "Michaił Bułhakow");
        values.put(BookEntry.COLUMN_PRICE, 59.99);
        values.put(BookEntry.COLUMN_QUANTITY, 7);
        values.put(BookEntry.COLUMN_SUPPLIER, "Znak");
        values.put(BookEntry.COLUMN_SUP_PHONE, "+48126199500");
        values.put(BookEntry.COLUMN_SUP_ADDRESS, "ul. Kościuszki 37, 30-105 Kraków");
        getContentResolver().insert(BookEntry.BOOKS_URI, values);
    }

    private void insertDummySupplier() {
        ContentValues values = new ContentValues();
        values.put(SupplierEntry.COLUMN_NAME, "Znak");
        values.put(SupplierEntry.COLUMN_ADDRESS, "ul. Kościuszki 37, 30-105 Kraków");
        values.put(SupplierEntry.COLUMN_PHONE, "+48126199500");
        getContentResolver().insert(SupplierEntry.SUPPLIERS_URI, values);
    }

    private void showDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(CatalogActivity.this);
        dialogBuilder.setView(getLayoutInflater().inflate(R.layout.dialog_warning, null))
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SQLiteDatabase db = databaseHelper.getWritableDatabase();
                db.execSQL("DELETE FROM " + BookEntry.TABLE_NAME);
                db.execSQL("DELETE FROM sqlite_sequence WHERE name=" + "'books'");
                db.execSQL("DELETE FROM " + SupplierEntry.TABLE_NAME);
                db.execSQL("DELETE FROM sqlite_sequence WHERE name=" + "'suppliers'");
                dialog.dismiss();
                displayDatabaseInfo();
            }
        })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        dialogBuilder.create().show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        displayDatabaseInfo();
    }
}
