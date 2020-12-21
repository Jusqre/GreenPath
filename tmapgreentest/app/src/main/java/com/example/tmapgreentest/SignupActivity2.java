package com.example.tmapgreentest;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class SignupActivity2 extends AppCompatActivity implements View.OnClickListener {

    private EditText mEmailText, mPasswordText, mNicname;
    private FirebaseAuth mAuth;
    private FirebaseFirestore mStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup2);

        mAuth = FirebaseAuth.getInstance();
        mStore = FirebaseFirestore.getInstance();

        mEmailText = findViewById(R.id.sign_email);
        mPasswordText = findViewById(R.id.sign_password);
        mNicname = findViewById(R.id.sign_nicname);

        findViewById(R.id.sign_success).setOnClickListener(this);


    }

    @Override
    public void onClick(View view) {
        mAuth.createUserWithEmailAndPassword(mEmailText.getText().toString(), mPasswordText.getText().toString())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if(user!=null) {
                                Map<String, Object> userMap = new HashMap<>();
                                userMap.put(FIrebaseID.documentId, user.getUid());
                                userMap.put(FIrebaseID.nicname, mNicname.getText().toString());
                                userMap.put(FIrebaseID.email,mEmailText.getText().toString());
                                userMap.put(FIrebaseID.password,mPasswordText.getText().toString());
                                mStore.collection(FIrebaseID.user).document(user.getUid()).set(userMap, SetOptions.merge());
                                finish();
                            }
                        } else {
                            Toast.makeText(SignupActivity2.this, "Sign Up Failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });


    }
}