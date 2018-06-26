package pl.pisze_czytam.bookinventory.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public final class BookstoreContract {
    public static final String CONTENT_AUTHORITY = "pl.pisze_czytam.bookinventory";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String BOOKS_PATH = "books";
    public static final String SUPPLIERS_PATH = "suppliers";

    private BookstoreContract() {
    }

    public static abstract class BookEntry implements BaseColumns {
        public static final Uri BOOKS_URI = Uri.withAppendedPath(BASE_CONTENT_URI, BOOKS_PATH);
        public static final String BOOKS_LIST_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/"
                + CONTENT_AUTHORITY + "/" + BOOKS_PATH;
        public static final String BOOK_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/"
                + CONTENT_AUTHORITY + "/" + BOOKS_PATH;

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
        public static final Uri SUPPLIERS_URI = Uri.withAppendedPath(BASE_CONTENT_URI, SUPPLIERS_PATH);
        public static final String SUPPLIERS_LIST_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/"
                + CONTENT_AUTHORITY + "/" + SUPPLIERS_PATH;
        public static final String SUPPLIERS_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/"
                + CONTENT_AUTHORITY + "/" + SUPPLIERS_PATH;

        public static final String TABLE_NAME = "suppliers";
        public static final String ID = BaseColumns._ID;
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_ADDRESS = "address";
        public static final String COLUMN_MAIL = "email";
        public static final String COLUMN_PHONE = "phone";
    }
}
