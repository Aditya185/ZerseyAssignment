package com.adityaprakash.zerseyassignment;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class SignUpActivity extends AppCompatActivity {

    private TextView alreadyAccount;
    private FirebaseAuth mAuth;
    private Button registerButton;
    private ProgressDialog progressDialog;
    private EditText emailText;
    private  EditText passwordText;
   

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        initializer();

        alreadyAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                transterToLogin();
            }
        });



        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CreateNewAccount();
            }
        });
    }

    private void CreateNewAccount() {

        String email = emailText.getText().toString();
        String password = passwordText.getText().toString();




        if(email == null || password == null){
            Toast.makeText(SignUpActivity.this,"Please enter the details:",Toast.LENGTH_SHORT).show();
        }else{

            progressDialog.setTitle("Creating Your Account");
            progressDialog.setMessage("Please Wait while we are creating your account");
            progressDialog.setCanceledOnTouchOutside(true);
            progressDialog.show();

            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {

                    if (task.isSuccessful()) {



                        String currentUserId = mAuth.getCurrentUser().getUid();

                        transferToMain();

                        Toast.makeText(SignUpActivity.this, "Account Created Successfully", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    } else {
                        String message = task.getException().toString();
                        Toast.makeText(SignUpActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                }
            });

        }



    }

    private void transferToMain() {
        Intent mainIntent = new Intent(SignUpActivity.this,MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK| Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();

    }

    private void transterToLogin() {
        Intent loginIntent = new Intent(SignUpActivity.this,LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK| Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();

    }

    private void initializer() {
        mAuth = FirebaseAuth.getInstance();
        registerButton = (Button) findViewById(R.id.signUpButton);
        alreadyAccount = (TextView) findViewById(R.id.textView2);
        progressDialog = new ProgressDialog(SignUpActivity.this);
        emailText = (EditText) findViewById(R.id.editText);
        passwordText = (EditText) findViewById(R.id.editText3);


    }
}
