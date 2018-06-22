package pl.pisze_czytam.bookinventory;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.database.Cursor;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import pl.pisze_czytam.bookinventory.data.BookContract.*;

public class CatalogActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    BookCursorAdapter bookCursorAdapter;
    private static final int LOADER_ID = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(CatalogActivity.this, BooksEditor.class));
            }
        });

        ListView listView = findViewById(R.id.listview);
        View emptyView = findViewById(R.id.empty_view);
        listView.setEmptyView(emptyView);
        bookCursorAdapter = new BookCursorAdapter(this, null);
        listView.setAdapter(bookCursorAdapter);
        getSupportLoaderManager().initLoader(LOADER_ID, null, this);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent editorIntent = new Intent(CatalogActivity.this, BooksEditor.class);
                Uri currentBookUri = ContentUris.withAppendedId(BookEntry.BOOKS_URI, id);
                editorIntent.setData(currentBookUri);
                startActivity(editorIntent);
            }
        });
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
                return true;
            case R.id.dummy_supplier:
                insertDummySupplier();
                return true;
            case R.id.add_books:
                startActivity(new Intent(CatalogActivity.this, BooksEditor.class));
                return true;
            case R.id.add_supplier:
                startActivity(new Intent(CatalogActivity.this, SupplierEditor.class));
                return true;
            case R.id.delete_data:
                showDeleteDialog();
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

    private void showDeleteDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(CatalogActivity.this);
        dialogBuilder.setView(getLayoutInflater().inflate(R.layout.dialog_delete_all, null))
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                getContentResolver().delete(BookEntry.BOOKS_URI, null, null);
                getContentResolver().delete(SupplierEntry.SUPPLIERS_URI, null, null);
                dialog.dismiss();
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

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] bookProjection = {BookEntry.ID, BookEntry.COLUMN_TITLE,
                BookEntry.COLUMN_PRICE, BookEntry.COLUMN_QUANTITY};
        return new CursorLoader(this, BookEntry.BOOKS_URI, bookProjection, null,
                null, BookEntry.COLUMN_TITLE + " ASC");
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        bookCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        bookCursorAdapter.swapCursor(null);
    }
}
