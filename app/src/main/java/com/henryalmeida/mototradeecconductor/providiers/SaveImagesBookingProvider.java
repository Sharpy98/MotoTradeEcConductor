package com.henryalmeida.mototradeecconductor.providiers;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.henryalmeida.mototradeecconductor.utils.CompressorBitmapImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import androidx.annotation.NonNull;

public class SaveImagesBookingProvider {
    private StorageReference mStorage;

    public SaveImagesBookingProvider(String ref) {
        //String timeStamp = new SimpleDateFormat("dd-MM-yyyy_HH-mm-ss").format(new Date()); // Guardar la fecha y hora actual
        //mStorage =  FirebaseStorage.getInstance().getReference().child("Booking/"+timeStamp+".jpg");
        mStorage =  FirebaseStorage.getInstance().getReference().child(ref);
    }
    public UploadTask saveImageBooking(Bitmap imageBitmap,String idDriver){
        //Pasamos la imagen a un array de byte /"+idDriver+name+".jpg"

        final StorageReference storage = mStorage.child(idDriver+".jpg");
        mStorage = storage;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Bitmap bitmap = imageBitmap;
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] datas = baos.toByteArray();

        // Empezamos con la subida a Firebase
        UploadTask uploadTask = mStorage.putBytes(datas);
        return uploadTask;
    }

    public StorageReference getmStorage(){
        return mStorage;
    }
}
