package org.janb.shoppinglist.activity;

import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import com.mikepenz.aboutlibraries.Libs;
import com.mikepenz.aboutlibraries.LibsBuilder;
import com.mikepenz.aboutlibraries.LibsConfiguration;
import com.mikepenz.aboutlibraries.entity.Library;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import org.janb.shoppinglist.CONSTS;
import org.janb.shoppinglist.R;
import org.janb.shoppinglist.fragments.CacheListFragment;
import org.janb.shoppinglist.fragments.FavoriteListFragment;
import org.janb.shoppinglist.fragments.ShoppingListFragment;


public class MainActivity extends AppCompatActivity {

    private Drawer result = null;
    private Toolbar toolbar;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        prefs = getSharedPreferences("settings", Context.MODE_PRIVATE);
        if (prefs.getBoolean("first_start", true)){
            openSettings();
            prefs.edit().putBoolean("first_start", false).apply();
        } else {
            toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            buildDrawer();
            displayList();
        }

    }

    private void buildDrawer() {
        result = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(toolbar)
                .withHeader(R.layout.drawer_header)
                .withTranslucentStatusBar(false)
                .withActionBarDrawerToggle(true)
                .withActionBarDrawerToggleAnimated(false)
                .addDrawerItems(
                        new PrimaryDrawerItem().withName(R.string.nav_item_home).withTag(CONSTS.TAG_LIST).withIcon(R.drawable.ic_toc_black_),
                        new PrimaryDrawerItem().withName(R.string.nav_item_favorites).withTag(CONSTS.TAG_FAVORITES).withIcon(R.drawable.ic_star_rate_black)
                )
                .addStickyDrawerItems(
                        new SecondaryDrawerItem().withName(R.string.nav_item_about).withTag(CONSTS.TAG_ABOUT).withIcon(R.drawable.ic_action_about),
                        new SecondaryDrawerItem().withName(R.string.nav_item_settings).withTag(CONSTS.TAG_SETTINGS).withIcon(R.drawable.ic_action_settings)
                )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(AdapterView<?> parent, View view, int position, long id, IDrawerItem drawerItem) {
                        fragmentSelector(Integer.parseInt(drawerItem.getTag().toString()));
                        return false;
                    }
                })
                .withSelectedItem(0)
                .build();
    }

    private void fragmentSelector(int tag) {
        switch (tag){
            case CONSTS.TAG_LIST:
                displayList();
                break;
            case CONSTS.TAG_FAVORITES:
                displayFavorites();
                break;
            case CONSTS.TAG_SETTINGS:
                openSettings();
                break;
            case CONSTS.TAG_ABOUT:
                new LibsBuilder()
                    .withActivityStyle(Libs.ActivityStyle.LIGHT_DARK_TOOLBAR)
                    .withAboutVersionShown(true)
                        .withFields(R.string.class.getFields())
                        .withListener(new LibsConfiguration.LibsListener() {
                            @Override
                            public void onIconClicked(View view) {
                            }

                            @Override
                            public boolean onLibraryAuthorClicked(View view, Library library) {
                                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(library.getRepositoryLink()));
                                startActivity(browserIntent);
                                return false;
                            }

                            @Override
                            public boolean onLibraryContentClicked(View view, Library library) {
                                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(library.getRepositoryLink()));
                                startActivity(browserIntent);
                                return false;
                            }

                            @Override
                            public boolean onLibraryBottomClicked(View view, Library library) {
                                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(library.getRepositoryLink()));
                                startActivity(browserIntent);
                                return false;
                            }

                            @Override
                            public boolean onExtraClicked(View view, Libs.SpecialButton specialButton) {
                                if (specialButton == Libs.SpecialButton.SPECIAL1) {
                                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getResources().getString(R.string.github_url)));
                                    startActivity(browserIntent);
                                }
                                return false;
                            }

                            @Override
                            public boolean onIconLongClicked(View view) {
                                return false;
                            }

                            @Override
                            public boolean onLibraryAuthorLongClicked(View view, Library library) {
                                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(library.getRepositoryLink()));
                                startActivity(browserIntent);
                                return false;
                            }

                            @Override
                            public boolean onLibraryContentLongClicked(View view, Library library) {
                                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(library.getRepositoryLink()));
                                startActivity(browserIntent);
                                return false;
                            }

                            @Override
                            public boolean onLibraryBottomLongClicked(View view, Library library) {
                                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(library.getRepositoryLink()));
                                startActivity(browserIntent);
                                return false;
                            }
                    })
                    .withActivityTitle(getResources().getString(R.string.title_about))
                    .start(this);
                break;
        }
    }

    private void openSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        CacheListFragment myFragment = (CacheListFragment)getFragmentManager().findFragmentByTag("CACHE_FRAGMENT");
        if (result != null && result.isDrawerOpen()) {
            result.closeDrawer();
        } else if (myFragment != null && myFragment.isVisible()) {
            displayList();
        } else {
            super.onBackPressed();
        }
    }

    private void displayList() {
        ShoppingListFragment listFR = new ShoppingListFragment();
        android.app.FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_container, listFR);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void displayFavorites() {
        FavoriteListFragment favFR = new FavoriteListFragment();
        android.app.FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_container, favFR);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
