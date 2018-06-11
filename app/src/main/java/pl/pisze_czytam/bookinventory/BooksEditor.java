package pl.pisze_czytam.bookinventory;

import android.content.ContentValues;
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
    BooksEditorBinding bind;
    private String supplier = BookEntry.SUPPLIER_UNKNOWN;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bind = DataBindingUtil.setContentView(this, R.layout.books_editor);

        setupSpinner();
    }

    private void setupSpinner() {
        BookstoreDbHelper dbHelper = new BookstoreDbHelper(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] projection = { SupplierEntry.COLUMN_NAME };
        Cursor suppliersCursor = db.query(SupplierEntry.TABLE_NAME, projection, null, null,
                null, null, SupplierEntry.COLUMN_NAME +" ASC");

        int nameColumnIndex = suppliersCursor.getColumnIndex(SupplierEntry.COLUMN_NAME);

        ArrayList<String> suppliersNames = new ArrayList<>();
        while (suppliersCursor.moveToNext()) {
            String currentName = suppliersCursor.getString(nameColumnIndex);
            suppliersNames.add(currentName);
        }
        suppliersCursor.close();

        if (suppliersNames.isEmpty()) {
            bind.spinnerSuppliers.setVisibility(View.GONE);
            bind.noSuppliersText.setVisibility(View.VISIBLE);
            bind.noSuppliersText.setText(R.string.no_suppliers);
        } else {
            ArrayAdapter suppliersAdapter = new ArrayAdapter<>(this, R.layout.supplier_spinner, suppliersNames);
            bind.spinnerSuppliers.setAdapter(suppliersAdapter);
            bind.spinnerSuppliers.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    supplier = (String) parent.getItemAtPosition(position);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    supplier = BookEntry.SUPPLIER_UNKNOWN;
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

    public void insertBook() {
        String title = bind.bookTitle.getText().toString().trim();
        String author = bind.bookAuthor.getText().toString().trim();
        double price = Double.parseDouble(bind.bookPrice.getText().toString().trim());
        int quantity = Integer.parseInt(bind.booksNumber.getText().toString().trim());

        BookstoreDbHelper dbHelper = new BookstoreDbHelper(this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(BookEntry.COLUMN_TITLE, title);
        contentValues.put(BookEntry.COLUMN_AUTHOR, author);
        contentValues.put(BookEntry.COLUMN_PRICE, price);
        contentValues.put(BookEntry.COLUMN_QUANTITY, quantity);
        contentValues.put(BookEntry.COLUMN_SUPPLIER, supplier);

        long newRowId = db.insert(BookEntry.TABLE_NAME, null, contentValues);
        if (newRowId == -1) {
            Toast.makeText(this, getString(R.string.error_save_book), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this,  getString(R.string.book_saved) + newRowId, Toast.LENGTH_SHORT).show();
        }
    }
}
