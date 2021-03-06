package pl.pisze_czytam.bookinventory;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import pl.pisze_czytam.bookinventory.data.BookstoreContract.BookEntry;
import pl.pisze_czytam.bookinventory.databinding.BookDetailsBinding;

public class BookDetails extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>,
        View.OnClickListener {
    private BookDetailsBinding bind;
    private Uri clickedBook;
    private static final int LOADER_ID = 0;
    private boolean quantityChanged;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bind = DataBindingUtil.setContentView(this, R.layout.book_details);

        clickedBook = getIntent().getData();
        getSupportLoaderManager().initLoader(LOADER_ID, null, this);

        bind.plusButton.setOnClickListener(this);
        bind.minusButton.setOnClickListener(this);
        bind.callButton.setOnClickListener(this);
        bind.fabEditBook.setOnClickListener(this);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuItem saveItem = menu.findItem(R.id.action_save);
        saveItem.setVisible(false);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.details_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                checkIfChanged();
                NavUtils.navigateUpFromSameTask(BookDetails.this);
                return true;
            case R.id.action_edit:
                checkIfChanged();
                goToEditor();
                return true;
            case R.id.action_delete:
                showDeleteDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        String[] bookProjection = {BookEntry.ID, BookEntry.COLUMN_TITLE, BookEntry.COLUMN_AUTHOR,
                BookEntry.COLUMN_PRICE, BookEntry.COLUMN_QUANTITY, BookEntry.COLUMN_SUPPLIER, BookEntry.COLUMN_SUP_PHONE};
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
            String phone = cursor.getString(cursor.getColumnIndex(BookEntry.COLUMN_SUP_PHONE));

            bind.bookTitle.setText(title);
            bind.bookAuthor.setText(author);
            bind.bookPrice.setText(String.valueOf(price));
            bind.bookQuantity.setText(String.valueOf(quantity));
            bind.supplierName.setText(supplier);
            bind.suppliersPhone.setText(phone);
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        bind.bookTitle.setText(null);
        bind.bookAuthor.setText(null);
        bind.bookPrice.setText(null);
        bind.bookQuantity.setText(String.valueOf(0));
        bind.supplierName.setText(null);
        bind.suppliersPhone.setText(null);
    }

    @SuppressLint("StringFormatInvalid")
    @Override
    public void onClick(View v) {
        String[] projection = {BookEntry.ID, BookEntry.COLUMN_QUANTITY, BookEntry.COLUMN_SUP_PHONE};
        Cursor cursor = getContentResolver().query(clickedBook, projection, null,
                null, null, null);
        cursor.moveToFirst();
        int quantity = cursor.getInt(cursor.getColumnIndex(BookEntry.COLUMN_QUANTITY));
        String phone = cursor.getString(cursor.getColumnIndex(BookEntry.COLUMN_SUP_PHONE));
        cursor.close();
        switch (v.getId()) {
            case R.id.minus_button:
                if (quantity >= 1) {
                    quantity--;
                    ContentValues values = new ContentValues();
                    values.put(BookEntry.COLUMN_QUANTITY, quantity);
                    getContentResolver().update(clickedBook, values, null, null);
                    bind.bookQuantity.setText(String.valueOf(quantity));
                    quantityChanged = true;
                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    int warnNumber = Integer.parseInt(sharedPreferences.getString(getApplicationContext().getResources().getString(R.string.warn_number_key), "2"));
                    if (quantity == warnNumber) {
                        createToast(getApplicationContext().getResources().getString(R.string.books_left, warnNumber));
                    }
                } else {
                    createToast(getString(R.string.no_books));
                }
                break;
            case R.id.plus_button:
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                int maxInStock = Integer.parseInt(sharedPreferences.getString(getString(R.string.max_stock_key), "100"));
                if (quantity < maxInStock) {
                    quantity++;
                    ContentValues values = new ContentValues();
                    values.put(BookEntry.COLUMN_QUANTITY, quantity);
                    getContentResolver().update(clickedBook, values, null, null);
                    bind.bookQuantity.setText(String.valueOf(quantity));
                    quantityChanged = true;
                }
                if (quantity >= maxInStock) {
                    createToast(getString(R.string.full_stock, maxInStock));
                }
                break;
            case R.id.call_button:

                Intent dialIntent = new Intent(Intent.ACTION_DIAL);
                dialIntent.setData(Uri.parse("tel:" + phone));
                startActivity(dialIntent);
                break;
            case R.id.fab_edit_book:
                checkIfChanged();
                goToEditor();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        checkIfChanged();
        super.onBackPressed();
    }

    private void goToEditor() {
        Intent editorIntent = new Intent(BookDetails.this, BookEditor.class);
        editorIntent.setData(clickedBook);
        startActivity(editorIntent);
    }

    private void showDeleteDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(BookDetails.this);
        dialogBuilder.setView(getLayoutInflater().inflate(R.layout.dialog_delete_item, null))
            .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
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
    private void deleteBook() {
        int rowsDeleted = getContentResolver().delete(clickedBook, null, null);
        if (rowsDeleted == 0) {
            createToast(getString(R.string.error_delete_book));
        } else {
            createToast(getString(R.string.book_deleted));
            finish();
        }
    }
    private void checkIfChanged() {
        if (quantityChanged) {
            createToast(getString(R.string.quantity_updated));
        }
    }
    private void createToast(String toastText) {
        View toastLayout = getLayoutInflater().inflate(R.layout.custom_toast,
                (ViewGroup) findViewById(R.id.custom_toast_container));
        TextView textView = toastLayout.findViewById(R.id.toast_text);
        textView.setText(toastText);
        Toast toast = new Toast(getApplicationContext());
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(toastLayout);
        toast.show();
    }
}