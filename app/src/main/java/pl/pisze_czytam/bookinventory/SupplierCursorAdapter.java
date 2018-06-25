package pl.pisze_czytam.bookinventory;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import pl.pisze_czytam.bookinventory.data.BookstoreContract.SupplierEntry;

public class SupplierCursorAdapter extends CursorAdapter {

    public SupplierCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.supplier_item, parent, false);

        ViewHolder holder = new ViewHolder(view);
        view.setTag(holder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder holder = (ViewHolder) view.getTag();

        String name = cursor.getString(cursor.getColumnIndex(SupplierEntry.COLUMN_NAME));
        String phone = cursor.getString(cursor.getColumnIndex(SupplierEntry.COLUMN_PHONE));

        holder.nameView.setText(name);
        holder.phoneView.setText(phone);
    }

    private static class ViewHolder {
        private TextView nameView;
        private TextView phoneView;

        private ViewHolder(View view) {
            nameView = view.findViewById(R.id.name_view);
            phoneView = view.findViewById(R.id.phone_view);
        }
    }
}
