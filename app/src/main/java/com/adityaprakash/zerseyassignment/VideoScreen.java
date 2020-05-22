package com.adityaprakash.zerseyassignment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.ArrayList;

public class VideoScreen extends AppCompatActivity {

   private Uri videoUri;
   private static final int REQUEST_CODE = 101;
   private StorageReference videoRef;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_view);
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        videoRef = storageReference.child("/videos/"+uid+"/user.3gp");
    }

    public void upload(View view){
        if(videoUri!=null){
            UploadTask uploadTask  = videoRef.putFile(videoUri);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(VideoScreen.this,"Upload Failed",Toast.LENGTH_SHORT).show();
                }
            } ).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Toast.makeText(VideoScreen.this,"Upload Failed",Toast.LENGTH_SHORT).show();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    updateProgress(taskSnapshot);
                }
            });

        }else{
            Toast.makeText(VideoScreen.this,"Nothing to upload",Toast.LENGTH_SHORT).show();
        }
    }

    private void updateProgress(UploadTask.TaskSnapshot taskSnapshot) {

         long  fileSize = taskSnapshot.getTotalByteCount();
         long uploadByte = taskSnapshot.getBytesTransferred();
         long progress = (100 * uploadByte)/fileSize;
         ProgressBar progressBar = findViewById(R.id.pbar);
         progressBar.setProgress((int)progress);
    }
  public void download(View view){
        try{
            final File localFile = File.createTempFile("user","3gp");
            videoRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    Toast.makeText(VideoScreen.this,"Download Complete",Toast.LENGTH_SHORT).show();
                    final VideoView videoView = (VideoView) findViewById(R.id.videoView);
                    videoView.setVideoURI(Uri.fromFile(localFile));
                    videoView.start();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(VideoScreen.this,"Download Failed",Toast.LENGTH_SHORT).show();

                }
            });

        }catch (Exception e){
            e.printStackTrace();
        }
  }
}
