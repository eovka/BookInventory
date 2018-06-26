package pl.pisze_czytam.bookinventory;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
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
import android.widget.Toast;

import pl.pisze_czytam.bookinventory.data.BookstoreContract.SupplierEntry;
import pl.pisze_czytam.bookinventory.databinding.SupplierDetailsBinding;

public class SupplierDetails extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>, View.OnClickListener {
    private SupplierDetailsBinding bind;
    Uri clickedSupplier;
    private static final int LOADER_ID = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bind = DataBindingUtil.setContentView(this, R.layout.supplier_details);

        clickedSupplier = getIntent().getData();
        getSupportLoaderManager().initLoader(LOADER_ID, null, this);

        bind.fabEditSupplier.setOnClickListener(this);
        bind.supplierMail.setOnClickListener(this);
        bind.supplierPhone.setOnClickListener(this);
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
        getMenuInflater().inflate(R.menu.editor_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_edit:
                goToEditor();
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
        String[] supplierProjection = {SupplierEntry.ID, SupplierEntry.COLUMN_NAME, SupplierEntry.COLUMN_ADDRESS,
                SupplierEntry.COLUMN_MAIL, SupplierEntry.COLUMN_PHONE};
        return new CursorLoader(this, clickedSupplier, supplierProjection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
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
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        bind.supplierName.setText(null);
        bind.supplierAddress.setText(null);
        bind.supplierMail.setText(null);
        bind.supplierPhone.setText(null);
    }

    @Override
    public void onClick(View v) {
        String[] projection = {SupplierEntry.ID, SupplierEntry.COLUMN_MAIL, SupplierEntry.COLUMN_PHONE};
        Cursor cursor = getContentResolver().query(clickedSupplier, projection, null,
                null, null, null);
        cursor.moveToFirst();
        switch (v.getId()) {
            case R.id.fab_edit_supplier:
                goToEditor();
                cursor.close();
                break;
            case R.id.supplier_mail:
                String mail = cursor.getString(cursor.getColumnIndex(SupplierEntry.COLUMN_MAIL));
                cursor.close();
                if (TextUtils.isEmpty(mail)) {
                    Toast.makeText(getApplicationContext(), R.string.cannot_email, Toast.LENGTH_SHORT).show();
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
                String phone = cursor.getString(cursor.getColumnIndex(SupplierEntry.COLUMN_PHONE));
                cursor.close();
                Intent dialIntent = new Intent(Intent.ACTION_DIAL);
                dialIntent.setData(Uri.parse("tel:" + phone));
                startActivity(dialIntent);
                break;
        }
    }

    private void goToEditor() {
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
            Toast.makeText(getApplicationContext(), R.string.error_delete_sup, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), R.string.supplier_deleted, Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}
