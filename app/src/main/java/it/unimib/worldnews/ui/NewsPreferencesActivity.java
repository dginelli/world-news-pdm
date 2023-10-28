package it.unimib.worldnews.ui;

import static it.unimib.worldnews.util.Constants.BUSINESS;
import static it.unimib.worldnews.util.Constants.ENTERTAINMENT;
import static it.unimib.worldnews.util.Constants.FRANCE;
import static it.unimib.worldnews.util.Constants.GENERAL;
import static it.unimib.worldnews.util.Constants.GERMANY;
import static it.unimib.worldnews.util.Constants.HEALTH;
import static it.unimib.worldnews.util.Constants.ITALY;
import static it.unimib.worldnews.util.Constants.SCIENCE;
import static it.unimib.worldnews.util.Constants.SHARED_PREFERENCES_COUNTRY_OF_INTEREST;
import static it.unimib.worldnews.util.Constants.SHARED_PREFERENCES_FILE_NAME;
import static it.unimib.worldnews.util.Constants.SHARED_PREFERENCES_TOPICS_OF_INTEREST;
import static it.unimib.worldnews.util.Constants.SPORTS;
import static it.unimib.worldnews.util.Constants.TECHNOLOGY;
import static it.unimib.worldnews.util.Constants.UNITED_KINGDOM;
import static it.unimib.worldnews.util.Constants.UNITED_STATES;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;

import com.google.android.material.snackbar.Snackbar;

import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Set;

import it.unimib.worldnews.R;
import it.unimib.worldnews.model.News;

/**
 * Activity to allow user to choose her news preferences.
 */
public class NewsPreferencesActivity extends AppCompatActivity {

    private static final String TAG = NewsPreferencesActivity.class.getSimpleName();

    static final String EXTRA_BUTTON_PRESSED_COUNTER_KEY = "BUTTON_PRESSED_COUNTER_KEY";
    static final String EXTRA_NEWS_KEY = "NEWS_KEY";

    private Spinner spinnerCountries;
    private CheckBox checkboxBusiness;
    private CheckBox checkboxEntertainment;
    private CheckBox checkboxGeneral;
    private CheckBox checkboxHealth;
    private CheckBox checkboxScience;
    private CheckBox checkboxSport;
    private CheckBox checkboxTechnology;

    private int buttonNextPressedCounter;
    private News news;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_news_preferences_constraint_layout);

        spinnerCountries = findViewById(R.id.spinner_countries);

        checkboxBusiness = findViewById(R.id.checkbox_business);
        checkboxEntertainment = findViewById(R.id.checkbox_entertainment);
        checkboxGeneral = findViewById(R.id.checkbox_general);
        checkboxHealth = findViewById(R.id.checkbox_health);
        checkboxScience = findViewById(R.id.checkbox_science);
        checkboxSport = findViewById(R.id.checkbox_sport);
        checkboxTechnology = findViewById(R.id.checkbox_technology);

        final Button buttonNext = findViewById(R.id.button_next);

        if (savedInstanceState != null) {
            buttonNextPressedCounter = savedInstanceState.getInt(EXTRA_BUTTON_PRESSED_COUNTER_KEY);
            news = savedInstanceState.getParcelable(EXTRA_NEWS_KEY);
            if (news != null) {
                Log.d(TAG, "savedInstanceState is not null, news: " + news);
            }
        } else {
            news = new News("Button next has been pressed 0 times",
                    "Mario Rossi", "UniMiB",
                    GregorianCalendar.getInstance().getTime().toString());

            Log.d(TAG, "savedInstanceState is null, news: " + news);
        }

        setViewsChecked();

        buttonNext.setOnClickListener(view -> {
            if (isCountryOfInterestSelected() && isTopicOfInterestSelected()) {
                Log.d(TAG, "One country of interest and at least one topic have been chosen");
                saveInformation();
                buttonNextPressedCounter++;
                news.setTitle("Button next has been pressed " + buttonNextPressedCounter + " times");
                news.setDate(GregorianCalendar.getInstance().getTime().toString());
                Log.d(TAG, "Button next has been pressed " + buttonNextPressedCounter + " times");
                Log.d(TAG, "onClick, news: " + news);
            }
        });
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
     * @return true if a country has been selected, false otherwise.
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

        Snackbar.make(findViewById(android.R.id.content), R.string.alert_text_topic_of_interest,
                Snackbar.LENGTH_SHORT).show();
        return false;
    }

    /**
     * Stores the country and the topics of interest chosen by the user
     * in the file system through the use of SharedPreferences API
     */
    private void saveInformation() {

        String country = spinnerCountries.getSelectedItem().toString();
        String countryShortName = null;

        if (country.equals(getString(R.string.france))) {
            countryShortName = FRANCE;
        } else if (country.equals(getString(R.string.germany))) {
            countryShortName = GERMANY;
        } else if (country.equals(getString(R.string.italy))) {
            countryShortName = ITALY;
        } else if (country.equals(getResources().getString(R.string.united_kingdom))) {
            countryShortName = UNITED_KINGDOM;
        } else if (country.equals(getResources().getString(R.string.united_states))) {
            countryShortName = UNITED_STATES;
        }

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

        SharedPreferences sharedPref = getSharedPreferences(SHARED_PREFERENCES_FILE_NAME,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(SHARED_PREFERENCES_COUNTRY_OF_INTEREST, countryShortName);
        editor.putStringSet(SHARED_PREFERENCES_TOPICS_OF_INTEREST, topics);
        editor.apply();
    }

    /**
     * Sets the spinner and the checkbox values based on what it has been saved in the
     * SharedPreferences file.
     */
    private void setViewsChecked() {
        SharedPreferences sharedPref = getSharedPreferences(SHARED_PREFERENCES_FILE_NAME,
                Context.MODE_PRIVATE);

        String countryOfInterest = sharedPref.getString(SHARED_PREFERENCES_COUNTRY_OF_INTEREST,
                null);
        Set<String> topicsOfInterest = sharedPref.getStringSet(SHARED_PREFERENCES_TOPICS_OF_INTEREST,
                null);

        if (countryOfInterest != null) {
            spinnerCountries.setSelection(
                    getSpinnerPositionBasedOnValue(getCountryOfInterest(countryOfInterest)));

            Log.d(TAG, "Country of interest from SharedPref: " + countryOfInterest);
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

            Log.d(TAG, "Country of interest from SharedPref: " + topicsOfInterest);
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
     * Retrieves the country id to be used with NewsAPI.org.
     * @param countryOfInterest the country chosen by the user
     * @return the country id
     */
    private String getCountryOfInterest(String countryOfInterest) {
        switch (countryOfInterest) {
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
}
