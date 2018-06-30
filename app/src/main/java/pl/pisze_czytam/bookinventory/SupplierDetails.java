package pl.pisze_czytam.bookinventory;

import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;

import pl.pisze_czytam.bookinventory.data.BookstoreContract.*;
import pl.pisze_czytam.bookinventory.databinding.SupplierDetailsBinding;

public class SupplierDetails extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>, View.OnClickListener {
    private SupplierDetailsBinding bind;
    private Uri clickedSupplier;
    private TitleCursorAdapter titleCursorAdapter;
    private static final int LOADER_ID = 0;
    private static final int TITLE_LOADER_ID = 1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bind = DataBindingUtil.setContentView(this, R.layout.supplier_details);

        clickedSupplier = getIntent().getData();
        getSupportLoaderManager().initLoader(LOADER_ID, null, this);

        titleCursorAdapter = new TitleCursorAdapter(getApplicationContext(), null);
        bind.bookList.setAdapter(titleCursorAdapter);
        bind.bookList.setEmptyView(bind.emptyView);
        getSupportLoaderManager().initLoader(TITLE_LOADER_ID, null, this);

        bind.bookList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent detailsIntent = new Intent(SupplierDetails.this, BookDetails.class);
                Uri currentBookUri = ContentUris.withAppendedId(BookEntry.BOOKS_URI, id);
                detailsIntent.setData(currentBookUri);
                startActivity(detailsIntent);
            }
        });

        bind.fabEditSupplier.setOnClickListener(this);
        bind.supplierMail.setOnClickListener(this);
        bind.supplierPhone.setOnClickListener(this);
        bind.emptyView.setOnClickListener(this);
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
            case R.id.action_edit:
                goToSupEditor();
                break;
            case R.id.action_delete:
                showDeleteDialog();
                break;
            case android.R.id.home:
                // go back to suppliers list, not to books one
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case LOADER_ID:
                String[] supplierProjection = {SupplierEntry.ID, SupplierEntry.COLUMN_NAME, SupplierEntry.COLUMN_ADDRESS,
                        SupplierEntry.COLUMN_MAIL, SupplierEntry.COLUMN_PHONE};
                return new CursorLoader(this, clickedSupplier, supplierProjection, null, null, null);
            case TITLE_LOADER_ID:
                String[] nameProjection = {SupplierEntry.ID, SupplierEntry.COLUMN_NAME};
                Cursor cursor = getContentResolver().query(clickedSupplier, nameProjection, null, null, null);
                cursor.moveToFirst();
                String currentSupplier = cursor.getString(cursor.getColumnIndex(SupplierEntry.COLUMN_NAME));
                cursor.close();

                String[] titleProjection = {BookEntry.ID, BookEntry.COLUMN_TITLE, BookEntry.COLUMN_SUPPLIER};
                String selection = BookEntry.COLUMN_SUPPLIER + "=?";
                String[] selectionArgs = {currentSupplier};
                return new CursorLoader(this, BookEntry.BOOKS_URI, titleProjection, selection, selectionArgs, BookEntry.COLUMN_TITLE + " ASC");
            default: return null;
        }
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        switch (loader.getId()) {
            case LOADER_ID:
                if (cursor.moveToFirst()) {
                    String name = cursor.getString(cursor.getColumnIndex(SupplierEntry.COLUMN_NAME));
                    String address = cursor.getString(cursor.getColumnIndex(SupplierEntry.COLUMN_ADDRESS));
                    String email = cursor.getString(cursor.getColumnIndex(SupplierEntry.COLUMN_MAIL));
                    String phone = cursor.getString(cursor.getColumnIndex(SupplierEntry.COLUMN_PHONE));

                    if (TextUtils.isEmpty(email)) {
                        email = getString(R.string.empty_data);
                    } else {
                        email = email.replace("@", " @");
                    }
                    if (TextUtils.isEmpty(address)) {
                        address = getString(R.string.empty_data);
                    }
                    bind.supplierName.setText(name);
                    bind.supplierAddress.setText(address);
                    bind.supplierMail.setText(email);
                    bind.supplierPhone.setText(phone);
                }
                break;
            case TITLE_LOADER_ID:
                titleCursorAdapter.swapCursor(cursor);
                break;
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        switch (loader.getId()) {
            case LOADER_ID:
                bind.supplierName.setText(null);
                bind.supplierAddress.setText(null);
                bind.supplierMail.setText(null);
                bind.supplierPhone.setText(null);
                break;
            case TITLE_LOADER_ID:
                titleCursorAdapter.swapCursor(null);
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab_edit_supplier:
                goToSupEditor();
                break;
            case R.id.supplier_mail:
                String[] mailProjection = {SupplierEntry.ID, SupplierEntry.COLUMN_MAIL};
                Cursor mailCursor = getContentResolver().query(clickedSupplier, mailProjection, null,
                        null, null, null);
                mailCursor.moveToFirst();
                String mail = mailCursor.getString(mailCursor.getColumnIndex(SupplierEntry.COLUMN_MAIL));
                mailCursor.close();
                if (TextUtils.isEmpty(mail)) {
                    createToast(getString(R.string.cannot_email));
                } else {
                    Intent mailIntent = new Intent(Intent.ACTION_SENDTO);
                    mailIntent.setData(Uri.parse("mailto:" + mail));
                    mailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.book_order));
                    if (mailIntent.resolveActivity(getPackageManager()) != null) {
                        startActivity(mailIntent);
                    }
                }
                break;
            case R.id.supplier_phone:
                String[] phoneProjection = {SupplierEntry.ID, SupplierEntry.COLUMN_PHONE};
                Cursor phoneCursor = getContentResolver().query(clickedSupplier, phoneProjection, null,
                        null, null, null);
                phoneCursor.moveToFirst();
                String phone = phoneCursor.getString(phoneCursor.getColumnIndex(SupplierEntry.COLUMN_PHONE));
                phoneCursor.close();
                Intent dialIntent = new Intent(Intent.ACTION_DIAL);
                dialIntent.setData(Uri.parse("tel:" + phone));
                startActivity(dialIntent);
                break;
            case R.id.empty_view:
                startActivity(new Intent(SupplierDetails.this, BookEditor.class));
        }
    }

    private void goToSupEditor() {
        Intent editorIntent = new Intent(SupplierDetails.this, SupplierEditor.class);
        editorIntent.setData(clickedSupplier);
        startActivity(editorIntent);
    }

    private void showDeleteDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(SupplierDetails.this);
        dialogBuilder.setView(getLayoutInflater().inflate(R.layout.dialog_delete_item, null))
                .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteSupplier();
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

    private void deleteSupplier() {
        int rowsDeleted = getContentResolver().delete(clickedSupplier, null, null);
        if (rowsDeleted == 0) {
            createToast(getString(R.string.error_delete_sup));
        } else {
            createToast(getString(R.string.supplier_deleted));
            finish();
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
