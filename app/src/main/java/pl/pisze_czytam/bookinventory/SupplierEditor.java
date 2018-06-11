package pl.pisze_czytam.bookinventory;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import pl.pisze_czytam.bookinventory.data.BookContract.*;
import pl.pisze_czytam.bookinventory.data.BookstoreDbHelper;
import pl.pisze_czytam.bookinventory.databinding.SuppliersEditorBinding;

public class SupplierEditor extends AppCompatActivity {
    SuppliersEditorBinding bind;

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
                 insertSupplier();
                 finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void insertSupplier() {
        String name = bind.supplierName.getText().toString().trim();
        String address = bind.supplierAddress.getText().toString().trim();
        String phone = bind.supplierPhone.getText().toString().trim();

        BookstoreDbHelper dbHelper = new BookstoreDbHelper(this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(SupplierEntry.COLUMN_NAME, name);
        contentValues.put(SupplierEntry.COLUMN_ADDRESS, address);
        contentValues.put(SupplierEntry.COLUMN_PHONE, phone);

        long newRowId = db.insert(SupplierEntry.TABLE_NAME, null, contentValues);
        if (newRowId == -1) {
            Toast.makeText(this, getString(R.string.error_save_sup), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, getString(R.string.supplier_saved) + newRowId, Toast.LENGTH_SHORT).show();
        }
    }
}
