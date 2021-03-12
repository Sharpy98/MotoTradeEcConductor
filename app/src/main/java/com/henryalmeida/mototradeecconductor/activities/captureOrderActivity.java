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
import com.henryalmeida.mototradeecconductor.CalificationClientActivity;
import com.henryalmeida.mototradeecconductor.R;
import com.henryalmeida.mototradeecconductor.providiers.AuthProvider;
import com.henryalmeida.mototradeecconductor.providiers.SaveImagesBookingProvider;
import com.henryalmeida.mototradeecconductor.utils.CompressorBitmapImage;
import com.henryalmeida.mototradeecconductor.utils.FileUtil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
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
    private Bitmap imageBitmap;

    private String mExtraClientId;

    static final int REQUEST_IMAGE_CAPTURE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture_order);

        imgBooking = findViewById(R.id.imageBooking);
        btnSendImage = findViewById(R.id.btn_sendImage);
        mExtraClientId = getIntent().getStringExtra("idClient");

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
                if (imageBitmap != null){
                    mProgress.setMessage("Espere un momento...");
                    mProgress.setCanceledOnTouchOutside(false);
                    mProgress.show();


                    saveImage();

                    Intent intent =  new Intent(captureOrderActivity.this, CalificationClientActivity.class);
                    intent.putExtra("idClient",mExtraClientId);
                    intent.putExtra("numDelivery","1");
                    startActivity(intent);
                    finish();
                }
                else {
                    Toast.makeText(captureOrderActivity.this, "Toma la foto de la entrega", Toast.LENGTH_SHORT).show();
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
    protected void onActivityResult(int requestCode, int resultCode,  Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {

            Bundle extras = data.getExtras();
            imageBitmap = (Bitmap) extras.get("data");
            imgBooking.setImageBitmap(imageBitmap);
        }

    }

    private void saveImage(){
        FirebaseStorage storage = FirebaseStorage.getInstance();

        String timeStamp = new SimpleDateFormat("dd-MM-yyyy_HH-mm-ss").format(new Date());

        // Creamos una referencia a la carpeta y el nombre de la imagen donde se guardara
        StorageReference mountainImagesRef = storage.getReference().child("Booking/"+timeStamp+".jpg");
        //Pasamos la imagen a un array de byte
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Bitmap bitmap = imageBitmap;
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] datas = baos.toByteArray();

        // Empezamos con la subida a Firebase
        UploadTask uploadTask = mountainImagesRef.putBytes(datas);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Toast.makeText(getBaseContext(),"Hubo un error",Toast.LENGTH_LONG);
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(getBaseContext(),"Subida con exito",Toast.LENGTH_LONG);

            }
        });
    }

}