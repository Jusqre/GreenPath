package com.example.tmapgreentest;

import android.content.Intent;
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

// LoginActivity
public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText mEmail, mPassword;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        mEmail = findViewById(R.id.login_email);
        mPassword = findViewById(R.id.login_password);

        findViewById(R.id.login_signup).setOnClickListener(this);
        findViewById(R.id.login_success).setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = null;
        if(user != null){
            Toast.makeText(this, "자동 로그인 : "+user.getUid(), Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, CommunityActivity.class));
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.login_signup:
                startActivity(new Intent(this, SignupActivity2.class));
                break;

            case R.id.login_success:
                mAuth.signInWithEmailAndPassword(mEmail.getText().toString(), mPassword.getText().toString())
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    if(user!=null) {
                                        //Toast.makeText(MainActivity.this, "Login Success : " + user.getUid(), Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                    }
                                } else {
                                    Toast.makeText(LoginActivity.this, "Login Failed.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                break;
        }
    }
}