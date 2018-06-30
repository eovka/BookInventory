package pl.pisze_czytam.bookinventory;

import android.content.ContentUris;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
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

import pl.pisze_czytam.bookinventory.data.BookstoreContract.BookEntry;

public class BookCatalogFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    BookCursorAdapter bookCursorAdapter;
    private static final int BOOK_LOADER_ID = 0;

    public BookCatalogFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.catalog_list, container, false);

        FloatingActionButton fab = rootView.findViewById(R.id.fab_add);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), BookEditor.class));
            }
        });

        ImageView emptyImage = rootView.findViewById(R.id.empty_list_image);
        TextView emptyTitleText = rootView.findViewById(R.id.empty_title_text);
        TextView emptySubtitleText = rootView.findViewById(R.id.empty_subtitle_text);

        emptyImage.setImageDrawable(getResources().getDrawable(R.drawable.bookcase));
        emptyTitleText.setText(R.string.no_books_title);
        emptySubtitleText.setText(R.string.no_books_subtitle);

        ListView listView = rootView.findViewById(R.id.listview);
        View emptyView = rootView.findViewById(R.id.empty_view);
        listView.setEmptyView(emptyView);
        bookCursorAdapter = new BookCursorAdapter(getContext(), null);
        listView.setAdapter(bookCursorAdapter);
        getActivity().getSupportLoaderManager().initLoader(BOOK_LOADER_ID, null, this);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent detailsIntent = new Intent(getActivity(), BookDetails.class);
                Uri currentBookUri = ContentUris.withAppendedId(BookEntry.BOOKS_URI, id);
                detailsIntent.setData(currentBookUri);
                startActivity(detailsIntent);
            }
        });
        return rootView;
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        String orderBy = sharedPreferences.getString(getString(R.string.sort_books_key), getString(R.string.sort_books_default));
        String sortOrder = null;
        if (orderBy.equals(getString(R.string.sort_abc_value))) {
            sortOrder = BookEntry.COLUMN_TITLE + " ASC";
        } else if (orderBy.equals(getString(R.string.sort_zyx_value))) {
            sortOrder = BookEntry.COLUMN_TITLE + " DESC";
        } else if (orderBy.equals(getString(R.string.sort_new_old_value))) {
            sortOrder = BookEntry.ID + " DESC";
        }
        String[] bookProjection = {BookEntry.ID, BookEntry.COLUMN_TITLE,
                BookEntry.COLUMN_PRICE, BookEntry.COLUMN_QUANTITY, BookEntry.COLUMN_SUP_PHONE};
        return new CursorLoader(getActivity(), BookEntry.BOOKS_URI, bookProjection, null,
                null, sortOrder);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        bookCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        bookCursorAdapter.swapCursor(null);
    }
}
