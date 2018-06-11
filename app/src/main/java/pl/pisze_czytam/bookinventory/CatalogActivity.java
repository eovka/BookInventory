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

    public void displayDatabaseInfo() {
        SQLiteDatabase database = databaseHelper.getReadableDatabase();
        String[] bookProjection = {BookEntry.ID, BookEntry.COLUMN_TITLE, BookEntry.COLUMN_AUTHOR,
                BookEntry.COLUMN_PRICE, BookEntry.COLUMN_QUANTITY, BookEntry.COLUMN_SUPPLIER};
        Cursor bookCursor = database.query(BookEntry.TABLE_NAME, bookProjection, null,
                null, null, null, null);

        String[] supplierProjection = {SupplierEntry.ID, SupplierEntry.COLUMN_NAME,
                SupplierEntry.COLUMN_ADDRESS, SupplierEntry.COLUMN_PHONE};
        Cursor supplierCursor = database.query(SupplierEntry.TABLE_NAME, supplierProjection,
                null, null, null, null, null);

        TextView booksTextView = findViewById(R.id.books_textview);
        try {
            booksTextView.setText("The books table contains " + bookCursor.getCount() + " items.\n\n");
            booksTextView.append(BookEntry.ID + " - " + BookEntry.COLUMN_TITLE
                    + " - " + BookEntry.COLUMN_AUTHOR
                    + " - " + BookEntry.COLUMN_PRICE
                    + " - " + BookEntry.COLUMN_QUANTITY
                    + " - " + BookEntry.COLUMN_SUPPLIER + "\n");

            int idColumnIndex = bookCursor.getColumnIndex(BookEntry.ID);
            int titleColumnIndex = bookCursor.getColumnIndex(BookEntry.COLUMN_TITLE);
            int authorColumnIndex = bookCursor.getColumnIndex(BookEntry.COLUMN_AUTHOR);
            int priceColumnIndex = bookCursor.getColumnIndex(BookEntry.COLUMN_PRICE);
            int quantityColumnIndex = bookCursor.getColumnIndex(BookEntry.COLUMN_QUANTITY);
            int supplierColumnIndex = bookCursor.getColumnIndex(BookEntry.COLUMN_SUPPLIER);

            while (bookCursor.moveToNext()) {
                int currentId = bookCursor.getInt(idColumnIndex);
                String currentTitle = bookCursor.getString(titleColumnIndex);
                String currentAuthor = bookCursor.getString(authorColumnIndex);
                double currentPrice = bookCursor.getDouble(priceColumnIndex);
                int currentQuantity = bookCursor.getInt(quantityColumnIndex);
                String currentSupplier = bookCursor.getString(supplierColumnIndex);
                booksTextView.append("\n" + currentId + " - " + currentTitle + " - " + currentAuthor
                        + " - " + currentPrice + " - " + currentQuantity + " - " + currentSupplier);
            }
            booksTextView.append("\n\n\nThe suppliers table contains " + supplierCursor.getCount() + " names.\n\n");
            booksTextView.append(SupplierEntry.ID + " - " + SupplierEntry.COLUMN_NAME
                    + " - " + SupplierEntry.COLUMN_ADDRESS
                    + " - " + SupplierEntry.COLUMN_PHONE + "\n");

            int idSupplierIndex = supplierCursor.getColumnIndex(SupplierEntry.ID);
            int nameColumnIndex = supplierCursor.getColumnIndex(SupplierEntry.COLUMN_NAME);
            int addressColumnIndex = supplierCursor.getColumnIndex(SupplierEntry.COLUMN_ADDRESS);
            int phoneColumnIndex = supplierCursor.getColumnIndex(SupplierEntry.COLUMN_PHONE);

            while (supplierCursor.moveToNext()) {
                int currentId = supplierCursor.getInt(idSupplierIndex);
                String currentSupplier = supplierCursor.getString(nameColumnIndex);
                String currentAddress = supplierCursor.getString(addressColumnIndex);
                String currentPhone = supplierCursor.getString(phoneColumnIndex);
                booksTextView.append("\n" + currentId + " - " + currentSupplier + " - " + currentAddress
                        + " - " + currentPhone);
            }
        } finally {
            bookCursor.close();
            supplierCursor.close();
        }
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
            case R.id.delete_books:
                showDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void insertDummyBook() {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(BookEntry.COLUMN_TITLE, "Mistrz i Małgorzata");
        values.put(BookEntry.COLUMN_AUTHOR, "Michaił Bułhakow");
        values.put(BookEntry.COLUMN_PRICE, 59.99);
        values.put(BookEntry.COLUMN_QUANTITY, 7);
        values.put(BookEntry.COLUMN_SUPPLIER, "Znak");
        db.insert(BookEntry.TABLE_NAME, null, values);
    }

    public void insertDummySupplier() {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(SupplierEntry.COLUMN_NAME, "Znak");
        values.put(SupplierEntry.COLUMN_ADDRESS, "ul. Kościuszki 37, 30-105 Kraków");
        values.put(SupplierEntry.COLUMN_PHONE, "+48126199500");
        db.insert(SupplierEntry.TABLE_NAME, null, values);
    }

    public void showDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(CatalogActivity.this);
        dialogBuilder.setView(getLayoutInflater().inflate(R.layout.dialog_warning, null))
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SQLiteDatabase db = databaseHelper.getWritableDatabase();
                db.execSQL("DELETE FROM " + BookEntry.TABLE_NAME);
                db.execSQL("DELETE FROM sqlite_sequence WHERE name=" + "'books'");
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
