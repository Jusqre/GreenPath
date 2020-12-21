package com.example.tmapgreentest;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tmapgreentest.adapters.PostAdapter;
import com.example.tmapgreentest.models.Post;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CommunityActivity extends AppCompatActivity implements View.OnClickListener, RecyclerViewItemClickListener.OnItemClickListener {

    private FirebaseFirestore mStore;

    private RecyclerView mPostRecyclerView;

    private PostAdapter mAdapter;
    private List<Post> mDatas;

    private BottomNavigationView bottomNavigationView;
    private Context mContext = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_community);

        mStore = FirebaseFirestore.getInstance();

        mPostRecyclerView = findViewById(R.id.main_recyclerview);


        findViewById(R.id.main_post_edit).setOnClickListener(this);

        mPostRecyclerView.addOnItemTouchListener(new RecyclerViewItemClickListener(this, mPostRecyclerView, this));
        
    }

    @Override
    protected void onStart() {
        super.onStart();
        mDatas = new ArrayList<>();
        mStore.collection(FIrebaseID.post)
                .orderBy(FIrebaseID.timestamp, Query.Direction.DESCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException error) {
                        if(queryDocumentSnapshots != null){
                            mDatas.clear();
                            for(DocumentSnapshot snap : queryDocumentSnapshots.getDocuments()){
                                Map<String, Object> shot = snap.getData();
                                String documentId = String.valueOf(shot.get(FIrebaseID.documentId));
                                String nicname = String.valueOf(shot.get(FIrebaseID.nicname));
                                String title = String.valueOf(shot.get(FIrebaseID.title));
                                String contents = String.valueOf(shot.get(FIrebaseID.contents));
                                Post data = new Post(documentId, nicname, title, contents);
                                mDatas.add(data);
                            }
                            mAdapter = new PostAdapter(mDatas);
                            mPostRecyclerView.setAdapter(mAdapter);
                        }
                    }
                });
    }

    @Override
    public void onClick(View view) {
        startActivity(new Intent(this, PostActivity.class));

    }

    @Override
    public void onItemClick(View view, int position) {
        Intent intent = new Intent(this,Post2Activity.class);
        intent.putExtra(FIrebaseID.documentId,mDatas.get(position).getDocumentId());
        startActivity(intent);

    }

    @Override
    public void onItemLongClick(View view, int position) {

    }
}