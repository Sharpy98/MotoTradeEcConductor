package com.henryalmeida.mototradeecconductor.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.henryalmeida.mototradeecconductor.R;
import com.henryalmeida.mototradeecconductor.models.Client;
import com.henryalmeida.mototradeecconductor.models.Drivers;
import com.henryalmeida.mototradeecconductor.providiers.AuthProvider;
import com.henryalmeida.mototradeecconductor.providiers.ClientProvider;
import com.henryalmeida.mototradeecconductor.providiers.DriverProvider;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import dmax.dialog.SpotsDialog;

public class CreateAcount extends AppCompatActivity {

    DriverProvider mDriverProvider;
    AuthProvider mAuthProvider;
    android.app.AlertDialog mDialog;

    //VIEWS
    Button bCreateCount;
    EditText etName;
    EditText etPhone;
    EditText etVehiculo;
    EditText etPlate;
    EditText etEmail;
    EditText etPassword1;
    EditText getEtPassword2;

    String name,phone,email,vehiculo,plate,password1,password2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_acount);

        mDriverProvider = new DriverProvider();
        mAuthProvider = new AuthProvider();

        mDialog = new SpotsDialog.Builder().setContext(CreateAcount.this).setMessage("Espere un momento").build();

        //Asociamos los datos con los componentes
        etName = (EditText)findViewById(R.id.et_Name);
        etPhone = (EditText)findViewById(R.id.et_Phone);
        etVehiculo = (EditText)findViewById(R.id.et_Vehbrand);
        etPlate = (EditText)findViewById(R.id.et_Vehplate);
        etEmail = (EditText)findViewById(R.id.et_Email);
        etPassword1 = (EditText)findViewById(R.id.et_Password1);
        getEtPassword2 = (EditText)findViewById(R.id.et_Password2);
        bCreateCount = (Button)findViewById(R.id.btn_CreateCount);

        bCreateCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickRegister();
            }
        });
    }

    void clickRegister(){
        name = etName.getText().toString();
        phone = etPhone.getText().toString();
        vehiculo = etVehiculo.getText().toString();
        plate = etPlate.getText().toString();
        email = etEmail.getText().toString();
        password1 = etPassword1.getText().toString();
        password2 = getEtPassword2.getText().toString();

        if (!name.isEmpty()  && !phone.isEmpty() && !vehiculo.isEmpty() && !plate.isEmpty() && !email.isEmpty() && !password1.isEmpty() && !password2.isEmpty()){
            if (password1.length()>=6 && password2.length()>=6){
                if (password1.equals(password2)){
                    mDialog.show();
                    register(name,phone,vehiculo,plate,email,password1);
                }
                else {
                    Toast.makeText(CreateAcount.this,"Las contraseñas no coinciden",Toast.LENGTH_SHORT).show();
                }
            }
            else {
                Toast.makeText(CreateAcount.this,"La contraseña debe tener al menos 6 caracteres",Toast.LENGTH_SHORT).show();
            }
        }
        else {
            Toast.makeText(CreateAcount.this,"Debe completar todos los campos ",Toast.LENGTH_SHORT).show();
        }
    }

    void register(String name,String phone, String vehiculo,String plate,String email,String password){
        mAuthProvider.register(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                mDialog.hide();
                if(task.isSuccessful()){
                    String id = FirebaseAuth.getInstance().getUid();
                    Drivers driver = new Drivers(id, name, phone, email, password,vehiculo,plate);
                    create(driver);
                }else {
                    Toast.makeText(CreateAcount.this,"No se pudo auntentificar",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    void create(Drivers drivers){
        mDriverProvider.create(drivers).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(CreateAcount.this,"El registro se realizo exitosamente ",Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(CreateAcount.this,MapDriver.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
                else {
                    Toast.makeText(CreateAcount.this,"No se pudo resgistrar el usuario ",Toast.LENGTH_SHORT).show();

                }
            }
        });

    }
    /*private void saveUser(String id){
        mAuth.createUserWithEmailAndPassword(email,password1).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){

                    Map<String, Object> map = new HashMap<>();
                    map.put("name",name);
                    map.put("phone",phone);
                    map.put("email",email);
                    map.put("password",password1);

                    //String id = mAuth.getCurrentUser().getUid();

                    mDatabase.child("Users").child("Drivers").child(id).setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task2) {
                            if(task2.isSuccessful()){
                                Toast.makeText(CreateAcount.this,"Registro exitoso",Toast.LENGTH_SHORT).show();
                                Intent pasar = new Intent(CreateAcount.this, Login.class);
                                startActivity(pasar);
                                finish();
                            }else{
                                Toast.makeText(CreateAcount.this,"Fallo el registro",Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }else {
                    Toast.makeText(CreateAcount.this,"No se pudo auntentificar",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
*/
    public void pass_Login (View view){
        Intent pass = new Intent(CreateAcount.this,Login.class);
        startActivity(pass);
    }
}