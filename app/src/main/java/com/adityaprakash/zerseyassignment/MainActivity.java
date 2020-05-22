package com.adityaprakash.zerseyassignment;
//firebase jabber connected

import android.Manifest;

import android.content.Context;
import android.content.DialogInterface;

import android.content.Intent;
import android.content.pm.PackageManager;


import android.media.MediaScannerConnection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.hbisoft.hbrecorder.HBRecorder;
import com.hbisoft.hbrecorder.HBRecorderListener;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

import yuku.ambilwarna.AmbilWarnaDialog;

public class MainActivity extends AppCompatActivity implements HBRecorderListener {

    private PaintView paintView;
    private int defaultColor;
    private StorageReference videoRef;
    private DatabaseReference databaseReference;
    FirebaseAuth mAuth;

    private String currentUserId;
    private static final int STORAGE_PERMISSION_CODE = 1;
    //Permissions
    private static final int SCREEN_RECORD_REQUEST_CODE = 777;
    private static final int PERMISSION_REQ_ID_RECORD_AUDIO = 22;
    private static final int PERMISSION_REQ_ID_WRITE_EXTERNAL_STORAGE = PERMISSION_REQ_ID_RECORD_AUDIO + 1;
    private boolean hasPermissions = false;
    //Declare HBRecorder
    private HBRecorder hbRecorder;

    private Uri videoUri;

    //Start/Stop Button
    private Button startbtn;
    StorageReference storageReference;




    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Button button;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        createFolder();
        startbtn = findViewById(R.id.start);
        databaseReference = FirebaseDatabase.getInstance().getReference();

        paintView = findViewById(R.id.paintView);
        button = findViewById(R.id.change_color_button);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        SeekBar seekBar = findViewById(R.id.seekBar);
        final TextView textView = findViewById(R.id.current_pen_size);

        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        paintView.initialise(displayMetrics);
        //Firebase
         mAuth = FirebaseAuth.getInstance();
        currentUserId=mAuth.getUid();

      //  String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        storageReference = FirebaseStorage.getInstance().getReference();


        textView.setText("Pen size: " + seekBar.getProgress());


        button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                openColourPicker();

            }

        });

        startbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    //first check if permissions was granted
                    if ( checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, PERMISSION_REQ_ID_WRITE_EXTERNAL_STORAGE)) {
                        hasPermissions = true;
                    }
                    if (hasPermissions) {
                        //check if recording is in progress
                        //and stop it if it is
                        if (hbRecorder.isBusyRecording()) {
                            hbRecorder.stopScreenRecording();
                            startbtn.setText("Record Video");
                        }
                        //else start recording
                        else {
                            startRecordingScreen();
                        }
                    }
                } else {
                    showLongToast("This library requires API 21>");
                }
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                paintView.setStrokeWidth(seekBar.getProgress());
                textView.setText("Pen size: " + seekBar.getProgress());

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //Init HBRecorder
            hbRecorder = new HBRecorder(this, this);
            hbRecorder.setOutputPath(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES) +"/Zersey");
            hbRecorder.isAudioEnabled(false);
            hbRecorder.recordHDVideo(false);


            //When the user returns to the application, some UI changes might be necessary,
            //check if recording is in progress and make changes accordingly
            if (hbRecorder.isBusyRecording()) {
                startbtn.setText("Stop");
            }
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main, menu);

        return super.onCreateOptionsMenu(menu);

    }

    private void requestStoragePermission () {

        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

            new AlertDialog.Builder(this)
                    .setTitle("Permission needed")
                    .setMessage("Needed to save image")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            ActivityCompat.requestPermissions(MainActivity.this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);

                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            dialog.dismiss();

                        }

                    })
                    .create().show();

        } else {

            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);

        }

    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.clear_button:
                paintView.clear();
                return true;
            case R.id.logout:
                mAuth.signOut();
                Intent loginIntent = new Intent(MainActivity.this,LoginActivity.class);
                loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK| Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(loginIntent);
                finish();
                return true;
            case R.id.undo_button:
                paintView.undo();
                return true;
            case R.id.redo_button:
                paintView.redo();
                return true;
            case R.id.save_button:

                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                    requestStoragePermission();

                }
                paintView.saveImage();

                return true;

        }

        return super.onOptionsItemSelected(item);

    }

    private void openColourPicker () {

        AmbilWarnaDialog ambilWarnaDialog = new AmbilWarnaDialog(this, defaultColor, new AmbilWarnaDialog.OnAmbilWarnaListener() {

            @Override
            public void onCancel(AmbilWarnaDialog dialog) {

                Toast.makeText(MainActivity.this, "Unavailable", Toast.LENGTH_LONG).show();

            }

            @Override
            public void onOk(AmbilWarnaDialog dialog, int color) {

                defaultColor = color;

                paintView.setColor(color);

            }

        });

        ambilWarnaDialog.show(); // add

    }


    @Override
    public void HBRecorderOnComplete() {
        startbtn.setText("Record Video");
        showLongToast("Saved Successfully");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            refreshGallery();
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void refreshGallery() {
        MediaScannerConnection.scanFile(this,
                new String[]{hbRecorder.getFilePath()}, null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String path, Uri uri) {
                        Log.i("ExternalStorage", "Scanned " + path + ":");
                        Log.i("ExternalStorage", "-> uri=" + uri);
                        videoUri = uri;
                        upload();
                    }
                });
    }

    @Override
    public void HBRecorderOnError(int errorCode, String reason) {
        if (errorCode == 38) {
            showLongToast("Some settings are not supported by your device");
        } else {
            showLongToast("HBRecorderOnError - See Log");
            Log.e("HBRecorderOnError", reason);
        }

        startbtn.setText("Record Video");

    }
    //Create Folder
    private void createFolder() {
        File f1 = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES), "Zersey");
        if (!f1.exists()) {
            if (f1.mkdirs()) {
                Log.i("Folder ", "created");
            }
        }
    }

    //hbRecorder.startScreenRecording(data); should only be called in onActivityResult
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void startRecordingScreen() {


            MediaProjectionManager mediaProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
            Intent permissionIntent = mediaProjectionManager != null ? mediaProjectionManager.createScreenCaptureIntent() : null;
            startActivityForResult(permissionIntent, SCREEN_RECORD_REQUEST_CODE);
            startbtn.setText("Stop");

    }


    //Check if permissions was granted
    private boolean checkSelfPermission(String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{permission}, requestCode);
            return false;
        }
        return true;
    }

    //Handle permissions
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQ_ID_RECORD_AUDIO:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, PERMISSION_REQ_ID_WRITE_EXTERNAL_STORAGE);
                } else {
                    hasPermissions = false;
                    showLongToast("No permission for " + Manifest.permission.RECORD_AUDIO);
                }
                break;
            case PERMISSION_REQ_ID_WRITE_EXTERNAL_STORAGE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    hasPermissions = true;
                    //Permissions was provided
                    //Start screen recording
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        startRecordingScreen();
                    }
                } else {
                    hasPermissions = false;
                    showLongToast("No permission for " + Manifest.permission.WRITE_EXTERNAL_STORAGE);
                }
                break;
            case STORAGE_PERMISSION_CODE :

                 if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                     Toast.makeText(this, "Access granted", Toast.LENGTH_LONG).show();

                 } else {

                     Toast.makeText(this, "Access denied", Toast.LENGTH_LONG).show();

                 }
                 break;

            default:
                break;
        }
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (requestCode == SCREEN_RECORD_REQUEST_CODE) {
                if (resultCode == RESULT_OK) {
                    //Start screen recording
                    hbRecorder.startScreenRecording(data, resultCode, this);

                }
            }
        }
    }

    //Show Toast
    private void showLongToast(final String msg) {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
    }

    //uploading to firebase cloud

    public void upload(){

        Random random = new Random();
        final int rand_int = random.nextInt(3000);

        String uid = mAuth.getCurrentUser().getUid();

        videoRef = storageReference.child("/videos/"+uid+"/video"+rand_int);
        if(videoUri!=null){
            UploadTask uploadTask  = videoRef.putFile(videoUri);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(MainActivity.this,"Upload Failed",Toast.LENGTH_SHORT).show();
                }
            } ).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    if (taskSnapshot.getMetadata() != null) {
                        if (taskSnapshot.getMetadata().getReference() != null) {
                            Task<Uri> result = taskSnapshot.getStorage().getDownloadUrl();
                            result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String downloadUri = uri.toString();
                                    //createNewPost(imageUrl);
                                    Toast.makeText(MainActivity.this,"Upload Success",Toast.LENGTH_SHORT).show();
                                    Log.i("uri",downloadUri);
                                    HashMap<String,Object> ItemsMap = new HashMap<>();
                                    ItemsMap.put("url",downloadUri);
                                    databaseReference.child("Users").child(currentUserId).child("Videos").child("video"+rand_int).updateChildren(ItemsMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {



                                        }
                                    });
                                }
                            });
                        }
                    }

                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {

                }
            });

        }else{
            Toast.makeText(MainActivity.this,"Nothing to upload",Toast.LENGTH_SHORT).show();
        }
    }
    public void videoList(View view){
        Intent videoIntent = new Intent(MainActivity.this,VideoListView.class);
        startActivity(videoIntent);
    }
}