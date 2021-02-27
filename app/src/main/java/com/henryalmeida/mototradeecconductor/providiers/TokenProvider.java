package com.henryalmeida.mototradeecconductor.providiers;



import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;
import com.henryalmeida.mototradeecconductor.models.Token;


import androidx.annotation.NonNull;

public class TokenProvider {

    DatabaseReference mDatabase;

    public TokenProvider( ) {
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Tokens");
    }

    public void create(String idUser){

        if(idUser == null)return;
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.d("Token_Error", "Token fallido", task.getException());
                            return;
                        }

                        // Get new FCM registration token
                        Token token = new Token(task.getResult());
                        mDatabase.child(idUser).setValue(token);

                    }
                });

    }
    public DatabaseReference getToken(String idUser){
        return mDatabase.child(idUser);
    }
}
