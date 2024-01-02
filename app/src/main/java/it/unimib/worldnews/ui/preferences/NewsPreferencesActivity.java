package it.unimib.worldnews.ui.preferences;

import static it.unimib.worldnews.util.Constants.BUSINESS;
import static it.unimib.worldnews.util.Constants.ENCRYPTED_SHARED_PREFERENCES_FILE_NAME;
import static it.unimib.worldnews.util.Constants.ENTERTAINMENT;
import static it.unimib.worldnews.util.Constants.FRANCE;
import static it.unimib.worldnews.util.Constants.GENERAL;
import static it.unimib.worldnews.util.Constants.GERMANY;
import static it.unimib.worldnews.util.Constants.HEALTH;
import static it.unimib.worldnews.util.Constants.ID_TOKEN;
import static it.unimib.worldnews.util.Constants.ITALY;
import static it.unimib.worldnews.util.Constants.SCIENCE;
import static it.unimib.worldnews.util.Constants.SHARED_PREFERENCES_COUNTRY_OF_INTEREST;
import static it.unimib.worldnews.util.Constants.SHARED_PREFERENCES_FILE_NAME;
import static it.unimib.worldnews.util.Constants.SHARED_PREFERENCES_FIRST_LOADING;
import static it.unimib.worldnews.util.Constants.SHARED_PREFERENCES_TOPICS_OF_INTEREST;
import static it.unimib.worldnews.util.Constants.SPORTS;
import static it.unimib.worldnews.util.Constants.TECHNOLOGY;
import static it.unimib.worldnews.util.Constants.UNITED_KINGDOM;
import static it.unimib.worldnews.util.Constants.UNITED_STATES;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.ViewModelProvider;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import it.unimib.worldnews.R;
import it.unimib.worldnews.model.News;
import it.unimib.worldnews.model.NewsSource;
import it.unimib.worldnews.data.repository.user.IUserRepository;
import it.unimib.worldnews.ui.main.MainActivityWithBottomNavigationView;
import it.unimib.worldnews.ui.welcome.UserViewModel;
import it.unimib.worldnews.ui.welcome.UserViewModelFactory;
import it.unimib.worldnews.util.DataEncryptionUtil;
import it.unimib.worldnews.util.ServiceLocator;
import it.unimib.worldnews.util.SharedPreferencesUtil;

/**
 * Activity to allow user to choose her news preferences.
 */
public class NewsPreferencesActivity extends AppCompatActivity {

    private static final String TAG = NewsPreferencesActivity.class.getSimpleName();

    public static final String EXTRA_BUTTON_PRESSED_COUNTER_KEY = "BUTTON_PRESSED_COUNTER_KEY";
    public static final String EXTRA_NEWS_KEY = "NEWS_KEY";

    private Spinner spinnerCountries;
    private CheckBox checkboxBusiness;
    private CheckBox checkboxEntertainment;
    private CheckBox checkboxGeneral;
    private CheckBox checkboxHealth;
    private CheckBox checkboxScience;
    private CheckBox checkboxSport;
    private CheckBox checkboxTechnology;

    private LinearProgressIndicator linearProgressIndicator;

    private int buttonNextPressedCounter;
    private News news;

    private ActivityResultLauncher<String> singlePermissionLauncher;
    private ActivityResultContracts.RequestPermission singlePermissionContract;

    private ActivityResultLauncher<String[]> multiplePermissionLauncher;
    private ActivityResultContracts.RequestMultiplePermissions multiplePermissionsContract;

    private UserViewModel userViewModel;

    private final String[] PERMISSIONS = {
            Manifest.permission.SEND_SMS,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    private FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_news_preferences_constraint_layout);

        IUserRepository userRepository = ServiceLocator.getInstance().
                getUserRepository(getApplication());
        userViewModel = new ViewModelProvider(
                this, new UserViewModelFactory(userRepository)).get(UserViewModel.class);

        spinnerCountries = findViewById(R.id.spinner_countries);

        checkboxBusiness = findViewById(R.id.checkbox_business);
        checkboxEntertainment = findViewById(R.id.checkbox_entertainment);
        checkboxGeneral = findViewById(R.id.checkbox_general);
        checkboxHealth = findViewById(R.id.checkbox_health);
        checkboxScience = findViewById(R.id.checkbox_science);
        checkboxSport = findViewById(R.id.checkbox_sport);
        checkboxTechnology = findViewById(R.id.checkbox_technology);

        linearProgressIndicator = findViewById(R.id.progress_bar);

        final Button buttonNext = findViewById(R.id.button_next);

        linearProgressIndicator.setVisibility(View.VISIBLE);

        userViewModel.getUserPreferences(userViewModel.getLoggedUser().getIdToken()).
            observe(this, result -> {
                linearProgressIndicator.setVisibility(View.GONE);
                setViewsChecked();
            });

        // Doc is here: https://developer.android.com/training/location/retrieve-current
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if (savedInstanceState != null) {
            buttonNextPressedCounter = savedInstanceState.getInt(EXTRA_BUTTON_PRESSED_COUNTER_KEY);
            news = savedInstanceState.getParcelable(EXTRA_NEWS_KEY);
            Log.d(TAG, "savedInstanceState is not null, news: " + news.toString());
        } else {
            news = new News("Mario Rossi", "Button next has been pressed 0 times",
                    new NewsSource("UniMiB"),
                    GregorianCalendar.getInstance().getTime().toString());

            Log.d(TAG, "savedInstanceState is null, News: " + news);
        }

        singlePermissionContract = new ActivityResultContracts.RequestPermission();
        multiplePermissionsContract = new ActivityResultContracts.RequestMultiplePermissions();

        singlePermissionLauncher = registerForActivityResult(singlePermissionContract, isGranted -> {
            if (isGranted) {
                // Permission is granted. Continue the action or workflow in your app.
                Log.d(TAG, "Single permission has been granted");
                getLocation();
            } else {
                // Explain to the user that the feature is unavailable because the feature requires
                // a permission that the user has denied. At the same time, respect
                // the user's decision. Don't link to system settings in an effort to convince the
                // user to change their decision.
                Log.d(TAG, "Single permission has not been granted");
            }
        });

        multiplePermissionLauncher = registerForActivityResult(multiplePermissionsContract, isGranted -> {
            for(Map.Entry<String, Boolean> set : isGranted.entrySet()) {
                Log.d(TAG, set.getKey() + " " + set.getValue());
            }
            if (!isGranted.containsValue(false)) {
                Log.d(TAG, "All permission have been granted");
                getLocation();
            }
        });

        buttonNext.setOnClickListener(view -> {
            buttonNextPressedCounter++;
            news.setTitle("Button next has been pressed " + buttonNextPressedCounter + " times");
            if (isCountryOfInterestSelected() && isTopicOfInterestSelected()) {
                Log.d(TAG, "One country of interest and at least one topic has been chosen");
                saveInformation();
                buttonNextPressedCounter++;
                news.setTitle("Button next has been pressed " + buttonNextPressedCounter + " times");
                news.setDate(GregorianCalendar.getInstance().getTime().toString());
                Log.d(TAG, "Button next has been pressed " + buttonNextPressedCounter + " times");
                Log.d(TAG, "onClick, News: " + news);
                Intent intent = new Intent(this, MainActivityWithBottomNavigationView.class);
                intent.putExtra(EXTRA_BUTTON_PRESSED_COUNTER_KEY, buttonNextPressedCounter);
                intent.putExtra(EXTRA_NEWS_KEY, news);
                startActivity(intent);
                finish();
            }
        });
    }

    /**
     * It finds the last known location of the user.
     */
    private void getLocation() {

        boolean singlePermissionsStatus =
                ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        boolean multiplePermissionsStatus =
                ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(this,
                                Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED;

        // Use multiplePermissionsStatus if you want to try the logic
        // of requesting more than one permission
        if (singlePermissionsStatus) {
            Log.d(TAG, "getLocation(): All permissions have been granted");
            // Doc is here: https://developer.android.com/training/location/retrieve-current
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            Log.d(TAG, location.getLatitude() + " " + location.getLongitude());
                        }
                    });
        } else {
            Log.d(TAG, "One or more permissions have not been granted");
            // Use multiplePermissionLauncher and PERMISSIONS variable as argument of method launch
            // if you want to try the logic of requesting more than one permission
            singlePermissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION);
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(TAG, "onSaveInstanceState");
        outState.putInt(EXTRA_BUTTON_PRESSED_COUNTER_KEY, buttonNextPressedCounter);
        outState.putParcelable(EXTRA_NEWS_KEY, news);
    }

    /**
     * Checks if a country of interest has been selected.
     * @return true if a country has been selected, false otherwise
     */
    private boolean isCountryOfInterestSelected() {

        if (spinnerCountries.getSelectedItem() != null) {
            return true;
        }

        Snackbar.make(findViewById(android.R.id.content), R.string.alert_text_country_of_interest,
                Snackbar.LENGTH_SHORT).show();
        return false;
    }

    /**
     * Checks if at least one topic of interest has been chosen.
     * @return true if at least one topic has been chosen, false otherwise
     */
    private boolean isTopicOfInterestSelected() {

        if (checkboxBusiness.isChecked() || checkboxEntertainment.isChecked() ||
                checkboxGeneral.isChecked() || checkboxHealth.isChecked() ||
                checkboxScience.isChecked() || checkboxSport.isChecked() ||
                checkboxTechnology.isChecked()) {
            return true;
        }

        getLocation();

        Snackbar.make(findViewById(android.R.id.content), R.string.alert_text_topic_of_interest,
                Snackbar.LENGTH_SHORT).show();
        return false;
    }

    /**
     * Stores the country and the topics of interest chosen by the user
     * in the file system through the use of SharedPreferences API.
     */
    private void saveInformation() {

        String country = spinnerCountries.getSelectedItem().toString();
        String countryShortName = getShortNameCountryOfInterest(country);

        Set<String> topics = new HashSet<>();

        if (checkboxBusiness.isChecked()) {
            topics.add(BUSINESS);
        }
        if (checkboxEntertainment.isChecked()) {
            topics.add(ENTERTAINMENT);
        }
        if (checkboxGeneral.isChecked()) {
            topics.add(GENERAL);
        }
        if (checkboxHealth.isChecked()) {
            topics.add(HEALTH);
        }
        if (checkboxScience.isChecked()) {
            topics.add(SCIENCE);
        }
        if (checkboxSport.isChecked()) {
            topics.add(SPORTS);
        }
        if (checkboxTechnology.isChecked()) {
            topics.add(TECHNOLOGY);
        }

        SharedPreferencesUtil sharedPreferencesUtil = new SharedPreferencesUtil(getApplication());
        sharedPreferencesUtil.writeStringData(
                SHARED_PREFERENCES_FILE_NAME, SHARED_PREFERENCES_COUNTRY_OF_INTEREST,
                countryShortName);

        sharedPreferencesUtil.writeStringSetData(SHARED_PREFERENCES_FILE_NAME,
                SHARED_PREFERENCES_TOPICS_OF_INTEREST, topics);

        sharedPreferencesUtil.writeBooleanData(SHARED_PREFERENCES_FILE_NAME,
                SHARED_PREFERENCES_FIRST_LOADING, true);

        DataEncryptionUtil dataEncryptionUtil = new DataEncryptionUtil(getApplication());

        String idToken = null;
        try {
            idToken = dataEncryptionUtil.readSecretDataWithEncryptedSharedPreferences(
                    ENCRYPTED_SHARED_PREFERENCES_FILE_NAME, ID_TOKEN);
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }

        userViewModel.saveUserPreferences(countryShortName, topics, idToken);
    }

    /**
     * Sets the spinner and the checkbox values based on what it has been saved in the
     * SharedPreferences file.
     */
    private void setViewsChecked() {
        SharedPreferencesUtil sharedPreferencesUtil = new SharedPreferencesUtil(getApplication());

        String countryOfInterest = sharedPreferencesUtil.readStringData(
                SHARED_PREFERENCES_FILE_NAME, SHARED_PREFERENCES_COUNTRY_OF_INTEREST);
        Set<String> topicsOfInterest = sharedPreferencesUtil.readStringSetData(SHARED_PREFERENCES_FILE_NAME,
                SHARED_PREFERENCES_TOPICS_OF_INTEREST);

        if (countryOfInterest != null) {
            spinnerCountries.setSelection(
                    getSpinnerPositionBasedOnValue(getUserVisibleCountryOfInterest(countryOfInterest)));
        }
        if (topicsOfInterest != null) {
            if (topicsOfInterest.contains(BUSINESS)) {
                checkboxBusiness.setChecked(true);
            }
            if (topicsOfInterest.contains(ENTERTAINMENT)) {
                checkboxEntertainment.setChecked(true);
            }
            if (topicsOfInterest.contains(GENERAL)) {
                checkboxGeneral.setChecked(true);
            }
            if (topicsOfInterest.contains(HEALTH)) {
                checkboxHealth.setChecked(true);
            }
            if (topicsOfInterest.contains(SCIENCE)) {
                checkboxScience.setChecked(true);
            }
            if (topicsOfInterest.contains(SPORTS)) {
                checkboxSport.setChecked(true);
            }
            if (topicsOfInterest.contains(TECHNOLOGY)) {
                checkboxTechnology.setChecked(true);
            }
        }
    }

    /**
     * Allows to get the position of a given country in the string array
     * used to fill the spinner.
     * @param value the country to be checked in the string array
     * @return the position of the country in the string array
     */
    private int getSpinnerPositionBasedOnValue(String value) {

        String[] countries = getResources().getStringArray(R.array.country_array);

        for (int i = 0; i < countries.length; i++) {
            if (countries[i].equals(value)) {
                return i;
            }
        }

        return 0;
    }

    /**
     * Gets the country name shown to the user.
     * @param shortNameCountryOfInterest the country short name used with NewsAPI.org
     * @return the country name shown to the user
     */
    private String getUserVisibleCountryOfInterest(String shortNameCountryOfInterest) {
        switch (shortNameCountryOfInterest) {
            case FRANCE:
                return getString(R.string.france);
            case GERMANY:
                return getString(R.string.germany);
            case ITALY:
                return getString(R.string.italy);
            case UNITED_KINGDOM:
                return getString(R.string.united_kingdom);
            case UNITED_STATES:
                return getString(R.string.united_states);
            default:
                return null;
        }
    }

    /**
     * Gets the country short name used with NewsAPI.org
     * @param userVisibleCountryOfInterest the country name shown to the user
     * @return the country short name used with NewsAPI.org
     */
    private String getShortNameCountryOfInterest(String userVisibleCountryOfInterest) {
        if (userVisibleCountryOfInterest.equals(getString(R.string.france))) {
            return FRANCE;
        } else if (userVisibleCountryOfInterest.equals(getString(R.string.germany))) {
            return GERMANY;
        } else if (userVisibleCountryOfInterest.equals(getString(R.string.italy))) {
            return ITALY;
        } else if (userVisibleCountryOfInterest.equals(getResources().getString(R.string.united_kingdom))) {
            return UNITED_KINGDOM;
        } else if (userVisibleCountryOfInterest.equals(getResources().getString(R.string.united_states))) {
            return UNITED_STATES;
        }
        return null;
    }
}
