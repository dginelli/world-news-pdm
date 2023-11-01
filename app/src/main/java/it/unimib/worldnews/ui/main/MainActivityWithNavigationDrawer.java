package it.unimib.worldnews.ui.main;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import android.os.Bundle;

import com.google.android.material.navigation.NavigationView;

import it.unimib.worldnews.R;

/**
 * Activity that contains Fragments, managed by a NavigationView (NavigationDrawer),
 * that show the news and user preferences.
 */
public class MainActivityWithNavigationDrawer extends AppCompatActivity {

    private static final String TAG = MainActivityWithNavigationDrawer.class.getSimpleName();

    private AppBarConfiguration appBarConfiguration;
    private NavController navController;
    private NavigationView navView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_with_navigation_drawer);

        Toolbar toolbar = findViewById(R.id.top_appbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        navView = findViewById(R.id.nav_view);

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().
                findFragmentById(R.id.nav_host_fragment);
        navController = navHostFragment.getNavController();

        appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.fragment_country_news, R.id.fragment_topic_news,
                R.id.fragment_favorite_news, R.id.fragment_settings).setOpenableLayout(drawerLayout)
                .build();

        navView.setCheckedItem(R.id.fragment_country_news);

        // For the Toolbar
        NavigationUI.setupActionBarWithNavController(this, navController,
                appBarConfiguration);

        // For the NavigationView (NavigationDrawer)
        NavigationUI.setupWithNavController(navView, navController);
    }

    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController, appBarConfiguration);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        NavDestination navDestination = navController.getCurrentDestination();
        if (navDestination != null) {
            int currentDestination = navController.getCurrentDestination().getId();
            if(currentDestination == R.id.fragment_country_news ){
                navView.setCheckedItem(R.id.fragment_country_news);
            } else if(currentDestination == R.id.fragment_topic_news ){
                navView.setCheckedItem(R.id.fragment_topic_news);
            } else if(currentDestination == R.id.fragment_favorite_news){
                navView.setCheckedItem(R.id.fragment_favorite_news);
            } else if (currentDestination == R.id.fragment_settings) {
                navView.setCheckedItem(R.id.fragment_settings);
            }
        }
    }
}
