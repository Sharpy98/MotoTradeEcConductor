package com.henryalmeida.mototradeecconductor.activities;

import android.content.DialogInterface;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.henryalmeida.mototradeecconductor.R;
import com.henryalmeida.mototradeecconductor.includes.MyToolbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import de.hdodenhof.circleimageview.CircleImageView;
import dmax.dialog.SpotsDialog;

public class Login extends AppCompatActivity {

    EditText eEmail,ePassword;
    Button bLogin;
    private CircleImageView mCircleImageBack;


    private FirebaseAuth mAut;
    private DatabaseReference mDatabase; ;

    android.app.AlertDialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Modulo de Firebase
        mAut = FirebaseAuth.getInstance();

       mDatabase = FirebaseDatabase.getInstance().getReference();

        eEmail = (EditText)findViewById(R.id.et_email);
        ePassword = (EditText)findViewById(R.id.et_Password);

        bLogin=(Button)findViewById(R.id.btn_Login);

        mCircleImageBack= findViewById(R.id.circleImageBack);


        //Barra de progreso
        mDialog = new SpotsDialog.Builder().setContext(Login.this).setMessage("Espere un momento").build();


        mCircleImageBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Login.this,MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        //Click en boton entrar
        bLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });

    }

    public void pass_CreateAcount (View view){
        Intent pass = new Intent(Login.this, CreateAcount.class);
        startActivity(pass);
    }

    private void login(){
        String email = eEmail.getText().toString();
        String password = ePassword.getText().toString();

        if (!email.isEmpty() && !password.isEmpty()){
            if (password.length()>=6){
                mDialog.show();
                mAut.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            //Toast.makeText(Login.this,"Autentificación correcta",Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(Login.this,MapDriver.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        }
                        else {
                            showAlert();
                        }
                        mDialog.dismiss();
                    }
                });
            }
            else{
                Toast.makeText(Login.this,"Contraseña incorrecta",Toast.LENGTH_SHORT).show();
            }
        }
        else{
            Toast.makeText(Login.this,"Los campos se encuentran vacios",Toast.LENGTH_SHORT).show();
        }
    }

    private void showAlert(){
        AlertDialog.Builder alert = new AlertDialog.Builder(Login.this);
        alert.setMessage("Se ha producido un error autentificando al usuario, la contraseña es incorrecta o no tiene cuenta.");
        alert.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        AlertDialog title = alert.create();
        title.setTitle("Error");
        title.show();

    }
}