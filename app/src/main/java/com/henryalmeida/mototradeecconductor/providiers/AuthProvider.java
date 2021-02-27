package com.henryalmeida.mototradeecconductor.providiers;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class AuthProvider {
    FirebaseAuth mAuth;

    public AuthProvider() {
        mAuth = FirebaseAuth.getInstance();
    }

    public Task<AuthResult> register(String email, String password){
        return mAuth.createUserWithEmailAndPassword(email,password);
    }

    public Task<AuthResult> login(String email, String password){
        return mAuth.createUserWithEmailAndPassword(email,password);
    }

    //Para que no se ciere la sesion

    public void logout(){
        mAuth.signOut();
    }
    // Obtener el id del condutor
    public String getId(){
        return mAuth.getCurrentUser().getUid();
    }
    // Para saber si existe el usuario en sesion
    public boolean existSession(){
        boolean exist = false;
        if(mAuth.getCurrentUser()!=null){
            exist = true;
        }
        return exist;
    }
}
