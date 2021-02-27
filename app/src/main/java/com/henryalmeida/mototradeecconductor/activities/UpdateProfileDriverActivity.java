package com.henryalmeida.mototradeecconductor.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import de.hdodenhof.circleimageview.CircleImageView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.henryalmeida.mototradeecconductor.R;
import com.henryalmeida.mototradeecconductor.includes.MyToolbar;
import com.henryalmeida.mototradeecconductor.models.Drivers;
import com.henryalmeida.mototradeecconductor.providiers.AuthProvider;
import com.henryalmeida.mototradeecconductor.providiers.ClientProvider;
import com.henryalmeida.mototradeecconductor.providiers.DriverProvider;
import com.henryalmeida.mototradeecconductor.providiers.ImageProvider;
import com.henryalmeida.mototradeecconductor.utils.CompressorBitmapImage;
import com.henryalmeida.mototradeecconductor.utils.FileUtil;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.sql.Driver;

public class UpdateProfileDriverActivity extends AppCompatActivity {

    private ImageView mImageViewProfile;
    private Button btnUpdate;
    private TextView tvName;
    private TextView tvBrand;
    private TextView tvPlate;

    private DriverProvider mDriverProvider;
    private AuthProvider mAuthProvider;
    private ImageProvider mImagePRovider;

    private File mImageFile;
    private String mImage; // Para almacenar el url de firebase

    private final int GALLERY_REQUEST = 1;

    private ProgressDialog mProgress;
    private String mBrand;
    private String mPlate;

    private CircleImageView mCircleImageBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile_driver);

        mImageViewProfile = findViewById(R.id.imageViewProfile);
        btnUpdate = findViewById(R.id.btnUpdateProfile);
        tvName = findViewById(R.id.textInputName);
        tvBrand = findViewById(R.id.textInputBrand);
        tvPlate = findViewById(R.id.textInputPlate);
        mCircleImageBack= findViewById(R.id.circleImageBack);


        mDriverProvider = new DriverProvider();
        mAuthProvider = new AuthProvider();
        mImagePRovider = new ImageProvider("driver_images");

        mProgress = new ProgressDialog(this);

        getDriverInfo();

        mImageViewProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();

            }
        });

        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateProfie();
            }
        });

        mCircleImageBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UpdateProfileDriverActivity.this,MapDriver.class);
                startActivity(intent);
                finish();
            }
        });

    }

    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/");
        startActivityForResult(galleryIntent,GALLERY_REQUEST);
    }

    // Para saber si el usuario selecciono un aimagen de la galeria
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GALLERY_REQUEST && resultCode == RESULT_OK){
            try {
                mImageFile = FileUtil.from(this,data.getData());
                mImageViewProfile.setImageBitmap(BitmapFactory.decodeFile(mImageFile.getAbsolutePath()));
            }catch (Exception e){
                Log.d("ERROR","Mensaje: " + e.getMessage());
            }
        }

    }

    // Para traer el nombre del cliente
    private void getDriverInfo(){
        mDriverProvider.getDriver(mAuthProvider.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    String name = snapshot.child("name").getValue().toString();
                    String brand = snapshot.child("VehBrand").getValue().toString();
                    String plate = snapshot.child("VehPlate").getValue().toString();

                    String image = "";
                    if (snapshot.hasChild("image")){
                        image = snapshot.child("image").getValue().toString();
                        Picasso.with(UpdateProfileDriverActivity.this).load(image).into(mImageViewProfile);
                    }

                    tvName.setText(name);
                    tvBrand.setText(brand);
                    tvPlate.setText(plate);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void updateProfie() {
        // String name = tvName.getText().toString();
        mBrand = tvBrand.getText().toString();
        mPlate = tvPlate.getText().toString();

        if (!mBrand.equals("") && !mPlate.equals("") && mImageFile != null){
            mProgress.setMessage("Espere un momento...");
            mProgress.setCanceledOnTouchOutside(false);
            mProgress.show();

            // Guardar en Firebase
            saveImage();
        }
        else {
            Toast.makeText(this, "Ingresa la imagen y los demas campos", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveImage() {
        mImagePRovider.saveImage(UpdateProfileDriverActivity.this,mImageFile,mAuthProvider.getId()).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                // Para verificar si la tarea fue exitosa es decir que se subio la imagen en firebase
                if (task.isSuccessful()){
                    // Obtener el link para poder mostrar la imagen
                    mImagePRovider.getmStorage().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String image = uri.toString();
                            Drivers driver = new Drivers();
                            driver.setImage(image);
                            driver.setId(mAuthProvider.getId());
                            driver.setVehiculoBrand(mBrand);
                            driver.setVehiculePlate(mPlate);
                            mDriverProvider.update(driver).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    mProgress.dismiss(); // Ocultar el Proegess cuando ya se suba la imagen
                                    Toast.makeText(UpdateProfileDriverActivity.this, "Su información se actualizo correctamente", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    });
                }
                else{
                    Toast.makeText(UpdateProfileDriverActivity.this, "Hubo un error al subir la imagen", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}