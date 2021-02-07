package com.nessy.ecommersapp.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nessy.ecommersapp.R;

public class SplashActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        firebaseAuth = FirebaseAuth.getInstance();

        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(() -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user==null){
                startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                finish();
            }else {
                checkUserType();
            }
        }, 1000);
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
                                //user is seller
                                startActivity(new Intent(SplashActivity.this, MainSellerActivity.class));
                                finish();
                            }else {
                                //user is buyer
                                startActivity(new Intent(SplashActivity.this, MainUserActivity.class));
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