package com.adityaprakash.zerseyassignment;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class VideoListView extends AppCompatActivity {

    RecyclerView recyclerView;
    VideoAdapter videoAdapter;

    ArrayList<DataSetList> arrayList;

    DataSetList dataSetList = new DataSetList("https://firebasestorage.googleapis.com/v0/b/jabber-b1aa7.appspot.com/o/videos%2FB5BPlXF0w5NUBoneE9GN3Zu4Oer1%2Fvideo1498?alt=media&token=31ca9b0e-4835-40be-8406-41f67da24fe3");



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_list_view);
        recyclerView = findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        arrayList = new ArrayList<DataSetList>();
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId).child("Videos")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            URLS urlID = snapshot.getValue(URLS.class);
                            dataSetList = new DataSetList(urlID.getUrl());
                            arrayList.add(dataSetList);
                            videoAdapter.notifyDataSetChanged();

                            Log.d("URL",dataSetList.getLink());

                            //System.out.println(urlID.getUrl());
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });

//        DataSetList dataSetList = new DataSetList("https://firebasestorage.googleapis.com/v0/b/jabber-b1aa7.appspot.com/o/videos%2FB5BPlXF0w5NUBoneE9GN3Zu4Oer1%2Fvideo1498?alt=media&token=31ca9b0e-4835-40be-8406-41f67da24fe3");
//        arrayList.add(dataSetList);



        videoAdapter = new VideoAdapter(arrayList,getApplicationContext());
        recyclerView.setAdapter(videoAdapter);

    }
}
