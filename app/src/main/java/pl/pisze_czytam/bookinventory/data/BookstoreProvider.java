package pl.pisze_czytam.bookinventory.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import pl.pisze_czytam.bookinventory.data.BookstoreContract.*;

public class BookstoreProvider extends ContentProvider {
    public static final String LOG_TAG = BookstoreProvider.class.getSimpleName();
    private BookstoreDbHelper dbHelper;
    private static final int BOOKS = 10;
    private static final int BOOKS_ID = 11;
    private static final int SUPPLIERS = 20;
    private static final int SUPPLIER_ID = 21;
    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        uriMatcher.addURI(BookstoreContract.CONTENT_AUTHORITY, BookstoreContract.BOOKS_PATH, BOOKS);
        uriMatcher.addURI(BookstoreContract.CONTENT_AUTHORITY, BookstoreContract.BOOKS_PATH + "/#", BOOKS_ID);
        uriMatcher.addURI(BookstoreContract.CONTENT_AUTHORITY, BookstoreContract.SUPPLIERS_PATH, SUPPLIERS);
        uriMatcher.addURI(BookstoreContract.CONTENT_AUTHORITY, BookstoreContract.SUPPLIERS_PATH + "/#", SUPPLIER_ID);
    }

    @Override
    public boolean onCreate() {
        dbHelper = new BookstoreDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
                        @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        Cursor cursor;

        int match = uriMatcher.match(uri);
        switch (match) {
            case BOOKS:
                cursor = database.query(BookEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case BOOKS_ID:
                selection = BookEntry.ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                cursor = database.query(BookEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case SUPPLIERS:
                cursor = database.query(SupplierEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case SUPPLIER_ID:
                selection = SupplierEntry.ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                cursor = database.query(SupplierEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query, unknown URI: " + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = uriMatcher.match(uri);
        switch (match) {
            case BOOKS:
                return BookEntry.BOOKS_LIST_TYPE;
            case BOOKS_ID:
                return BookEntry.BOOK_ITEM_TYPE;
            case SUPPLIERS:
                return SupplierEntry.SUPPLIERS_LIST_TYPE;
            case SUPPLIER_ID:
                return SupplierEntry.SUPPLIERS_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        final int match = uriMatcher.match(uri);
        switch (match) {
            case BOOKS:
                return insertBook(uri, values);
            case SUPPLIERS:
                return insertSupplier(uri, values);
            default:
                throw new IllegalArgumentException("Cannot insert anything for " + uri);
        }
    }

    private Uri insertBook(Uri uri, ContentValues values) {
        // prevent adding invalid book's data to database
        validateTitle(values);
        validateSupplier(values);
        validatePrice(values);
        validateQuantity(values);

        SQLiteDatabase database = dbHelper.getWritableDatabase();
        long newBookId = database.insert(BookEntry.TABLE_NAME, null, values);
        if (newBookId == -1) {
            Log.e(LOG_TAG, "Failed to insert book row for " + uri);
            return null;
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return ContentUris.withAppendedId(uri, newBookId);
    }

    private Uri insertSupplier(Uri uri, ContentValues values) {
        validateName(values);
        validatePhone(values);

        SQLiteDatabase database = dbHelper.getWritableDatabase();
        long newSupplierId = database.insert(SupplierEntry.TABLE_NAME, null, values);
        if (newSupplierId == -1) {
            Log.e(LOG_TAG, "Failed to insert supplier row for " + uri);
            return null;
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return ContentUris.withAppendedId(uri, newSupplierId);
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        int rowsDeleted;

        final int match = uriMatcher.match(uri);
        switch (match) {
            case BOOKS:
                rowsDeleted = database.delete(BookEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case BOOKS_ID:
                selection = BookEntry.ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(BookEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case SUPPLIERS:
                rowsDeleted = database.delete(SupplierEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case SUPPLIER_ID:
                selection = SupplierEntry.ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(SupplierEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Impossible to delete item with " + uri);
        }

        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        final int match = uriMatcher.match(uri);
        switch (match) {
            case BOOKS:
                return updateBook(uri, values, selection, selectionArgs);
            case BOOKS_ID:
                selection = BookEntry.ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                return updateBook(uri, values, selection, selectionArgs);
            case SUPPLIERS:
                return updateSupplier(uri, values, selection, selectionArgs);
            case SUPPLIER_ID:
                selection = SupplierEntry.ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                return updateSupplier(uri, values, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is impossible for " + uri);
        }
    }

    private int updateBook(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        if (values.size() == 0) {
            return 0;
        }
        if (values.containsKey(BookEntry.COLUMN_TITLE)) {
            validateTitle(values);
        }
        if (values.containsKey(BookEntry.COLUMN_SUPPLIER)) {
            validateSupplier(values);
        }
        if (values.containsKey(BookEntry.COLUMN_PRICE)) {
            validatePrice(values);
        }
        if (values.containsKey(BookEntry.COLUMN_QUANTITY)) {
            validateQuantity(values);
        }

        SQLiteDatabase database = dbHelper.getWritableDatabase();
        int rowsUpdated = database.update(BookEntry.TABLE_NAME, values, selection, selectionArgs);
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    private int updateSupplier(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        if (values.size() == 0) {
            return 0;
        }
        if (values.containsKey(SupplierEntry.COLUMN_NAME)) {
            validateName(values);
        }
        if (values.containsKey(SupplierEntry.COLUMN_PHONE)) {
            validatePhone(values);
        }
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        int rowsUpdated = database.update(SupplierEntry.TABLE_NAME, values, selection, selectionArgs);
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    /** helper methods to validate data put by a user in editor - used when insert and update **/
    private void validateTitle(ContentValues values) {
        String title = values.getAsString(BookEntry.COLUMN_TITLE);
        if (TextUtils.isEmpty(title)) {
            throw new IllegalArgumentException("Book requires a title.");
        }
    }
    private void validateSupplier(ContentValues values) {
        String supplier = values.getAsString(BookEntry.COLUMN_SUPPLIER);
        if (supplier.equals(BookEntry.SUPPLIER_UNKNOWN)) {
            throw new IllegalArgumentException("Book requires a supplier.");
        }
    }
    private void validatePrice(ContentValues values) {
        Double price = values.getAsDouble(BookEntry.COLUMN_PRICE);
        if (price != null && price < 0.0) {
            throw new IllegalArgumentException("Price can't be less than zero.");
        }
    }
    private void validateQuantity(ContentValues values) {
        Integer quantity = values.getAsInteger(BookEntry.COLUMN_QUANTITY);
        if (quantity != null && quantity < 0) {
            throw new IllegalArgumentException("Quantity can't be less than zero.");
        }
    }
    private void validateName(ContentValues values) {
        String name = values.getAsString(SupplierEntry.COLUMN_NAME);
        if (TextUtils.isEmpty(name)) {
            throw new IllegalArgumentException("Supplier needs a name.");
        }
    }
    private void validatePhone(ContentValues values) {
        String phone = values.getAsString(SupplierEntry.COLUMN_PHONE);
        if (TextUtils.isEmpty(phone)) {
            throw new IllegalArgumentException("Supplier needs a phone number.");
        }
    }
}