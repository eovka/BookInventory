package pl.pisze_czytam.bookinventory;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class CatalogAdapter extends FragmentPagerAdapter {
    private Context context = null;

    public CatalogAdapter (FragmentManager fm, Context context) {
        super(fm);
        this.context = context;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new BookCatalogFragment();
            case 1:
                return new SupplierCatalogFragment();
            default: return null;
        }
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return context.getString(R.string.books);
            case 1:
                return context.getString(R.string.suppliers);
            default: return null;
        }
    }
}
