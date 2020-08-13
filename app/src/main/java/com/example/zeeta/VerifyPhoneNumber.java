package com.example.zeeta;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.zeeta.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskExecutors;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class VerifyPhoneNumber extends AppCompatActivity {
    Button verifyBtn;
    EditText otpET;
    ProgressBar otpProgressBar;
    private String phoneNumber;
    private String verificationCodeBySystem;
    private String name;
    private String email;
    private String password;
    private String fullName;
    private FirebaseFirestore mDb;
    private DocumentReference phoneRef;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        @Override
        public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            verificationCodeBySystem = s;
            Log.d("Check", "Czechkoncodesent called");
        }

        @Override
        public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
            String code = phoneAuthCredential.getSmsCode();
            if (code != null) {
                otpProgressBar.setVisibility(View.VISIBLE);
                verifyCode(code);
            }
        }

        @Override
        public void onVerificationFailed(@NonNull FirebaseException e) {
            Toast.makeText(VerifyPhoneNumber.this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_phone_number);
        verifyBtn = findViewById(R.id.verifyBtn);
        otpET = findViewById(R.id.otp_input);
        otpProgressBar = findViewById(R.id.otp_pbar);
        //otpProgressBar.setVisibility(View.GONE);

        mDb = FirebaseFirestore.getInstance();

        phoneNumber = getIntent().getStringExtra("phoneNumber");
        email = getIntent().getStringExtra("email");
        password = getIntent().getStringExtra("password");
        fullName = getIntent().getStringExtra("fullName");

        sendVerificationToPhone(phoneNumber);

        verifyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String code = otpET.getText().toString();
                if (code.length() < 6 || code.isEmpty()) {
                    otpET.setError("Wrong OTP...");
                    otpET.requestFocus();
                } else {
                    otpProgressBar.setVisibility(View.INVISIBLE);
                    verifyCode(code);
                }
            }
        });

    }

    private void sendVerificationToPhone(String phoneNumber) {
        Log.d("Check", "Czechksendverification called " + phoneNumber);
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                TaskExecutors.MAIN_THREAD,               // Activity (for callback binding)
                mCallbacks);        // OnVerificationStateChangedCallbacks
    }

    private void verifyCode(String codeByUser) {
        Log.d("Check", "Czechkverifycode called");
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationCodeBySystem, codeByUser);
        signInCredential(credential);
    }

    private void signInCredential(PhoneAuthCredential credential) {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                Log.d("Check", "Czechkoncomplete called");
                if (task.isSuccessful()) {
                    if (fullName != null && fullName.length() > 3) {
                        registerWithPhoneNumber();
                    } else {
                        redirectLoginScreen();
                    }

                } else {
                    Toast.makeText(VerifyPhoneNumber.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("Check", "Czechkonfailure called" + e.getMessage());
                Toast.makeText(VerifyPhoneNumber.this, e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }


    public void registerWithPhoneNumber() {
        Log.d("Check", "Czechkregisterwithnewphone called");

        otpProgressBar.setVisibility(View.VISIBLE);
        //insert some default data
        User user = new User();
        user.setEmail(email);
        user.setUsername(fullName);
        user.setUser_id(FirebaseAuth.getInstance().getUid());
        user.setPhoneNumber(phoneNumber);
        user.setNewUser(true);
        user.setRating("3.5");
        user.setWallet(0.0);

        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setTimestampsInSnapshotsEnabled(true)
                .build();
        mDb.setFirestoreSettings(settings);
        DatabaseReference ref = FirebaseDatabase.getInstance("https://zeeta-6b4c0.firebaseio.com").getReference("phone").child(phoneNumber);

        DocumentReference newUserRef = mDb
                .collection(getString(R.string.collection_users))
                .document(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()));

        newUserRef.set(user).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                ref.child("phone").setValue(phoneNumber).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            redirectLoginScreen();
                        } else {
                            View parentLayout = findViewById(android.R.id.content);
                            Snackbar.make(parentLayout, "Something went wrong.", Snackbar.LENGTH_SHORT).show();
                        }
                    }
                });

            }
        });

    }

    /**
     * Redirects the user to the login screen
     */
    private void redirectLoginScreen() {
        otpProgressBar.setVisibility(View.INVISIBLE);

        Intent intent = new Intent(VerifyPhoneNumber.this, MapActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }


}