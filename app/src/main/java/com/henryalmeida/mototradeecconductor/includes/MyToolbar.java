package com.henryalmeida.mototradeecconductor.includes;

import com.henryalmeida.mototradeecconductor.R;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class MyToolbar {
    public static void show(Toolbar toolbar,AppCompatActivity activity, String title, boolean upButton){

        //Toolbar toolbar = activity.findViewById(R.id.toolbar);
        activity.setSupportActionBar(toolbar);
        activity.getSupportActionBar().setTitle(title);
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(upButton);
    }
    public static void show1(AppCompatActivity activity, String title, boolean upButton){

        Toolbar toolbar = activity.findViewById(R.id.toolbar);
        activity.setSupportActionBar(toolbar);
        activity.getSupportActionBar().setTitle(title);
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(upButton);
    }
}
