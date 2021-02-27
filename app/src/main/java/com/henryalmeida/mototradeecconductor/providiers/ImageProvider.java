package com.henryalmeida.mototradeecconductor.providiers;

import android.content.Context;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.henryalmeida.mototradeecconductor.utils.CompressorBitmapImage;

import java.io.File;

public class ImageProvider {

    private StorageReference mStorage;

    public ImageProvider(String ref) {
        mStorage = FirebaseStorage.getInstance().getReference().child(ref);
    }

    public UploadTask saveImage(Context context, File image, String idUser){
        byte[] imageByte = CompressorBitmapImage.getImage(context,image.getPath(),500,500);
        // Vamos a crear una carpeta para guardar con ese nombre en firebase
        final StorageReference storage = mStorage.child(idUser+".jpg");
        mStorage = storage;
        UploadTask uploadTask = storage.putBytes(imageByte);
        return uploadTask;
    }

    public StorageReference getmStorage(){
        return mStorage;
    }
}
