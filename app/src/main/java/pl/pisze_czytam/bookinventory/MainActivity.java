package pl.pisze_czytam.bookinventory;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.database.Cursor;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import pl.pisze_czytam.bookinventory.data.BookstoreContract.*;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        ViewPager viewPager = findViewById(R.id.viewpager);
        CatalogAdapter adapter = new CatalogAdapter(getSupportFragmentManager(), this);
        viewPager.setAdapter(adapter);

        TabLayout tabLayout = findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(viewPager);
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
                startActivity(new Intent(MainActivity.this, BookEditor.class));
                return true;
            case R.id.add_supplier:
                startActivity(new Intent(MainActivity.this, SupplierEditor.class));
                return true;
            case R.id.delete_data:
                showDeleteDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void insertDummyBook() {
        // to avoid adding dummy book without supplier (it's impossible in normal app logic)
        String[] projection = {SupplierEntry.COLUMN_NAME, SupplierEntry.COLUMN_PHONE, SupplierEntry.COLUMN_ADDRESS};
        Cursor cursor = getContentResolver().query(SupplierEntry.SUPPLIERS_URI, projection, null, null,
                null, null);
        if (!cursor.moveToFirst()) {
            insertDummySupplier();
        }

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
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(MainActivity.this);
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
}
