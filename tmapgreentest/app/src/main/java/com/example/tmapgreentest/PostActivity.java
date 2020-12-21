package com.example.tmapgreentest;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class PostActivity extends AppCompatActivity implements View.OnClickListener {

    private FirebaseAuth mAuth;
    private FirebaseFirestore mStore;

    private EditText mTitle, mContents;
    private String nicname;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        mAuth = FirebaseAuth.getInstance();
        mStore = FirebaseFirestore.getInstance();

        mTitle = findViewById(R.id.post_title_edit);
        mContents = findViewById(R.id.post_contents_edit);

        findViewById(R.id.post_save_button).setOnClickListener(this);

        if(mAuth.getCurrentUser() != null){
            mStore.collection(FIrebaseID.user).document(mAuth.getCurrentUser().getUid())
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if(task.getResult().exists()) {
                                if (task.getResult() != null) {
                                    nicname = (String) task.getResult().getData().get(FIrebaseID.nicname);
                                }
                            }
                        }
                    });
        }
    }

    @Override
    public void onClick(View view) {
        if(mAuth.getCurrentUser() != null){
            String postId = mStore.collection(FIrebaseID.post).document().getId();
            Map<String, Object> data = new HashMap<>();
            data.put(FIrebaseID.documentId, postId);
            data.put(FIrebaseID.nicname, nicname);
            data.put(FIrebaseID.title, mTitle.getText().toString());
            data.put(FIrebaseID.contents, mContents.getText().toString());
            data.put(FIrebaseID.timestamp, FieldValue.serverTimestamp());
            mStore.collection(FIrebaseID.post).document(postId).set(data, SetOptions.merge());
            finish();
        }
    }
}