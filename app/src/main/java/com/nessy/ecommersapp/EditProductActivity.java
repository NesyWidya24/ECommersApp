package com.nessy.ecommersapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

public class EditProductActivity extends AppCompatActivity {
    private  String productId;

    private ImageButton backBtn;
    private ImageView productIconIv;
    private EditText titleEt, descEt, quantityEt, priceEt, discountedPriceEt, discountedNoteEt;
    private Button updateProductBtn;
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

    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;

    //img picked uri
    private Uri img_uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_product);

        productId = getIntent().getStringExtra("productId");

        backBtn = findViewById(R.id.backBtn);
        productIconIv = findViewById(R.id.productIconIv);
        titleEt = findViewById(R.id.titleEt);
        descEt = findViewById(R.id.descEt);
        quantityEt = findViewById(R.id.quantityEt);
        priceEt = findViewById(R.id.priceEt);
        discountedPriceEt = findViewById(R.id.discountedPriceEt);
        discountedNoteEt = findViewById(R.id.discountedNoteEt);
        updateProductBtn = findViewById(R.id.updateProductBtn);
        discountSwitch = findViewById(R.id.discountSwitch);
        categoryTv = findViewById(R.id.categoryTv);

        discountedPriceEt.setVisibility(View.GONE);
        discountedNoteEt.setVisibility(View.GONE);

        firebaseAuth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please Wait");
        progressDialog.setCanceledOnTouchOutside(false);

        //init permission arrays
        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        discountSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                //checked, show discountPriceEt, discountNoteEt
                discountedPriceEt.setVisibility(View.VISIBLE);
                discountedNoteEt.setVisibility(View.VISIBLE);
            } else {
                //checked, show discountPriceEt, discountNoteEt
                discountedPriceEt.setVisibility(View.GONE);
                discountedNoteEt.setVisibility(View.GONE);
            }
        });

        productIconIv.setOnClickListener(v -> {
//            showImgPickDialog();
        });

        categoryTv.setOnClickListener(v -> {
//            categoryDialog();
        });
//
//        addProductBtn.setOnClickListener(v -> {
//            //flow (input data > validate data > add data to db)
//            inputData();
//        });

        backBtn.setOnClickListener(v -> onBackPressed());
    }
}