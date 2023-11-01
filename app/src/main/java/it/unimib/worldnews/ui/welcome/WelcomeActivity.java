package it.unimib.worldnews.ui.welcome;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import it.unimib.worldnews.R;

/**
 * Activity that contains Fragments to allow user to login or to create an account.
 */
public class WelcomeActivity extends AppCompatActivity {

    private static final String TAG = WelcomeActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
    }
}
