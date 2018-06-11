package pl.pisze_czytam.bookinventory.data;

import android.provider.BaseColumns;

public final class BookContract {
    private BookContract() {
    }

    public static abstract class BookEntry implements BaseColumns {
        public static final String TABLE_NAME = "books";
        public static final String ID = BaseColumns._ID;
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_AUTHOR = "author";
        public static final String COLUMN_PRICE = "price";
        public static final String COLUMN_QUANTITY = "quantity";
        public static final String COLUMN_SUPPLIER = "supplier";
        public static final String COLUMN_SUP_PHONE = "telephone";
        public static final String COLUMN_SUP_ADDRESS = "address";

        public static final String SUPPLIER_UNKNOWN = "unknown";
        public static final String PHONE_UNKNOWN = "unknown";
        public static final String ADDRESS_UNKNOWN = "unknown";

        public static final double PRICE_DEFAULT = 0.00d;
        public static final int NUMBER_DEFAULT = 0;
    }

    public static abstract class SupplierEntry implements BaseColumns {
        public static final String TABLE_NAME = "suppliers";
        public static final String ID = BaseColumns._ID;
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_ADDRESS = "address";
        public static final String COLUMN_PHONE = "phone";
    }
}
