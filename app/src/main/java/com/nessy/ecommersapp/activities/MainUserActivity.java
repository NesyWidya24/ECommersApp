package com.nessy.ecommersapp.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nessy.ecommersapp.R;

import java.util.HashMap;

public class MainUserActivity extends AppCompatActivity {

    private TextView nameTv, emailTv, phoneTv, tabShopsTv, tabOrdersTv;
    private ImageButton logoutBtn, editProfileBtn;
    private ImageView profileIv;
    private RelativeLayout shopsRl, ordersRl;

    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_user);

        nameTv = findViewById(R.id.nameTv);
        logoutBtn = findViewById(R.id.logoutBtn);
        editProfileBtn = findViewById(R.id.editProfileBtn);
        emailTv = findViewById(R.id.emailTv);
        phoneTv = findViewById(R.id.phoneTv);
        tabShopsTv = findViewById(R.id.tabShopsTv);
        tabOrdersTv = findViewById(R.id.tabOrdersTv);
        profileIv = findViewById(R.id.profileIv);
        shopsRl = findViewById(R.id.shopsRl);
        ordersRl = findViewById(R.id.ordersRl);

        firebaseAuth = FirebaseAuth.getInstance();
        checkUser();

//        at start show shops ui
        showShopsUI();

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        logoutBtn.setOnClickListener(v -> {
            //make offline > sign out > go to login activity
            makeMeOffline();
        });

        editProfileBtn.setOnClickListener(v -> {
            startActivity(new Intent(MainUserActivity.this, ProfileEditUserActivity.class));
            finish();
        });

        tabShopsTv.setOnClickListener(v -> {
//            show shops
            showShopsUI();
        });
        tabOrdersTv.setOnClickListener(v -> {
//            show orders
            showOrdersUI();
        });
    }

    private void showShopsUI() {
        //show shops ui and hide orders ui
        shopsRl.setVisibility(View.VISIBLE);
        ordersRl.setVisibility(View.GONE);

        tabShopsTv.setTextColor(getBaseContext().getColor(R.color.black));
        tabShopsTv.setBackgroundResource(R.drawable.shape_rect04);

        tabOrdersTv.setTextColor(getBaseContext().getColor(R.color.white));
        tabOrdersTv.setBackgroundColor(getBaseContext().getColor(android.R.color.transparent));

    }

    private void showOrdersUI() {
        //show orders ui and hide products ui
        ordersRl.setVisibility(View.VISIBLE);
        shopsRl.setVisibility(View.GONE);

        tabOrdersTv.setTextColor(getBaseContext().getColor(R.color.black));
        tabOrdersTv.setBackgroundResource(R.drawable.shape_rect04);

        tabShopsTv.setTextColor(getBaseContext().getColor(R.color.white));
        tabShopsTv.setBackgroundColor(getBaseContext().getColor(android.R.color.transparent));
    }

    private void makeMeOffline() {
        progressDialog.setMessage("Logging out...");

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("online", "false");

        //update value to db
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).updateChildren(hashMap)
                .addOnSuccessListener(aVoid -> {
                    //update successfully
                    firebaseAuth.signOut();
                    checkUser();
                })
                .addOnFailureListener(e -> {
                    //failed updating
                    progressDialog.dismiss();
                    Toast.makeText(MainUserActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void checkUser() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null) {
            startActivity(new Intent(MainUserActivity.this, LoginActivity.class));
            finish();
        } else {
            loadMyInfo();
        }
    }

    private void loadMyInfo() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.orderByChild("uid").equalTo(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            String name = "" + dataSnapshot.child("name").getValue();
                            String accountType = "" + dataSnapshot.child("accountType").getValue();

                            nameTv.setText(name);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}