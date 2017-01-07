package com.bocha.calendartest;

/**
 * Created by bob on 03.01.17.
 *
 * source: http://sourcey.com/beautiful-android-login-and-signup-screens-with-material-design/
 *
 */

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bocha.calendartest.activities.NewEventsActivity;
import com.bocha.calendartest.activities.SignupActivity;

import org.w3c.dom.Text;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    public static final String PREFS_NAME = "LoginPrefs";
    private static final int REQUEST_SIGNUP = 0;

    private SharedPreferences userData;

    private EditText _emailText;
    private EditText _passwordText;
    private Button _loginButton;
    private TextView _signupLink;

    private String userName;
    private String userMail;
    private String userPass;

    private boolean authenticationError = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        _emailText = (EditText) findViewById(R.id.input_email);
        _passwordText = (EditText) findViewById(R.id.input_password);
        _loginButton = (Button) findViewById(R.id.btn_login);
        _signupLink = (TextView) findViewById(R.id.link_signup);

        checkLoginStatus();

        _loginButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                login();
            }
        });

        _signupLink.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // Start the Signup activity
                Intent intent = new Intent(getApplicationContext(), SignupActivity.class);
                startActivityForResult(intent, REQUEST_SIGNUP);
            }
        });
    }

    private void checkLoginStatus() {
        userData = getSharedPreferences(PREFS_NAME, 0);
        userName = userData.getString("userName", "Default");
        userMail = userData.getString("userMail", "Default");
        userPass = userData.getString("userPass", "Default");

        if(userMail != "Default" && userPass != "Default" && validate(userMail, userPass)){
            Log.v(TAG, "Automatic login restored usermail: "+userMail+" --- userpass: "+userPass);
            showLoginInfo();
        }
    }

    private void showLoginInfo() {
        final ProgressDialog progressDialog = new ProgressDialog(LoginActivity.this);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Automatic log in as " + userMail);
        progressDialog.setCancelable(false);
        progressDialog.show();

        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        if(progressDialog != null && progressDialog.isShowing()){
                            progressDialog.dismiss();
                        }

                        loginToApp();

                    }
                }, 2000);
    }

    public void login() {
        Log.d(TAG, "Login");

        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();

        if (!validate(email, password)) {
            onLoginFailed();
            return;
        }

        _loginButton.setEnabled(false);

        final ProgressDialog progressDialog = new ProgressDialog(LoginActivity.this);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Authenticating...");
        progressDialog.show();

        // TODO: Implement your own authentication logic here.
        authenticateUser(email, password);

        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        // On complete call either onLoginSuccess or onLoginFailed
                        if(!authenticationError){
                            Log.v(TAG, "Login success");
                            onLoginSuccess();
                        }else{
                            Log.v(TAG, "Login fail");
                            onLoginFailed();
                        }
                        progressDialog.dismiss();
                    }
                }, 3000);
    }

    private void authenticateUser(String email, String password) {

        //TODO:Authenticate user per server request

        userMail = userData.getString("userMail", "Default");
        userPass = userData.getString("userPass", "Default");

        Log.v(TAG, "alt: "+userMail+userPass);
        Log.v(TAG, "neu: "+email+password);
        if(email.equals(userMail) && password.equals(userPass)){
            authenticationError = false;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SIGNUP) {
            if (resultCode == RESULT_OK) {

                // TODO: Implement successful signup logic here
                // By default we just finish the Activity and log them in automatically
                //this.finish();


            }
        }
    }

    @Override
    public void onBackPressed() {
        // disable going back to the MainActivity
        moveTaskToBack(true);
    }

    public void onLoginSuccess() {
        _loginButton.setEnabled(true);

        authenticationError = true;

        //finish();

        loginToApp();
    }

    private void loginToApp() {
        Intent loginIntent = new Intent(this, NewEventsActivity.class);
        startActivity(loginIntent);
    }

    public void onLoginFailed() {
        Toast.makeText(getBaseContext(), "Login failed", Toast.LENGTH_LONG).show();

        _loginButton.setEnabled(true);
    }

    public boolean validate(String email, String password) {
        boolean valid = true;

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _emailText.setError("enter a valid email address");
            valid = false;
        } else {
            _emailText.setError(null);
        }

        if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
            _passwordText.setError("between 4 and 10 alphanumeric characters");
            valid = false;
        } else {
            _passwordText.setError(null);
        }

        return valid;
    }
}