package com.example.zeeta;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.zeeta.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import static android.text.TextUtils.isEmpty;
import static com.example.zeeta.ui.Check.doStringsMatch;

public class Enrollment extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "EnrollmentActivity";
    //widgets
    private EditText mEmail, mPassword, mConfirmPassword, phoneNumber, fullName;
    private ProgressBar mProgressBar;
    //vars
    private FirebaseFirestore mDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enrollment);

        mEmail = findViewById(R.id.email_input);
        mPassword = findViewById(R.id.input_password);
        mConfirmPassword = findViewById(R.id.input_confirm_password);
        mProgressBar = findViewById(R.id.progressBar);
        phoneNumber = findViewById(R.id.phone_number);
        fullName = findViewById(R.id.username_input);

        findViewById(R.id.register_btn).setOnClickListener(this);

        mDb = FirebaseFirestore.getInstance();

        hideSoftKeyboard();

    }


    /**
     * Register a new email and password to Firebase Authentication
     *
     * @param email
     * @param password
     */
    public void registerNewEmail(final String email, String password, final String phoneNumber, final String fullName) {

        showDialog();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()) {
                            //insert some default data
                            User user = new User();
                            user.setEmail(email);
                            user.setUsername(fullName);
                            user.setUser_id(FirebaseAuth.getInstance().getUid());
                            user.setPhoneNumber(phoneNumber);
                            user.setNewUser(true);


                            FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                                    .setTimestampsInSnapshotsEnabled(true)
                                    .build();
                            mDb.setFirestoreSettings(settings);

                            DocumentReference newUserRef = mDb
                                    .collection(getString(R.string.collection_users))
                                    .document(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()));

                            newUserRef.set(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    hideDialog();

                                    if (task.isSuccessful()) {
                                        redirectLoginScreen();
                                    } else {
                                        View parentLayout = findViewById(android.R.id.content);
                                        Snackbar.make(parentLayout, "Something went wrong.", Snackbar.LENGTH_SHORT).show();
                                    }
                                }
                            });

                        } else {
                            View parentLayout = findViewById(android.R.id.content);
                            Snackbar.make(parentLayout, "Something went wrong.", Snackbar.LENGTH_SHORT).show();
                            hideDialog();
                        }

                        // ...
                    }
                });
    }

    /**
     * Redirects the user to the login screen
     */
    private void redirectLoginScreen() {
        Log.d(TAG, "redirectLoginScreen: redirecting to login screen.");
        Toast.makeText(Enrollment.this, "Redirecting to login page", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(Enrollment.this, Signin.class);
        startActivity(intent);
        finish();
    }

    private void showDialog() {
        mProgressBar.setVisibility(View.VISIBLE);
    }

    private void hideDialog() {
        if (mProgressBar.getVisibility() == View.VISIBLE) {
            mProgressBar.setVisibility(View.INVISIBLE);
        }
    }

    private void hideSoftKeyboard() {
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }


    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.register_btn: {
                Log.d(TAG, "onClick: attempting to register.");
                Toast.makeText(Enrollment.this, "Attempting to register", Toast.LENGTH_SHORT).show();

                //registerNewEmail(mEmail.getText().toString(), mPassword.getText().toString(), phoneNumber.getText().toString());

                //check for null valued EditText fields
                if (!isEmpty(mEmail.getText().toString())
                        && !isEmpty(mPassword.getText().toString())
                        && !isEmpty(mConfirmPassword.getText().toString())) {

                    //check if passwords match
                    if (doStringsMatch(mPassword.getText().toString(), mConfirmPassword.getText().toString())) {

                        if (!isEmpty(phoneNumber.getText().toString())) {
                            //Initiate registration task
                            registerNewEmail(mEmail.getText().toString(), mPassword.getText().toString(), phoneNumber.getText().toString(), fullName.getText().toString());
                        } else {
                            Toast.makeText(Enrollment.this, "Please type your phone number", Toast.LENGTH_SHORT).show();
                        }
                        //registerNewEmail(mEmail.getText().toString(), mPassword.getText().toString());
                    } else {
                        Toast.makeText(Enrollment.this, "Passwords do not Match", Toast.LENGTH_SHORT).show();
                    }

                } else {
                    Toast.makeText(Enrollment.this, "You must fill out all the fields", Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }

    }
}
