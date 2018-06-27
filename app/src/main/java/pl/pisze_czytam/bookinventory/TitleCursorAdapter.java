package pl.pisze_czytam.bookinventory;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import pl.pisze_czytam.bookinventory.data.BookstoreContract.BookEntry;

public class TitleCursorAdapter extends CursorAdapter {

    public TitleCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.title_item, parent, false);
        ViewHolder holder = new ViewHolder(view);
        view.setTag(holder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder holder = (ViewHolder) view.getTag();

        String title = cursor.getString(cursor.getColumnIndex(BookEntry.COLUMN_TITLE));
        holder.titleView.setText(title);
    }

    private static class ViewHolder {
        private TextView titleView;

        private ViewHolder (View view) {
            titleView = view.findViewById(R.id.booktitle_view);
        }
    }
}
