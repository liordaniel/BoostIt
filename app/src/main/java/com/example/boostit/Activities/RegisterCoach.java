package com.example.boostit.Activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.boostit.Objects.ObjCoach;
import com.example.boostit.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class RegisterCoach extends AppCompatActivity {

    private static final int CAMERA_REQUEST_CODE = 1;

    EditText            txtEmail, txtPassword, txtPassword2, txtFullName, txtPhoneNumber,
                        txtStudioName, txtStudioCity, txtStudioAddress;
    Button              btnLetsGo, btnTakePhoto;
    FirebaseAuth        myAuth;
    FirebaseDatabase    database;
    DatabaseReference   myRef;
    StorageReference    myStorageRef;

    ProgressDialog      mProg;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_coach);

        mProg               =   new ProgressDialog(this);

        btnTakePhoto        =   findViewById(R.id.btnTakePhoto);
        txtEmail            =   findViewById(R.id.txtEmail);
        txtPassword         =   findViewById(R.id.txtPassword);
        txtPassword2        =   findViewById(R.id.txtPassword2);
        txtFullName         =   findViewById(R.id.txtFullName);
        txtPhoneNumber      =   findViewById(R.id.txtPhoneNumber);
        txtStudioName       =   findViewById(R.id.txtStudioName);
        txtStudioCity       =   findViewById(R.id.txtStudioCity);
        txtStudioAddress    =   findViewById(R.id.txtStudioAddress);
        btnLetsGo           =   findViewById(R.id.btnLetsGo);

        myAuth              =   FirebaseAuth.getInstance();
        myStorageRef        =   FirebaseStorage.getInstance().getReference();

        btnTakePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
               startActivityForResult(intent, CAMERA_REQUEST_CODE);
            }
        });

        btnLetsGo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String    strEmail            =   txtEmail.getText().toString();
                final String    strPassword         =   txtPassword.getText().toString();
                final String    strPassword2        =   txtPassword2.getText().toString();
                final String    strFullName         =   txtFullName.getText().toString();
                final String    strPhoneNumber      =   txtPhoneNumber.getText().toString();
                final String    strStudioName       =   txtStudioName.getText().toString();
                final String    strStudioCity       =   txtStudioCity.getText().toString();
                final String    strStudioAddress    =   txtStudioAddress.getText().toString();

                if(TextUtils.isEmpty(strFullName)){
                    txtFullName.setError("full name is required");
                    return;
                }
                if(TextUtils.isEmpty(strPhoneNumber)){
                    txtPhoneNumber.setError("phone number is required");
                    return;
                }
                if(TextUtils.isEmpty(strStudioName)){
                    txtPhoneNumber.setError("studio name is required");
                    return;
                }
                if(TextUtils.isEmpty(strStudioCity)){
                    txtPhoneNumber.setError("studio city is required");
                    return;
                }
                if(TextUtils.isEmpty(strStudioAddress)){
                    txtPhoneNumber.setError("studio address is required");
                    return;
                }
                if(TextUtils.isEmpty(strEmail)){
                    txtEmail.setError("email is required");
                    return;
                }
                if(strPassword.length() < 6){
                    txtPassword.setError("password must be >= 6 characters");
                    return;
                }

                if(!strPassword.equals(strPassword2)){
                    txtPassword2.setError("password and confirm Password has to be equal");
                    return;
                }





                CreateUserAccount(strEmail, strPassword, strFullName, strPhoneNumber, strStudioName, strStudioCity, strStudioAddress);

            }
        });
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK){

            mProg.setMessage("Uploading...");
            mProg.show();

            Uri uri = data.getData();

            StorageReference filePath = myStorageRef.child("COACH PHOTOS").child(myAuth.getCurrentUser().getUid()).child(uri.getLastPathSegment());

            filePath.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    mProg.dismiss();
                    Toast.makeText(getApplicationContext(), "Photo has uploaded!", Toast.LENGTH_LONG).show();
                }
            });

        }
    }

    private void CreateUserAccount(final String strEmail,final String strPassword,final String strFullName,final String strPhoneNumber,
                                   final String strStudioName, final String strStudioCity, final String strStudioAddress) {
        myAuth.createUserWithEmailAndPassword(strEmail, strPassword).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    Toast.makeText(getApplicationContext(), "Coaches account created!", Toast.LENGTH_LONG).show();
                    ObjCoach coach = new ObjCoach(myAuth.getCurrentUser().getUid(), strEmail, strPassword, strFullName, strPhoneNumber, strStudioName, strStudioCity, strStudioAddress);
                    database = FirebaseDatabase.getInstance();
                    myRef = database.getReference().child("COACHES USERS");
                    myRef.child(myAuth.getCurrentUser().getUid()).setValue(coach); //add the coach obj to the firebase
                    myRef = database.getReference().child("CITIES");
                    myRef.child(strStudioCity).child(myAuth.getCurrentUser().getUid()).setValue(strFullName);
                    startActivity(new Intent(RegisterCoach.this, LogIn.class));
                    return;
                }
                else{
                    Toast.makeText(getApplicationContext(), "Account creation failed : " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
