package instant.moveadapt.com.backedupnotes;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;

import instant.moveadapt.com.backedupnotes.Cloud.CloudManager;
import instant.moveadapt.com.backedupnotes.Cloud.LoginCallback;
import instant.moveadapt.com.backedupnotes.Cloud.RegisterCallback;
import instant.moveadapt.com.backedupnotes.R;

public class LoginActivity extends Activity implements View.OnClickListener, LoginCallback,
        RegisterCallback{

    private static final String TAG = "LOGIN_ACTIVITY";
    private Button loginButton;
    private Button registerButton;
    private EditText emailEditText;
    private EditText passwordEditText;
    private RelativeLayout credentialsRelativeLayout;
    private RelativeLayout alreadyLoggedInRelativeLayout;
    private TextView messageTextView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity_layout);

        loginButton = (Button)findViewById(R.id.login_activity_layout_login_btn);
        registerButton = (Button)findViewById(R.id.login_activity_layout_register_btn);
        emailEditText = (EditText)findViewById(R.id.login_activity_layout_email_et);
        passwordEditText = (EditText)findViewById(R.id.login_activity_layout_password_et);
        credentialsRelativeLayout = (RelativeLayout)findViewById(R.id.login_activity_credentials_rl);
        alreadyLoggedInRelativeLayout = (RelativeLayout)findViewById(R.id.login_layout_logged_in_message_rl);
        messageTextView = (TextView)findViewById(R.id.login_activity_already_logged_in_tv);
        updateUIAccordingToLoginStatus();

        emailEditText.setText("conturicric@gmail.com");
        passwordEditText.setText("criccric");

        loginButton.setOnClickListener(this);
        registerButton.setOnClickListener(this);
    }

    private void updateUIAccordingToLoginStatus() {
        if (CloudManager.isLoggedIn()){
            credentialsRelativeLayout.setVisibility(View.INVISIBLE);
            alreadyLoggedInRelativeLayout.setVisibility(View.VISIBLE);
            messageTextView.setText("Welcome " + CloudManager.getLoggedInEmail());
        } else {
            credentialsRelativeLayout.setVisibility(View.VISIBLE);
            alreadyLoggedInRelativeLayout.setVisibility(View.INVISIBLE);
        }
    }
    
    @Override
    public void onClick(View v) {
        if (v == loginButton){
            if (emailEditText != null && (emailEditText.getText() == null ||
                    emailEditText.getText().toString().equals("")))
                //TODO Show error message
                return;
            if (passwordEditText != null && (passwordEditText.getText() == null ||
            passwordEditText.getText().toString().equals("")))
                return;
            String email = emailEditText.getText().toString();
            String password = passwordEditText.getText().toString();
            CloudManager.login(getApplicationContext(), email, password, this);

        }
        if (v == registerButton){
            if (emailEditText != null && (emailEditText.getText() == null ||
                    emailEditText.getText().toString().equals("")))
                //TODO Show error message
                return;
            if (passwordEditText != null && (passwordEditText.getText() == null ||
                    passwordEditText.getText().toString().equals("")))
                return;
            String email = emailEditText.getText().toString();
            String password = passwordEditText.getText().toString();
            CloudManager.register(getApplicationContext(), email, password, this);

        }
    }

    @Override
    public void onLoginSuccessful() {
        Log.d(TAG, "Login successful");
        updateUIAccordingToLoginStatus();
    }

    @Override
    public void onLoginFailed(String response) {
        Log.d(TAG, "Login failed " + response);
        updateUIAccordingToLoginStatus();
    }

    @Override
    public void onRegisterSuccessful() {
        Log.d(TAG, "Register successful");
        updateUIAccordingToLoginStatus();
    }

    @Override
    public void onRegiserFailed(String message) {
        Log.d(TAG, "Register failed " + message);
        updateUIAccordingToLoginStatus();
    }
}
