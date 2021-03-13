package com.henryalmeida.mototradeecconductor.receivers;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.henryalmeida.mototradeecconductor.R;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class PopupAdapter implements GoogleMap.InfoWindowAdapter {
    private View popup = null;
    private LayoutInflater inflater = null;
    private HashMap<String, String> images = null;
    private Context ctxt = null;
    private Marker lastMarker = null;

    public PopupAdapter(Context ctxt, LayoutInflater inflater,
                        HashMap<String, String> images) {
        this.ctxt = ctxt;
        this.inflater = inflater;
        this.images = images;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return (null);
    }

    @SuppressLint("InflateParams")
    @Override
    public View getInfoContents(final Marker marker) {
        if (popup == null) {
            popup = inflater.inflate(R.layout.popup, null);
        }

        if (lastMarker == null || !lastMarker.getId().equals(marker.getId())) {
            lastMarker = marker;

            // Intaciamos los objetos como en onCreate
            TextView tv = popup.findViewById(R.id.title);
            TextView icon =  popup.findViewById(R.id.icon);
            TextView collectMoney = popup.findViewById(R.id.collectMoney);
            // Button buttonRequest = popup.findViewById(R.id.btn_CallPhone);

            tv.setText("Debe entregar: \n"+ marker.getTitle());
            icon.setText("Número celular: \n" + images.get("phone"));
            collectMoney.setText("Recaudar: \n" + images.get("collectMoney"));
            // tv.setText(marker.getSnippet());

           /* String image = images.get(marker.getTag());

            if (image == null) {
                icon.setText("");
            } else {
                Picasso.with(ctxt).load(image).into(icon, new MarkerCallback(marker));
            }*/

        }

        return (popup);
    }

    class MarkerCallback implements Callback {
        Marker marker = null;

        MarkerCallback(Marker marker) {
            this.marker = marker;
        }

        @Override
        public void onError() {
            Log.e(getClass().getSimpleName(), "Error loading thumbnail!");
        }

        @Override
        public void onSuccess() {
            if (marker != null && marker.isInfoWindowShown()) {
                marker.showInfoWindow();
            }
        }
    }
}

