package pl.pisze_czytam.bookinventory;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

public class BooksEditor extends AppCompatActivity {
    private Spinner suppliersSpinner;
//    private String defaultSupplier =

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.books_editor);

        suppliersSpinner = findViewById(R.id.spinner_suppliers);
        setupSpinner();
    }

    private void setupSpinner() {
        ArrayAdapter suppliersAdapter = ArrayAdapter.createFromResource(this,
                R.array.suppliers_array, R.layout.supplier_spinner);
        suppliersSpinner.setAdapter(suppliersAdapter);
//        suppliersSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                // TODO: Add what to do after clicking any of suppliers.
//        }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//       // TODO: set the default value
//            }
//        });
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
                // insertBook(); + Toast
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
