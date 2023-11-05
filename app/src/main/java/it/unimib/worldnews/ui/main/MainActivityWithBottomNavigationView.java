package it.unimib.worldnews.ui.main;

import static it.unimib.worldnews.ui.preferences.NewsPreferencesActivity.EXTRA_BUNDLE_INT;
import static it.unimib.worldnews.ui.preferences.NewsPreferencesActivity.EXTRA_BUTTON_PRESSED_COUNTER_KEY;
import static it.unimib.worldnews.ui.preferences.NewsPreferencesActivity.EXTRA_NEWS_KEY;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import it.unimib.worldnews.R;

/**
 * Activity that contains Fragments, managed by a BottomNavigationView, that
 * show the news and user preferences.
 */
public class MainActivityWithBottomNavigationView extends AppCompatActivity {

    private static final String TAG = MainActivityWithBottomNavigationView.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_with_bottom_navigation_view);

        Toolbar toolbar = findViewById(R.id.top_appbar);
        setSupportActionBar(toolbar);

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().
                findFragmentById(R.id.nav_host_fragment);
        NavController navController = navHostFragment.getNavController();
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.fragment_country_news, R.id.fragment_topic_news,
                R.id.fragment_favorite_news, R.id.fragment_settings).build();

        // For the Toolbar
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        // For the BottomNavigationView
        NavigationUI.setupWithNavController(bottomNav, navController);

        Intent intent = getIntent();
        Log.d(TAG, "Times: " + intent.getIntExtra(EXTRA_BUTTON_PRESSED_COUNTER_KEY, 0));
        Log.d(TAG, "News: " + intent.getParcelableExtra(EXTRA_NEWS_KEY));

        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            Log.d(TAG, "Int from Bundle " + bundle.getInt(EXTRA_BUNDLE_INT));
        }
    }
}
