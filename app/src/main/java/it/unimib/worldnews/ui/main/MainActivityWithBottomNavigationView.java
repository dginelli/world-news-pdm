package it.unimib.worldnews.ui.main;

import static it.unimib.worldnews.ui.preferences.NewsPreferencesActivity.EXTRA_BUTTON_PRESSED_COUNTER_KEY;
import static it.unimib.worldnews.ui.preferences.NewsPreferencesActivity.EXTRA_NEWS_KEY;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import it.unimib.worldnews.R;
import it.unimib.worldnews.databinding.ActivityMainWithBottomNavigationViewBinding;

/**
 * Activity that contains Fragments, managed by a BottomNavigationView, that
 * show the news and user preferences.
 */
public class MainActivityWithBottomNavigationView extends AppCompatActivity {

    private static final String TAG = MainActivityWithBottomNavigationView.class.getSimpleName();

    private ActivityMainWithBottomNavigationViewBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainWithBottomNavigationViewBinding.inflate(getLayoutInflater());

        View view = binding.getRoot();
        setContentView(view);

        setSupportActionBar(binding.topAppbar);

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
    }
}
