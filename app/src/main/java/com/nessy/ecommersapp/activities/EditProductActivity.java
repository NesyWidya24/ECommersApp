package com.nessy.ecommersapp.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.nessy.ecommersapp.Constants;
import com.nessy.ecommersapp.R;

import java.util.HashMap;

public class EditProductActivity extends AppCompatActivity {
    private String productId;

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
        loadProductDetails();

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
            showImgPickDialog();
        });

        categoryTv.setOnClickListener(v -> {
            categoryDialog();
        });
//
        updateProductBtn.setOnClickListener(v -> {
            //flow (input data > validate data > update data to db)
            inputData();
        });

        backBtn.setOnClickListener(v -> onBackPressed());
    }

    private void loadProductDetails() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("Products").child(productId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String productId = "" + snapshot.child("productId").getValue();
                        String productTitle = "" + snapshot.child("productTitle").getValue();
                        String productDesc = "" + snapshot.child("productDesc").getValue();
                        String productCategory = "" + snapshot.child("productCategory").getValue();
                        String productQuantity = "" + snapshot.child("productQuantity").getValue();
                        String productIcon = "" + snapshot.child("productIcon").getValue();
                        String originalPrice = "" + snapshot.child("originalPrice").getValue();
                        String discountPrice = "" + snapshot.child("discountPrice").getValue();
                        String discountNote = "" + snapshot.child("discountNote").getValue();
                        String discountAvailable = "" + snapshot.child("discountAvailable").getValue();
                        String timestamp = "" + snapshot.child("timestamp").getValue();
                        String uid = "" + snapshot.child("uid").getValue();

                        //set data to viewss
                        if (discountAvailable.equals("true")) {
                            discountSwitch.setChecked(true);

                            discountedPriceEt.setVisibility(View.VISIBLE);
                            discountedNoteEt.setVisibility(View.VISIBLE);
                        } else {
                            discountSwitch.setChecked(false);

                            discountedPriceEt.setVisibility(View.GONE);
                            discountedNoteEt.setVisibility(View.GONE);
                        }

                        titleEt.setText(productTitle);
                        descEt.setText(productDesc);
                        categoryTv.setText(discountNote);
                        quantityEt.setText(productQuantity);
                        priceEt.setText(originalPrice);
                        discountedPriceEt.setText(discountPrice);

                        try {
                            Glide.with(getApplicationContext()).load(productIcon).placeholder(R.drawable.ic_add_shipping_white).into(productIconIv);
                        } catch (Exception e) {
                            productIconIv.setImageResource(R.drawable.ic_add_shipping_white);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }


    private String productTitle, productDesc, productCategory, productQuantity, originalPrice, discountPrice, discountNote;
    private boolean discountAvailable = false;

    private void inputData() {
        productTitle = titleEt.getText().toString().trim();
        productDesc = descEt.getText().toString().trim();
        productCategory = categoryTv.getText().toString().trim();
        productQuantity = quantityEt.getText().toString().trim();
        originalPrice = priceEt.getText().toString().trim();
        discountAvailable = discountSwitch.isChecked();

        if (TextUtils.isEmpty(productTitle)) {
            Toast.makeText(this, "Title is required..", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(productCategory)) {
            Toast.makeText(this, "Category is required..", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(originalPrice)) {
            Toast.makeText(this, "Price is required..", Toast.LENGTH_SHORT).show();
            return;
        }

        if (discountAvailable) {
            discountPrice = discountedPriceEt.getText().toString().trim();
            discountNote = discountedNoteEt.getText().toString().trim();

            if (TextUtils.isEmpty(discountPrice)) {
                Toast.makeText(this, "Discount Price is required..", Toast.LENGTH_SHORT).show();
                return;
            }
        } else {
            discountPrice = "0";
            discountNote = "";
        }
        updateProduct();
    }

    private void updateProduct() { //add timestamp for date update
        //show progress
        progressDialog.setMessage("Updating product...");
        progressDialog.show();

        if (img_uri == null) {
            //update without img

            //setup data in hashmap to update
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("productTitle", "" + productTitle);
            hashMap.put("productDesc", "" + productDesc);
            hashMap.put("productCategory", "" + productCategory);
            hashMap.put("productQuantity", "" + productQuantity);
            hashMap.put("originalPrice", "" + originalPrice);
            hashMap.put("discountPrice", "" + discountPrice);
            hashMap.put("discountNote", "" + discountNote);
            hashMap.put("discountAvailable", "" + discountAvailable);

            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
            reference.child(firebaseAuth.getUid()).child("Products").child(productId)
                    .updateChildren(hashMap)
                    .addOnSuccessListener(aVoid -> {
                        progressDialog.dismiss();
                        Toast.makeText(EditProductActivity.this, "Updated...", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        progressDialog.dismiss();
                        Toast.makeText(EditProductActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            //update with img
            //1. upload img > img name and path on firebase storage
            String filePathAndName = "product_images/" + "" + productId;
            //upload img
            StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePathAndName);
            storageReference.putFile(img_uri)
                    .addOnSuccessListener(taskSnapshot -> {
                        //img uploaded, get url of uploaded img
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful()) ;
                        Uri downloadImgUri = uriTask.getResult();

                        if (uriTask.isSuccessful()) {
                            //setup data in hashmap to update
                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("productTitle", "" + productTitle);
                            hashMap.put("productDesc", "" + productDesc);
                            hashMap.put("productCategory", "" + productCategory);
                            hashMap.put("productIcon", "" + downloadImgUri);
                            hashMap.put("productQuantity", "" + productQuantity);
                            hashMap.put("originalPrice", "" + originalPrice);
                            hashMap.put("discountPrice", "" + discountPrice);
                            hashMap.put("discountNote", "" + discountNote);
                            hashMap.put("discountAvailable", "" + discountAvailable);

                            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
                            reference.child(firebaseAuth.getUid()).child("Products").child(productId)
                                    .updateChildren(hashMap)
                                    .addOnSuccessListener(aVoid -> {
                                        progressDialog.dismiss();
                                        Toast.makeText(EditProductActivity.this, "Updated...", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> {
                                        progressDialog.dismiss();
                                        Toast.makeText(EditProductActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        }
                    })
                    .addOnFailureListener(e -> {
                        progressDialog.dismiss();
                        Toast.makeText(EditProductActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void categoryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Product Category")
                .setItems(Constants.productCategories, (dialog, which) -> {
                    //get picked category
                    String category = Constants.productCategories[which];

                    //set picked category
                    categoryTv.setText(category);
                }).show();
    }

    private void showImgPickDialog() {
        //option to display in dialog
        String[] options = {"Camera", "Gallery"};
        //dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick Image")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        if (checkCameraPermission()) {
                            pickFromCamera();
                        } else {
                            requestCameraPermission();
                        }
                    } else {
                        if (checkStoragePermission()) {
                            pickFromGallery();
                        } else {
                            requestStoragePermission();
                        }
                    }
                }).show();
    }

    private void pickFromGallery() {
        //intent to pick img from gallery
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMG_PICK_GALLERY_CODE);
    }

    private void pickFromCamera() {
        //intent to pick img from camera, using media store to pick/ori quality img
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE, "Temp_Image_Title");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "Temp_Image_Description");

        img_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, img_uri);
        startActivityForResult(intent, IMG_PICK_CAMERA_CODE);
    }

    private boolean checkStoragePermission() {
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                (PackageManager.PERMISSION_GRANTED);

        return result;
    }

    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this, storagePermissions, STORAGE_REQUEST_CODE);
    }

    private boolean checkCameraPermission() {
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
                (PackageManager.PERMISSION_GRANTED);

        boolean result1 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                (PackageManager.PERMISSION_GRANTED);

        return result && result1;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, cameraPermissions, CAMERA_REQUEST_CODE);
    }

    //handle permission result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case CAMERA_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && storageAccepted) {
                        pickFromCamera();
                    } else {
                        Toast.makeText(this, "Camera & storage permissions are required...", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            case STORAGE_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (storageAccepted) {
                        pickFromGallery();
                    } else {
                        Toast.makeText(this, "Storage permission is required...", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    //handle img pick results
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == IMG_PICK_GALLERY_CODE) {
                img_uri = data.getData();
                productIconIv.setImageURI(img_uri);
            } else if (requestCode == IMG_PICK_CAMERA_CODE) {
                productIconIv.setImageURI(img_uri);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

}