package pl.pisze_czytam.bookinventory;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import pl.pisze_czytam.bookinventory.data.BookstoreContract.BookEntry;

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
    public void bindView(final View view, final Context context, final Cursor cursor) {
        ViewHolder holder = (ViewHolder) view.getTag();

        String title = cursor.getString(cursor.getColumnIndex(BookEntry.COLUMN_TITLE));
        double priceDouble = cursor.getDouble(cursor.getColumnIndex(BookEntry.COLUMN_PRICE));
        String price = String.valueOf(priceDouble);
        if (priceDouble == 0.0) {
            price = context.getResources().getString(R.string.priceless);
        } else {
            price += " " + context.getResources().getString(R.string.price_unit);
        }

        final int quantityInt = cursor.getInt(cursor.getColumnIndex(BookEntry.COLUMN_QUANTITY));
        String quantityText = String.valueOf(quantityInt);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        final int warnNumber = Integer.parseInt(sharedPreferences.getString(context.getApplicationContext().getResources().getString(R.string.warn_number_key), "2"));

        if (quantityInt <= warnNumber) {
            holder.quantityLabel.setTextColor(context.getApplicationContext().getResources().getColor(R.color.colorAccent));
            holder.quantityView.setTextColor(context.getApplicationContext().getResources().getColor(R.color.colorAccent));
        } else {
            holder.quantityLabel.setTextColor(context.getApplicationContext().getResources().getColor(R.color.colorPrimaryDark));
            holder.quantityView.setTextColor(context.getApplicationContext().getResources().getColor(R.color.colorPrimaryDark));
            holder.saleButton.setBackgroundColor(context.getApplicationContext().getResources().getColor(R.color.buttonColor));
            holder.saleButton.setText(R.string.sale);
        }
        if (quantityInt <= 0) {
            holder.saleButton.setBackgroundColor(context.getApplicationContext().getResources().getColor(R.color.warningButton));
            holder.saleButton.setText(R.string.order);
        }

        holder.titleView.setText(title);
        holder.priceView.setText(price);
        holder.quantityView.setText(quantityText);

        final String id = String.valueOf(cursor.getInt(cursor.getColumnIndex(BookEntry.ID)));
        final String phone = cursor.getString(cursor.getColumnIndex(BookEntry.COLUMN_SUP_PHONE));

        holder.saleButton.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("StringFormatInvalid")
            @Override
            public void onClick(View v) {
                if (quantityInt >= 1) {
                    Uri currentBookUri = Uri.withAppendedPath(BookEntry.BOOKS_URI, id);
                    ContentValues values = new ContentValues();
                    values.put(BookEntry.COLUMN_QUANTITY, quantityInt - 1);
                        if (quantityInt == warnNumber + 1) {
                            String toastText = context.getApplicationContext().getResources().getString(R.string.books_left, warnNumber);

                            View toastLayout = LayoutInflater.from(mContext).inflate(R.layout.custom_toast,
                                    (ViewGroup) view.findViewById(R.id.custom_toast_container));
                            TextView textView = toastLayout.findViewById(R.id.toast_text);
                            textView.setText(toastText);
                            Toast toast = new Toast(context.getApplicationContext());
                            toast.setDuration(Toast.LENGTH_LONG);
                            toast.setView(toastLayout);
                            toast.show();
                        }
                    int rowsAffected = context.getContentResolver().update(currentBookUri, values, null, null);
                    if (rowsAffected == 0) {
                        String toastText = context.getApplicationContext().getResources().getString(R.string.error_update_book);

                        View toastLayout = LayoutInflater.from(mContext).inflate(R.layout.custom_toast,
                                (ViewGroup) view.findViewById(R.id.custom_toast_container));
                        TextView textView = toastLayout.findViewById(R.id.toast_text);
                        textView.setText(toastText);
                        Toast toast = new Toast(context.getApplicationContext());
                        toast.setDuration(Toast.LENGTH_SHORT);
                        toast.setView(toastLayout);
                        toast.show();
                    }
                } else {
                    Intent dialIntent = new Intent(Intent.ACTION_DIAL);
                    dialIntent.setData(Uri.parse("tel:" + phone));
                    context.startActivity(dialIntent);
                }
            }
        });
    }

    private static class ViewHolder {
        private TextView titleView;
        private TextView priceView;
        private TextView quantityLabel;
        private TextView quantityView;
        private Button saleButton;

        private ViewHolder (View view) {
            titleView = view.findViewById(R.id.title_view);
            priceView = view.findViewById(R.id.price_view);
            quantityLabel = view.findViewById(R.id.quantity_label);
            quantityView = view.findViewById(R.id.quantity_view);
            saleButton = view.findViewById(R.id.sale_button);
        }
    }
}
