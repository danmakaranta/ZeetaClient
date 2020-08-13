package com.example.zeeta;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.zeeta.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class Login extends AppCompatActivity {
    ProgressBar loginProgressBar;
    DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("phone");
    private Button loginBtn;
    private TextView signUpTxt;
    private EditText phoneEditTxt;
    //Firebase
    private FirebaseAuth.AuthStateListener mAuthListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        loginBtn = findViewById(R.id.loginPhone_btn);
        signUpTxt = findViewById(R.id.signPhone_up);
        phoneEditTxt = findViewById(R.id.phoneETX_input);
        loginProgressBar = findViewById(R.id.login_pbar);
        loginProgressBar.setVisibility(View.GONE);

        setupFirebaseAuth();

        signUpTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Login.this, Enrollment.class));
            }
        });

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String temp = phoneEditTxt.getText().toString();
                String phoneNum;
                if (temp.startsWith("0")) {
                    phoneNum = "+234" + temp.substring(1);
                    Log.d("start", "kdkdkdstarts with zero");
                } else {
                    phoneNum = "+234" + phoneEditTxt.getText().toString();
                    Log.d("start", "kdkdkddoes not start with zero");
                }

                DatabaseReference ref = FirebaseDatabase.getInstance("https://zeeta-6b4c0.firebaseio.com").getReference("phone");
                loginProgressBar.setVisibility(View.VISIBLE);
                if (phoneNum.isEmpty() || phoneNum.length() < 9) {
                    loginProgressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(Login.this, "Invalid phone number", Toast.LENGTH_SHORT).show();
                } else {
                    ref.orderByChild("phone").equalTo(phoneNum).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                loginProgressBar.setVisibility(View.INVISIBLE);
                                Intent loginIntent = new Intent(Login.this, VerifyPhoneNumber.class);
                                loginIntent.putExtra("phoneNumber", phoneNum);
                                startActivity(loginIntent);
                            } else {
                                loginProgressBar.setVisibility(View.INVISIBLE);
                                Toast.makeText(Login.this, "Phone number not registered, you need to sign up", Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                }
            }
        });


    }

    /*
      ----------------------------- Firebase setup ---------------------------------
   */
    private void setupFirebaseAuth() {

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {

                    Toast.makeText(Login.this, "Welcome: " + user.getEmail(), Toast.LENGTH_SHORT).show();

                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                            .setTimestampsInSnapshotsEnabled(true)
                            .build();
                    db.setFirestoreSettings(settings);

                    DocumentReference userRef = db.collection(getString(R.string.collection_users))
                            .document(user.getUid());

                    userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                User user = task.getResult().toObject(User.class);
                                ((UserClient) (getApplicationContext())).setUser(user);
                            }
                        }
                    });

                    Intent intent = new Intent(Login.this, MapActivity.class);
                    startActivity(intent);

                }
                // ...
            }
        };
    }


}