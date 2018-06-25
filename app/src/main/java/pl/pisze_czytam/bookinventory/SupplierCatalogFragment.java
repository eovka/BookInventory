package pl.pisze_czytam.bookinventory;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import pl.pisze_czytam.bookinventory.data.BookstoreContract.SupplierEntry;

public class SupplierCatalogFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    SupplierCursorAdapter supplierCursorAdapter;
    private static final int SUPPLIER_LOADER_ID = 1;

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.catalog_list, container, false);

        FloatingActionButton fab = rootView.findViewById(R.id.fab_add);
        fab.setImageDrawable(getResources().getDrawable(R.drawable.ic_add));
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), SupplierEditor.class));
            }
        });

        // change default image and texts when list is empty
        ImageView emptyImage = rootView.findViewById(R.id.empty_list_image);
        TextView emptyTitleText = rootView.findViewById(R.id.empty_title_text);
        TextView emptySubtitleText = rootView.findViewById(R.id.empty_subtitle_text);
        emptyImage.setImageDrawable(getResources().getDrawable(R.drawable.suppliers));
        emptyTitleText.setText(R.string.no_suppliers_title);
        emptySubtitleText.setText(R.string.no_suppliers_subtitle);

        ListView listView = rootView.findViewById(R.id.listview);
        View emptyView = rootView.findViewById(R.id.empty_view);
        listView.setEmptyView(emptyView);
        supplierCursorAdapter = new SupplierCursorAdapter(getContext(), null);
        listView.setAdapter(supplierCursorAdapter);
        getActivity().getSupportLoaderManager().initLoader(SUPPLIER_LOADER_ID, null, this);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });

        return rootView;
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] supplierProjection = {SupplierEntry.ID, SupplierEntry.COLUMN_NAME, SupplierEntry.COLUMN_PHONE};
        return new CursorLoader(getActivity(), SupplierEntry.SUPPLIERS_URI, supplierProjection, null,
                null, SupplierEntry.COLUMN_NAME + " ASC");
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        supplierCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        supplierCursorAdapter.swapCursor(null);
    }
}
