package pl.pisze_czytam.bookinventory;

import android.content.ContentValues;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import pl.pisze_czytam.bookinventory.data.BookContract.*;
import pl.pisze_czytam.bookinventory.databinding.SuppliersEditorBinding;

public class SupplierEditor extends AppCompatActivity {
    private SuppliersEditorBinding bind;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bind = DataBindingUtil.setContentView(this, R.layout.suppliers_editor);
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
                // inform user sth is wrong with data
                String name = bind.supplierName.getText().toString().trim();
                String phone = bind.supplierPhone.getText().toString().trim();
                if (TextUtils.isEmpty(name) || TextUtils.isEmpty(phone)) {
                    Toast.makeText(getApplicationContext(), R.string.supplier_required, Toast.LENGTH_SHORT).show();
                    return true;
                }
                insertSupplier();
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void insertSupplier() {
        String name = bind.supplierName.getText().toString().trim();
        String address = bind.supplierAddress.getText().toString().trim();
        String phone = bind.supplierPhone.getText().toString().trim();

        ContentValues contentValues = new ContentValues();
        contentValues.put(SupplierEntry.COLUMN_NAME, name);
        contentValues.put(SupplierEntry.COLUMN_ADDRESS, address);
        contentValues.put(SupplierEntry.COLUMN_PHONE, phone);

        Uri newUri = getContentResolver().insert(SupplierEntry.SUPPLIERS_URI, contentValues);
        if (newUri == null) {
            Toast.makeText(getApplicationContext(), getString(R.string.error_save_sup), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getApplicationContext(), getString(R.string.supplier_saved), Toast.LENGTH_SHORT).show();
        }
    }
}
