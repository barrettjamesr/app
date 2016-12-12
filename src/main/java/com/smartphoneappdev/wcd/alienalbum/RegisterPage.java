package com.smartphoneappdev.wcd.alienalbum;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import static android.Manifest.permission.READ_CONTACTS;

public class RegisterPage extends AppCompatActivity {

    private RegisterPage.UserRegisterTask mAuthTask = null;

    private AutoCompleteTextView mEmailView;
    private AutoCompleteTextView mUserNameView;
    private EditText mPasswordView;

    private View mProgressView;
    private View mRegisterFormView;

    private String strErrorMsg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_page);
        setTitle(getTitle() + ": " + "Register");

        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        mUserNameView = (AutoCompleteTextView) findViewById(R.id.username);

        mPasswordView = (EditText) findViewById(R.id.password);

        Button mRegisterButton = (Button) findViewById(R.id.email_register_button);
        mRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptRegister();
            }
        });
        mRegisterButton.requestFocus();

        mRegisterFormView = findViewById(R.id.register_form);
        mProgressView = findViewById(R.id.login_progress);
    }

    /**
     * Attempts to register the account specified by the login form.
     * If there are form errors (invalid email, duplicate email etc, missing fields, etc.), the
     * user is notified and asked to try again
     */
    private void attemptRegister() {

        // Reset errors.
        mEmailView.setError(null);
        mUserNameView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String username = mUserNameView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        if (!TextUtils.isEmpty(username) && !isUserNameValid(username)) {
            mUserNameView.setError(getString(R.string.error_invalid_username));
            focusView = mUserNameView;
            cancel = true;
        }
        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new RegisterPage.UserRegisterTask(this, email, username, password);
            mAuthTask.execute((Void) null);
        }

    }
    private boolean isEmailValid(String email) {
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 4;
    }

    private boolean isUserNameValid(String userName) {
        return userName.length() < 50;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mRegisterFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mRegisterFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mRegisterFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mRegisterFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserRegisterTask extends AsyncTask<Void, Void, Boolean> {

        private Context context;
        private final String mEmail;
        private final String mUsername;
        private final String mPassword;

        private String strResult;

        UserRegisterTask(Context context, String email, String username, String password) {
            this.context = context;
            mEmail = email;
            mUsername = username.trim();
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                String link = getString(R.string.server_dir) + "register_user_jrb.php"
                        + "?email=" + Uri.encode(mEmail, "UTF-8")
                        + "&user_name=" + Uri.encode(mUsername, "UTF-8")
                        + "&password=" + Uri.encode(mPassword, "UTF-8");

                URL url = new URL(link);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setInstanceFollowRedirects(true);
                conn.setRequestMethod("GET");
                conn.connect();

                BufferedReader reader = new BufferedReader(new InputStreamReader(
                        conn.getInputStream(), "UTF-8"));
                String parsedString = reader.readLine();

                JSONObject obj = new JSONObject(parsedString);

                strResult = obj.getString("Result");

                return !obj.getBoolean("error");

            } catch (Exception e) {
                return false;
            }
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                Toast.makeText(getApplicationContext(), getApplicationContext().getResources().getString(R.string.registered), Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(RegisterPage.this, LoginActivity.class);
                context.startActivity(intent);
                ((Activity) context).finish();
            } else {
                if(strResult.contains("user name")){
                    mUserNameView.setError(strResult);
                    mUserNameView.requestFocus();
                } else {
                    mEmailView.setError(strResult);
                    mEmailView.requestFocus();
                }
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }
}
