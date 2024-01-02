package it.unimib.worldnews.ui.welcome;

import static it.unimib.worldnews.util.Constants.EMAIL_ADDRESS;
import static it.unimib.worldnews.util.Constants.ENCRYPTED_DATA_FILE_NAME;
import static it.unimib.worldnews.util.Constants.ENCRYPTED_SHARED_PREFERENCES_FILE_NAME;
import static it.unimib.worldnews.util.Constants.INVALID_CREDENTIALS_ERROR;
import static it.unimib.worldnews.util.Constants.INVALID_USER_ERROR;
import static it.unimib.worldnews.util.Constants.PASSWORD;
import static it.unimib.worldnews.util.Constants.SHARED_PREFERENCES_COUNTRY_OF_INTEREST;
import static it.unimib.worldnews.util.Constants.SHARED_PREFERENCES_FILE_NAME;
import static it.unimib.worldnews.util.Constants.ID_TOKEN;
import static it.unimib.worldnews.util.Constants.SHARED_PREFERENCES_TOPICS_OF_INTEREST;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.identity.BeginSignInResult;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.android.gms.auth.api.identity.SignInCredential;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;

import org.apache.commons.validator.routines.EmailValidator;

import java.io.IOException;
import java.security.GeneralSecurityException;

import it.unimib.worldnews.R;
import it.unimib.worldnews.model.Result;
import it.unimib.worldnews.model.User;

import it.unimib.worldnews.repository.user.IUserRepository;
import it.unimib.worldnews.ui.main.MainActivityWithBottomNavigationView;
import it.unimib.worldnews.ui.preferences.NewsPreferencesActivity;
import it.unimib.worldnews.util.DataEncryptionUtil;
import it.unimib.worldnews.util.ServiceLocator;
import it.unimib.worldnews.util.SharedPreferencesUtil;

/**
 * Fragment that allows the user to login.
 */
public class LoginFragment extends Fragment {

    private static final String TAG = LoginFragment.class.getSimpleName();
    private static final boolean USE_NAVIGATION_COMPONENT = true;

    private ActivityResultLauncher<IntentSenderRequest> activityResultLauncher;
    private ActivityResultContracts.StartIntentSenderForResult startIntentSenderForResult;

    private UserViewModel userViewModel;
    private TextInputLayout textInputLayoutEmail;
    private TextInputLayout textInputLayoutPassword;
    private LinearProgressIndicator progressIndicator;

    private SignInClient oneTapClient;
    private BeginSignInRequest signInRequest;

    private DataEncryptionUtil dataEncryptionUtil;

    public LoginFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment LoginFragment.
     */
    public static LoginFragment newInstance() {
        return new LoginFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        IUserRepository userRepository = ServiceLocator.getInstance().
                getUserRepository(requireActivity().getApplication());
        userViewModel = new ViewModelProvider(
                requireActivity(),
                new UserViewModelFactory(userRepository)).get(UserViewModel.class);
        dataEncryptionUtil = new DataEncryptionUtil(requireActivity().getApplication());

        oneTapClient = Identity.getSignInClient(requireActivity());
        signInRequest = BeginSignInRequest.builder()
            .setPasswordRequestOptions(BeginSignInRequest.PasswordRequestOptions.builder()
                    .setSupported(true)
                    .build())
            .setGoogleIdTokenRequestOptions(BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    // Your server's client ID, not your Android client ID.
                    .setServerClientId(getString(R.string.default_web_client_id))
                    // Only show accounts previously used to sign in.
                    .setFilterByAuthorizedAccounts(false)
                    .build())
            // Automatically sign in when exactly one credential is retrieved.
            .setAutoSelectEnabled(true)
            .build();

        startIntentSenderForResult = new ActivityResultContracts.StartIntentSenderForResult();

        activityResultLauncher = registerForActivityResult(startIntentSenderForResult, activityResult -> {
            if (activityResult.getResultCode() == Activity.RESULT_OK) {
                Log.d(TAG, "result.getResultCode() == Activity.RESULT_OK");
                try {
                    SignInCredential credential = oneTapClient.getSignInCredentialFromIntent(activityResult.getData());
                    String idToken = credential.getGoogleIdToken();
                    if (idToken !=  null) {
                        // Got an ID token from Google. Use it to authenticate with Firebase.
                        userViewModel.getGoogleUserMutableLiveData(idToken).observe(getViewLifecycleOwner(), authenticationResult -> {
                            if (authenticationResult.isSuccess()) {
                                User user = ((Result.UserResponseSuccess) authenticationResult).getData();
                                saveLoginData(user.getEmail(), null, user.getIdToken());
                                userViewModel.setAuthenticationError(false);
                                retrieveUserInformationAndStartActivity(user, R.id.navigate_to_newsPreferencesActivity);
                            } else {
                                userViewModel.setAuthenticationError(true);
                                progressIndicator.setVisibility(View.GONE);
                                Snackbar.make(requireActivity().findViewById(android.R.id.content),
                                        getErrorMessage(((Result.Error) authenticationResult).getMessage()),
                                        Snackbar.LENGTH_SHORT).show();
                            }
                        });
                    }
                } catch (ApiException e) {
                        Snackbar.make(requireActivity().findViewById(android.R.id.content),
                                requireActivity().getString(R.string.unexpected_error),
                                Snackbar.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void retrieveUserInformationAndStartActivity(User user, int destination) {
        progressIndicator.setVisibility(View.VISIBLE);

        userViewModel.getUserFavoriteNewsMutableLiveData(user.getIdToken()).observe(
            getViewLifecycleOwner(), userFavoriteNewsRetrievalResult -> {
                progressIndicator.setVisibility(View.GONE);
                startActivityBasedOnCondition(NewsPreferencesActivity.class, destination);
            }
        );
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (userViewModel.getLoggedUser() != null) {
            SharedPreferencesUtil sharedPreferencesUtil =
                    new SharedPreferencesUtil(requireActivity().getApplication());

            if (sharedPreferencesUtil.readStringData(SHARED_PREFERENCES_FILE_NAME,
                    SHARED_PREFERENCES_COUNTRY_OF_INTEREST) != null &&
                    sharedPreferencesUtil.readStringSetData(SHARED_PREFERENCES_FILE_NAME,
                            SHARED_PREFERENCES_TOPICS_OF_INTEREST) != null) {

                startActivityBasedOnCondition(MainActivityWithBottomNavigationView.class,
                        R.id.navigate_to_mainActivityWithBottomNavigationView);
            } else {
                startActivityBasedOnCondition(NewsPreferencesActivity.class,
                        R.id.navigate_to_newsPreferencesActivity);
            }
        }

        textInputLayoutEmail = view.findViewById(R.id.text_input_layout_email);
        textInputLayoutPassword = view.findViewById(R.id.text_input_layout_password);
        progressIndicator = view.findViewById(R.id.progress_bar);

        final Button buttonLogin = view.findViewById(R.id.button_login);
        final Button buttonGoogleLogin = view.findViewById(R.id.button_google_login);
        final Button buttonRegistration = view.findViewById(R.id.button_registration);

        dataEncryptionUtil = new DataEncryptionUtil(requireActivity().getApplication());

        try {
            Log.d(TAG, "Email address from encrypted SharedPref: " + dataEncryptionUtil.
                    readSecretDataWithEncryptedSharedPreferences(
                            ENCRYPTED_SHARED_PREFERENCES_FILE_NAME, EMAIL_ADDRESS));
            Log.d(TAG, "Password from encrypted SharedPref: " + dataEncryptionUtil.
                    readSecretDataWithEncryptedSharedPreferences(
                            ENCRYPTED_SHARED_PREFERENCES_FILE_NAME, PASSWORD));
            Log.d(TAG, "Token from encrypted SharedPref: " + dataEncryptionUtil.
                    readSecretDataWithEncryptedSharedPreferences(
                            ENCRYPTED_SHARED_PREFERENCES_FILE_NAME, ID_TOKEN));
            Log.d(TAG, "Login data from encrypted file: " + dataEncryptionUtil.
                    readSecretDataOnFile(ENCRYPTED_DATA_FILE_NAME));
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }

        buttonLogin.setOnClickListener(v -> {
            String email = textInputLayoutEmail.getEditText().getText().toString().trim();
            String password = textInputLayoutPassword.getEditText().getText().toString().trim();

            // Start login if email and password are ok
            if (isEmailOk(email) & isPasswordOk(password)) {
                if (!userViewModel.isAuthenticationError()) {
                    progressIndicator.setVisibility(View.VISIBLE);
                    userViewModel.getUserMutableLiveData(email, password, true).observe(
                        getViewLifecycleOwner(), result -> {
                            if (result.isSuccess()) {
                                User user = ((Result.UserResponseSuccess) result).getData();
                                saveLoginData(email, password, user.getIdToken());
                                userViewModel.setAuthenticationError(false);
                                retrieveUserInformationAndStartActivity(user, R.id.navigate_to_newsPreferencesActivity);
                            } else {
                                userViewModel.setAuthenticationError(true);
                                progressIndicator.setVisibility(View.GONE);
                                Snackbar.make(requireActivity().findViewById(android.R.id.content),
                                        getErrorMessage(((Result.Error) result).getMessage()),
                                        Snackbar.LENGTH_SHORT).show();
                            }
                        });
                } else {
                    userViewModel.getUser(email, password, true);
                }
            } else {
                Snackbar.make(requireActivity().findViewById(android.R.id.content),
                        R.string.check_login_data_message, Snackbar.LENGTH_SHORT).show();
            }
        });

        buttonGoogleLogin.setOnClickListener(v -> oneTapClient.beginSignIn(signInRequest)
            .addOnSuccessListener(requireActivity(), new OnSuccessListener<BeginSignInResult>() {
                @Override
                public void onSuccess(BeginSignInResult result) {
                    Log.d(TAG, "onSuccess from oneTapClient.beginSignIn(BeginSignInRequest)");
                    IntentSenderRequest intentSenderRequest =
                            new IntentSenderRequest.Builder(result.getPendingIntent()).build();
                    activityResultLauncher.launch(intentSenderRequest);
                }
            })
            .addOnFailureListener(requireActivity(), new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    // No saved credentials found. Launch the One Tap sign-up flow, or
                    // do nothing and continue presenting the signed-out UI.
                    Log.d(TAG, e.getLocalizedMessage());

                    Snackbar.make(requireActivity().findViewById(android.R.id.content),
                            requireActivity().getString(R.string.error_no_google_account_found_message),
                            Snackbar.LENGTH_SHORT).show();
                }
            }));

        buttonRegistration.setOnClickListener(v -> {
            Navigation.findNavController(view).navigate(R.id.navigate_to_registrationFragment);
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        userViewModel.setAuthenticationError(false);
    }

    /**
     * Returns the text to be shown to the user based on the type of error.
     * @param errorType The type of error.
     * @return The message to be shown to the user.
     */
    private String getErrorMessage(String errorType) {
        switch (errorType) {
            case INVALID_CREDENTIALS_ERROR:
                return requireActivity().getString(R.string.error_login_password_message);
            case INVALID_USER_ERROR:
                return requireActivity().getString(R.string.error_login_user_message);
            default:
                return requireActivity().getString(R.string.unexpected_error);
        }
    }

    /**
     * Starts another Activity using Intent or NavigationComponent.
     * @param destinationActivity The class of Activity to start.
     * @param destination The ID associated with the action defined in welcome_nav_graph.xml.
     */
    private void startActivityBasedOnCondition(Class<?> destinationActivity, int destination) {
        if (USE_NAVIGATION_COMPONENT) {
            Navigation.findNavController(requireView()).navigate(destination);
        } else {
            Intent intent = new Intent(requireContext(), destinationActivity);
            startActivity(intent);
        }
        requireActivity().finish();
    }

    /**
     * Checks if the email address has a correct format.
     * @param email The email address to be validated
     * @return true if the email address is valid, false otherwise
     */
    private boolean isEmailOk(String email) {
        // Check if the email is valid through the use of this library:
        // https://commons.apache.org/proper/commons-validator/
        if (!EmailValidator.getInstance().isValid((email))) {
            textInputLayoutEmail.setError(getString(R.string.error_email));
            return false;
        } else {
            textInputLayoutEmail.setError(null);
            return true;
        }
    }

    /**
     * Checks if the password is not empty.
     * @param password The password to be checked
     * @return True if the password is not empty, false otherwise
     */
    private boolean isPasswordOk(String password) {
        // Check if the password length is correct
        if (password.isEmpty()) {
            textInputLayoutPassword.setError(getString(R.string.error_password));
            return false;
        } else {
            textInputLayoutPassword.setError(null);
            return true;
        }
    }

    /**
     * Encrypts login data using DataEncryptionUtil class.
     * @param email The email address to be encrypted and saved
     * @param password The password to be encrypted and saved
     * @param idToken The token associated with the account
     */
    private void saveLoginData(String email, String password, String idToken) {
        try {
            dataEncryptionUtil.writeSecretDataWithEncryptedSharedPreferences(
                    ENCRYPTED_SHARED_PREFERENCES_FILE_NAME, EMAIL_ADDRESS, email);
            dataEncryptionUtil.writeSecretDataWithEncryptedSharedPreferences(
                    ENCRYPTED_SHARED_PREFERENCES_FILE_NAME, PASSWORD, password);
            dataEncryptionUtil.writeSecretDataWithEncryptedSharedPreferences(
                    ENCRYPTED_SHARED_PREFERENCES_FILE_NAME, ID_TOKEN, idToken);

            if (password != null) {
                dataEncryptionUtil.writeSecreteDataOnFile(ENCRYPTED_DATA_FILE_NAME,
                        email.concat(":").concat(password));
            }

        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }
    }
}
