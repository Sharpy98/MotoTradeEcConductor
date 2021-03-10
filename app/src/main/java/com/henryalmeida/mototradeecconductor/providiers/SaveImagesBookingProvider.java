package com.henryalmeida.mototradeecconductor.providiers;

import android.content.Context;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.henryalmeida.mototradeecconductor.utils.CompressorBitmapImage;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SaveImagesBookingProvider {
    private StorageReference mStorage;

    public SaveImagesBookingProvider(String ref) {
// Creamos una referencia a la carpeta y el nombre de la imagen donde se guardara
        mStorage = FirebaseStorage.getInstance().getReference().child(ref);
    }
    public UploadTask saveImage(Context context, File image, String idUser){
        String timeStamp = new SimpleDateFormat("dd-MM-yyyy_HH-mm-ss").format(new Date());
        byte[] imageByte = CompressorBitmapImage.getImage(context,image.getPath(),500,500);
        // Vamos a crear una carpeta para guardar con ese nombre en firebase
        final StorageReference storage = mStorage.child(idUser+"/"+timeStamp+".jpg");
        mStorage = storage;
        UploadTask uploadTask = storage.putBytes(imageByte);
        return uploadTask;
    }

    public StorageReference getmStorage(){
        return mStorage;
    }
}
