package pl.pisze_czytam.bookinventory;

import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;

import pl.pisze_czytam.bookinventory.data.BookstoreContract.*;
import pl.pisze_czytam.bookinventory.data.BookstoreDbHelper;
import pl.pisze_czytam.bookinventory.databinding.BookEditorBinding;

public class BookEditor extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private BookEditorBinding bind;
    private String supplier = BookEntry.SUPPLIER_UNKNOWN;
    private String telephone = BookEntry.PHONE_UNKNOWN;
    private String address = BookEntry.ADDRESS_UNKNOWN;
    private double bookPrice = BookEntry.PRICE_DEFAULT;
    private int bookQuantity = BookEntry.NUMBER_DEFAULT;
    Uri clickedBook;
    private static final int LOADER_ID = 1;
    private boolean bookChanged;

    private View.OnTouchListener touchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            bookChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bind = DataBindingUtil.setContentView(this, R.layout.book_editor);

        clickedBook = getIntent().getData();
        if (clickedBook != null) {
            setTitle(R.string.edit_book);
            getSupportLoaderManager().initLoader(LOADER_ID, null, this);
        }
        setupSpinner();

        bind.buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(BookEditor.this, SupplierEditor.class));
            }
        });

        bind.bookTitle.setOnTouchListener(touchListener);
        bind.bookAuthor.setOnTouchListener(touchListener);
        bind.bookPrice.setOnTouchListener(touchListener);
        bind.booksQuantity.setOnTouchListener(touchListener);
        bind.spinnerSuppliers.setOnTouchListener(touchListener);
    }

    public void setupSpinner() {
        BookstoreDbHelper dbHelper = new BookstoreDbHelper(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] projection = {SupplierEntry.COLUMN_NAME, SupplierEntry.COLUMN_PHONE, SupplierEntry.COLUMN_ADDRESS};
        Cursor suppliersCursor = db.query(SupplierEntry.TABLE_NAME, projection, null, null,
                null, null, SupplierEntry.COLUMN_NAME + " ASC");

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
            bind.spinnerSuppliers.setVisibility(View.VISIBLE);
            bind.suppliersPhone.setVisibility(View.VISIBLE);
            bind.supplierAddress.setVisibility(View.VISIBLE);
            bind.noSuppliersText.setVisibility(View.GONE);
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
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
            if (clickedBook == null) {
                MenuItem deleteItem = menu.findItem(R.id.action_delete);
                deleteItem.setVisible(false);
            }
            MenuItem editItem = menu.findItem(R.id.action_edit);
            editItem.setVisible(false);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.editor_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (!bookChanged) {
                    NavUtils.navigateUpFromSameTask(BookEditor.this);
                    return true;
                }
                DialogInterface.OnClickListener discardClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        NavUtils.navigateUpFromSameTask(BookEditor.this);
                    }
                };
                showUnsavedDialog(discardClickListener);
                return true;
            case R.id.action_save:
                // inform user something is wrong with his data (and don't try to add them to database)
                String title = bind.bookTitle.getText().toString();
                String supplierName = null;
                if (bind.spinnerSuppliers != null && bind.spinnerSuppliers.getSelectedItem() != null) {
                    supplierName = bind.spinnerSuppliers.getSelectedItem().toString();
                }
                if (TextUtils.isEmpty(title) || TextUtils.isEmpty(supplierName)) {
                    Toast.makeText(getApplicationContext(), R.string.book_required, Toast.LENGTH_SHORT).show();
                    return true;
                }
                saveBook();
                finish();
                return true;
            case R.id.action_delete:
                showDeleteDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveBook() {
        String title = bind.bookTitle.getText().toString().trim();
        String author = bind.bookAuthor.getText().toString().trim();
        String priceText = bind.bookPrice.getText().toString().trim();
        String quantity = bind.booksQuantity.getText().toString().trim();
        if (!priceText.isEmpty()) {
            bookPrice = Double.parseDouble(priceText);
        }
        if (!quantity.isEmpty()) {
            bookQuantity = Integer.parseInt(quantity);
        }
        if (Integer.parseInt(quantity) > 100) {
            Toast.makeText(getApplicationContext(), "Your limit for a book is 100 copies. Setting quantity to 100.", Toast.LENGTH_LONG).show();
            bookQuantity = 100;
        }
        ContentValues contentValues = new ContentValues();
        contentValues.put(BookEntry.COLUMN_TITLE, title);
        contentValues.put(BookEntry.COLUMN_AUTHOR, author);
        contentValues.put(BookEntry.COLUMN_PRICE, bookPrice);
        contentValues.put(BookEntry.COLUMN_QUANTITY, bookQuantity);
        contentValues.put(BookEntry.COLUMN_SUPPLIER, supplier);
        contentValues.put(BookEntry.COLUMN_SUP_PHONE, telephone);
        contentValues.put(BookEntry.COLUMN_SUP_ADDRESS, address);

        if (clickedBook == null) {
            Uri newUri = getContentResolver().insert(BookEntry.BOOKS_URI, contentValues);
            if (newUri == null) {
                Toast.makeText(getApplicationContext(), getString(R.string.error_save_book), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), getString(R.string.book_saved), Toast.LENGTH_SHORT).show();
            }
        } else {
            long rowsAffected = getContentResolver().update(clickedBook, contentValues, null, null);
            if (rowsAffected == 0) {
                Toast.makeText(getApplicationContext(), getString(R.string.error_update_book), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), getString(R.string.book_updated), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void deleteBook() {
        if (clickedBook != null) {
            int rowsDelete = getContentResolver().delete(clickedBook, null, null);
            if (rowsDelete == 0) {
                Toast.makeText(getApplicationContext(), R.string.error_delete_book, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), R.string.book_deleted, Toast.LENGTH_SHORT).show();
                finish();
                // finishing leaves a user in details activity, so go up:
                NavUtils.navigateUpFromSameTask(this);
            }
        }
    }

    /** Show dialog about unsaved changes - to use both when back or up pressed. **/
    private void showUnsavedDialog(DialogInterface.OnClickListener discardClickListener) {
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(BookEditor.this);
        dialogBuilder.setView(getLayoutInflater().inflate(R.layout.dialog_unsaved, null))
                .setPositiveButton(R.string.discard, discardClickListener)
                .setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (dialog != null) {
                            dialog.dismiss();
                        }
                    }
                });
        dialogBuilder.create().show();
    }

    private void showDeleteDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(BookEditor.this);
        dialogBuilder.setView(getLayoutInflater().inflate(R.layout.dialog_delete_item, null))
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                deleteBook();
                            }
                        })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (dialog != null) {
                            dialog.dismiss();
                        }
                    }
                });
        dialogBuilder.create().show();
    }

    @Override
    public void onBackPressed() {
        if (!bookChanged) {
            super.onBackPressed();
            return;
        }

        DialogInterface.OnClickListener discardClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        };
        showUnsavedDialog(discardClickListener);
    }

    // Refresh spinner after pressing button "add a supplier" in book editor, adding it and coming back to 1st editor.
    @Override
    protected void onResume() {
        super.onResume();
        setupSpinner();
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        String[] bookProjection = {BookEntry.ID, BookEntry.COLUMN_TITLE, BookEntry.COLUMN_AUTHOR,
                BookEntry.COLUMN_PRICE, BookEntry.COLUMN_QUANTITY, BookEntry.COLUMN_SUPPLIER,
                BookEntry.COLUMN_SUP_ADDRESS, BookEntry.COLUMN_SUP_PHONE};
        return new CursorLoader(this, clickedBook, bookProjection, null, null, null);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        if (cursor.moveToFirst()) {
            String title = cursor.getString(cursor.getColumnIndex(BookEntry.COLUMN_TITLE));
            String author = cursor.getString(cursor.getColumnIndex(BookEntry.COLUMN_AUTHOR));
            double price = cursor.getDouble(cursor.getColumnIndex(BookEntry.COLUMN_PRICE));
            int quantity = cursor.getInt(cursor.getColumnIndex(BookEntry.COLUMN_QUANTITY));
            String supplier = cursor.getString(cursor.getColumnIndex(BookEntry.COLUMN_SUPPLIER));

            bind.bookTitle.setText(title);
            bind.bookAuthor.setText(author);
            bind.bookPrice.setText(String.valueOf(price));
            bind.booksQuantity.setText(String.valueOf(quantity));
            bind.spinnerSuppliers.setSelection(getIndex(bind.spinnerSuppliers, supplier));
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        bind.bookTitle.setText(null);
        bind.bookAuthor.setText(null);
        bind.bookPrice.setText(null);
        bind.booksQuantity.setText(null);
        bind.spinnerSuppliers.setSelection(getIndex(bind.spinnerSuppliers, supplier));
    }

    /** Find supplier in a spinner and set a proper one when editing a book. **/
    private int getIndex(Spinner spinner, String myString) {
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(myString)) {
                return i;
            }
        }
        return 0;
    }
}
