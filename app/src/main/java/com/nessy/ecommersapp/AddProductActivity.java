package com.nessy.ecommersapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class AddProductActivity extends AppCompatActivity {

    private ImageButton backBtn;
    private ImageView productIconIv;
    private EditText titleEt, descEt, quantityEt, priceEt, discountedPriceEt, discountedNoteEt;
    private Button addProductBtn;
    private SwitchCompat discountSwitch;
    private TextView categoryTv;

    //permission const
    private static final int CAMERA_REQUEST_CODE = 200;
    private static final int STORAGE_REQUEST_CODE = 300;

    //img pick const
    private static final int IMG_PICK_GALLERY_CODE = 400;
    private static final int IMG_PICK_CAMERA_CODE = 500;

    //permission array
    private String[] cameraPermissions;
    private String[] storagePermissions;

    //img picked uri
    private Uri img_uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);

        backBtn = findViewById(R.id.backBtn);
        productIconIv = findViewById(R.id.productIconIv);
        titleEt = findViewById(R.id.titleEt);
        descEt = findViewById(R.id.descEt);
        quantityEt = findViewById(R.id.quantityEt);
        priceEt = findViewById(R.id.priceEt);
        discountedPriceEt = findViewById(R.id.discountedPriceEt);
        discountedNoteEt = findViewById(R.id.discountedNoteEt);
        addProductBtn = findViewById(R.id.addProductBtn);
        discountSwitch = findViewById(R.id.discountSwitch);
        categoryTv = findViewById(R.id.categoryTv);

        //init permission arrays
        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        productIconIv.setOnClickListener(v -> {
            showImgPickDialog();
        });

        addProductBtn.setOnClickListener(v -> {

        });

    }

    private void showImgPickDialog() {

    }

    private void pickFromGallery(){
        //intent to pick img from gallery
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMG_PICK_GALLERY_CODE);
    }
}