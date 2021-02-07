package com.nessy.ecommersapp.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nessy.ecommersapp.R;

import java.util.HashMap;

public class LoginActivity extends AppCompatActivity {
    private EditText emailEt, passEt;
    private TextView forgotTv, noAccountTv;
    private Button loginBtn;

    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailEt = findViewById(R.id.emailEt);
        passEt = findViewById(R.id.passEt);
        forgotTv = findViewById(R.id.forgotTv);
        noAccountTv = findViewById(R.id.noAccountTv);
        loginBtn = findViewById(R.id.loginBtn);

        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        noAccountTv.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, RegisterUserActivity.class)));

        forgotTv.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, ForgotPassActivity.class)));

        loginBtn.setOnClickListener(v -> loginUser());
    }

    private String email,pass;

    private void loginUser() {
        email = emailEt.getText().toString().trim();
        pass = passEt.getText().toString().trim();

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            Toast.makeText(this, "Invalid email pattern..", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(pass)){
            Toast.makeText(this, "Enter password..", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.setMessage("Logging In...");
        progressDialog.show();

        firebaseAuth.signInWithEmailAndPassword(email, pass)
                .addOnSuccessListener(authResult -> makeMeOnline())
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(LoginActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void makeMeOnline() {
        progressDialog.setMessage("Checking User...");

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("online","true");

        //update value to db
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //update successfully
                        checkUserType();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed updating
                        progressDialog.dismiss();
                        Toast.makeText(LoginActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkUserType() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.orderByChild("uid").equalTo(firebaseAuth.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot: snapshot.getChildren()){
                            String accountType = ""+dataSnapshot.child("accountType").getValue();
                            if (accountType.equals("Seller")){
                                progressDialog.dismiss();

                                //user is seller
                                startActivity(new Intent(LoginActivity.this, MainSellerActivity.class));
                                finish();
                            }else {
                                progressDialog.dismiss();
                                //user is buyer
                                startActivity(new Intent(LoginActivity.this, MainUserActivity.class));
                                finish();
                            }
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}