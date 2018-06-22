package pl.pisze_czytam.bookinventory;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import pl.pisze_czytam.bookinventory.data.BookContract.BookEntry;

public class BookCursorAdapter extends CursorAdapter {

    public BookCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.book_item, parent, false);

        ViewHolder holder = new ViewHolder(view);
        view.setTag(holder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder holder = (ViewHolder) view.getTag();

        String title = cursor.getString(cursor.getColumnIndex(BookEntry.COLUMN_TITLE));
        String price = cursor.getString(cursor.getColumnIndex(BookEntry.COLUMN_PRICE));
        price += " zł";
        String quantity = cursor.getString(cursor.getColumnIndex(BookEntry.COLUMN_QUANTITY));

        holder.titleView.setText(title);
        holder.priceView.setText(price);
        holder.quantityView.setText(quantity);
    }

    private static class ViewHolder {
        private TextView titleView;
        private TextView priceView;
        private TextView quantityView;

        private ViewHolder (View view) {
            titleView = view.findViewById(R.id.title_view);
            priceView = view.findViewById(R.id.price_view);
            quantityView = view.findViewById(R.id.quantity_view);
        }
    }
}
