package com.henryalmeida.mototradeecconductor.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.henryalmeida.mototradeecconductor.R;
import com.henryalmeida.mototradeecconductor.providiers.AuthProvider;
import com.henryalmeida.mototradeecconductor.providiers.SaveImagesBookingProvider;
import com.henryalmeida.mototradeecconductor.utils.CompressorBitmapImage;
import com.henryalmeida.mototradeecconductor.utils.FileUtil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class captureOrderActivity extends AppCompatActivity {

    private ImageView imgBooking;
    private Button btnSendImage;

    private ProgressDialog mProgress;

    private SaveImagesBookingProvider mSaveImageProvider;
    private AuthProvider mAuthProvider;

    private File mImageFile;

    static final int REQUEST_IMAGE_CAPTURE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture_order);

        imgBooking = findViewById(R.id.imageBooking);
        btnSendImage = findViewById(R.id.btn_sendImage);

        mAuthProvider = new AuthProvider();
        mSaveImageProvider = new SaveImagesBookingProvider("booking_images");

        mProgress = new ProgressDialog(this);

        imgBooking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCamera();
            }
        });

        btnSendImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mImageFile != null){
                    mProgress.setMessage("Espere un momento...");
                    mProgress.setCanceledOnTouchOutside(false);
                    mProgress.show();

                    // Guardar en Firebase
                    saveImage();
                }
                else {
                    Toast.makeText(captureOrderActivity.this, "Ingresa la imagen y el número de celular", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private void openCamera(){
        // Esto es para poder utilizar la camara
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            try {
                mImageFile = FileUtil.from(this,data.getData());
                imgBooking.setImageBitmap(BitmapFactory.decodeFile(mImageFile.getAbsolutePath()));
            }catch (Exception e){
                Log.d("ERROR","Mensaje: " + e.getMessage());
            }
        }

    }

    private void saveImage(){
        mSaveImageProvider.saveImage(captureOrderActivity.this,mImageFile,mAuthProvider.getId()).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()){
                    mProgress.dismiss(); // Ocultar el Proegess cuando ya se suba la imagen
                    Toast.makeText(captureOrderActivity.this, "La foto se guardo correctamente", Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(captureOrderActivity.this, "Hubo un error al subir la imagen", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}