package pl.pisze_czytam.bookinventory;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import java.util.ArrayList;

import pl.pisze_czytam.bookinventory.data.BookContract.*;
import pl.pisze_czytam.bookinventory.data.BookstoreDbHelper;
import pl.pisze_czytam.bookinventory.databinding.BooksEditorBinding;

public class BooksEditor extends AppCompatActivity {
    private BooksEditorBinding bind;
    private String supplier = BookEntry.SUPPLIER_UNKNOWN;
    private String telephone = BookEntry.PHONE_UNKNOWN;
    private String address = BookEntry.ADDRESS_UNKNOWN;
    private double bookPrice = BookEntry.PRICE_DEFAULT;
    private int bookNumber = BookEntry.NUMBER_DEFAULT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bind = DataBindingUtil.setContentView(this, R.layout.books_editor);

        setupSpinner();

        bind.buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(BooksEditor.this, SupplierEditor.class));
            }
        });
    }

    public void setupSpinner() {
        BookstoreDbHelper dbHelper = new BookstoreDbHelper(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] projection = { SupplierEntry.COLUMN_NAME, SupplierEntry.COLUMN_PHONE, SupplierEntry.COLUMN_ADDRESS };
        Cursor suppliersCursor = db.query(SupplierEntry.TABLE_NAME, projection, null, null,
                null, null, SupplierEntry.COLUMN_NAME +" ASC");

        int nameColumnIndex = suppliersCursor.getColumnIndex(SupplierEntry.COLUMN_NAME);
        int phoneColumnIndex = suppliersCursor.getColumnIndex(SupplierEntry.COLUMN_PHONE);
        int addressColumnIndex = suppliersCursor.getColumnIndex(SupplierEntry.COLUMN_ADDRESS);

        ArrayList<String> suppliersNames = new ArrayList<>();
        // Build also phones and addresses lists to display them correctly after choosing supplier.
        final ArrayList<String> suppliersPhones = new ArrayList<>();
        final ArrayList<String> suppliersAddresses = new ArrayList<>();

        while (suppliersCursor.moveToNext()) {
            suppliersNames.add(suppliersCursor.getString(nameColumnIndex));
            suppliersPhones.add(suppliersCursor.getString(phoneColumnIndex));
            suppliersAddresses.add(suppliersCursor.getString(addressColumnIndex));
        }
        suppliersCursor.close();

        if (suppliersNames.isEmpty()) {
            bind.spinnerSuppliers.setVisibility(View.GONE);
            bind.suppliersPhone.setVisibility(View.GONE);
            bind.supplierAddress.setVisibility(View.GONE);
            bind.noSuppliersText.setVisibility(View.VISIBLE);
            bind.noSuppliersText.setText(R.string.no_suppliers);
        } else {
            ArrayAdapter suppliersAdapter = new ArrayAdapter<>(this, R.layout.supplier_spinner, suppliersNames);
            bind.spinnerSuppliers.setAdapter(suppliersAdapter);
            bind.spinnerSuppliers.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    supplier = (String) parent.getItemAtPosition(position);
                    telephone = suppliersPhones.get(position);
                    bind.suppliersPhone.setText(telephone);
                    address = suppliersAddresses.get(position);
                    bind.supplierAddress.setText(address);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    supplier = BookEntry.SUPPLIER_UNKNOWN;
                    telephone = BookEntry.PHONE_UNKNOWN;
                    address = BookEntry.ADDRESS_UNKNOWN;
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.editor_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                insertBook();
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void insertBook() {
        String title = bind.bookTitle.getText().toString().trim();
        String author = bind.bookAuthor.getText().toString().trim();
        String priceText = bind.bookPrice.getText().toString().trim();
        String numberText = bind.booksNumber.getText().toString().trim();
        if (!priceText.isEmpty()) {
            bookPrice = Double.parseDouble(priceText);
        }
        if (!numberText.isEmpty()) {
            bookNumber = Integer.parseInt(numberText);
        }

        BookstoreDbHelper dbHelper = new BookstoreDbHelper(this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(BookEntry.COLUMN_TITLE, title);
        contentValues.put(BookEntry.COLUMN_AUTHOR, author);
        contentValues.put(BookEntry.COLUMN_PRICE, bookPrice);
        contentValues.put(BookEntry.COLUMN_QUANTITY, bookNumber);
        contentValues.put(BookEntry.COLUMN_SUPPLIER, supplier);
        contentValues.put(BookEntry.COLUMN_SUP_PHONE, telephone);
        contentValues.put(BookEntry.COLUMN_SUP_ADDRESS, address);

        long newRowId = db.insert(BookEntry.TABLE_NAME, null, contentValues);
        if (newRowId == -1) {
            Toast.makeText(this, getString(R.string.error_save_book), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this,  getString(R.string.book_saved) + newRowId, Toast.LENGTH_SHORT).show();
        }
    }

    // Refresh spinner after pressing button "add a supplier" in book editor, adding it and coming back to 1st editor.
    @Override
    protected void onResume() {
        setupSpinner();
        super.onResume();
    }
}
