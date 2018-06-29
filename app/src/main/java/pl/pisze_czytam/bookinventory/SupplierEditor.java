package pl.pisze_czytam.bookinventory;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import pl.pisze_czytam.bookinventory.data.BookstoreContract.*;
import pl.pisze_czytam.bookinventory.databinding.SupplierEditorBinding;

public class SupplierEditor extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private SupplierEditorBinding bind;
    Uri clickedSupplier;
    private boolean supplierChanged;

    private View.OnTouchListener touchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            supplierChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bind = DataBindingUtil.setContentView(this, R.layout.supplier_editor);

        clickedSupplier = getIntent().getData();
        if (clickedSupplier != null) {
            getSupportLoaderManager().initLoader(0, null, this);
        }

        bind.supplierName.setOnTouchListener(touchListener);
        bind.supplierAddress.setOnTouchListener(touchListener);
        bind.supplierMail.setOnTouchListener(touchListener);
        bind.supplierPhone.setOnTouchListener(touchListener);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (clickedSupplier == null) {
            MenuItem deleteItem = menu.findItem(R.id.action_delete);
            deleteItem.setVisible(false);
        }
        MenuItem editItem = menu.findItem(R.id.action_edit);
        editItem.setVisible(false);
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
                if (!supplierChanged) {
                    NavUtils.navigateUpFromSameTask(SupplierEditor.this);
                    return true;
                }
                DialogInterface.OnClickListener discard = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        NavUtils.navigateUpFromSameTask(SupplierEditor.this);
                    }
                };
                showUnsavedDialog(discard);
                return true;

            case R.id.action_save:
                // inform user sth is wrong with data
                String name = bind.supplierName.getText().toString().trim();
                String phone = bind.supplierPhone.getText().toString().trim();
                if (TextUtils.isEmpty(name) || TextUtils.isEmpty(phone)) {
                    createToast(getString(R.string.supplier_required));
                    return true;
                }
                saveSupplier();
                finish();
                return true;
            case R.id.action_delete:
                showDeleteDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onBackPressed() {
        if (!supplierChanged) {
            super.onBackPressed();
            return;
        }
        DialogInterface.OnClickListener discard = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        };
        showUnsavedDialog(discard);
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        String [] supplierProjection = {SupplierEntry.ID, SupplierEntry.COLUMN_NAME, SupplierEntry.COLUMN_ADDRESS,
                SupplierEntry.COLUMN_MAIL, SupplierEntry.COLUMN_PHONE};
        return new CursorLoader(this, clickedSupplier, supplierProjection, null, null, null);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        if (cursor.moveToFirst()) {
            String name = cursor.getString(cursor.getColumnIndex(SupplierEntry.COLUMN_NAME));
            String address = cursor.getString(cursor.getColumnIndex(SupplierEntry.COLUMN_ADDRESS));
            String email = cursor.getString(cursor.getColumnIndex(SupplierEntry.COLUMN_MAIL));
            String phone = cursor.getString(cursor.getColumnIndex(SupplierEntry.COLUMN_PHONE));

            bind.supplierName.setText(name);
            bind.supplierAddress.setText(address);
            bind.supplierMail.setText(email);
            bind.supplierPhone.setText(phone);
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        bind.supplierName.setText(null);
        bind.supplierAddress.setText(null);
        bind.supplierMail.setText(null);
        bind.supplierPhone.setText(null);
    }

    private void saveSupplier() {
        String name = bind.supplierName.getText().toString().trim();
        String address = bind.supplierAddress.getText().toString().trim();
        String mail = bind.supplierMail.getText().toString().trim();
        String phone = bind.supplierPhone.getText().toString().trim();

        ContentValues contentValues = new ContentValues();
        contentValues.put(SupplierEntry.COLUMN_NAME, name);
        contentValues.put(SupplierEntry.COLUMN_ADDRESS, address);
        contentValues.put(SupplierEntry.COLUMN_MAIL, mail);
        contentValues.put(SupplierEntry.COLUMN_PHONE, phone);

        if (clickedSupplier == null) {
            Uri newUri = getContentResolver().insert(SupplierEntry.SUPPLIERS_URI, contentValues);
            if (newUri == null) {
                createToast(getString(R.string.error_save_sup));
            } else {
                createToast(getString(R.string.supplier_saved));
            }
        } else {
            long rowsAffected = getContentResolver().update(clickedSupplier, contentValues, null, null);
            if (rowsAffected == 0) {
                createToast(getString(R.string.error_update_sup));
            } else {
                createToast(getString(R.string.supplier_updated));
            }
        }
    }

    private void showDeleteDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(SupplierEditor.this);
        dialogBuilder.setView(getLayoutInflater().inflate(R.layout.dialog_delete_item, null))
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
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
        if (clickedSupplier != null) {
            int rowsDelete = getContentResolver().delete(clickedSupplier, null, null);
            if (rowsDelete == 0) {
                createToast(getString(R.string.error_delete_sup));
            } else {
                createToast(getString(R.string.supplier_deleted));
                finish();
                NavUtils.navigateUpFromSameTask(this);
            }
        }
    }

    private void showUnsavedDialog(DialogInterface.OnClickListener discardClickListener) {
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(SupplierEditor.this);
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
