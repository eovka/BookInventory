package pl.pisze_czytam.bookinventory;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.design.widget.TabLayout;
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
            case R.id.add_books:
                startActivity(new Intent(MainActivity.this, BookEditor.class));
                return true;
            case R.id.add_supplier:
                startActivity(new Intent(MainActivity.this, SupplierEditor.class));
                return true;
            case R.id.setting:
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                return true;
            case R.id.delete_data:
                showDeleteDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
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
