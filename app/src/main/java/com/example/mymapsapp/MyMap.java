package com.example.mymapsapp;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;

public class MyMap extends FragmentActivity implements OnMapReadyCallback, LocationListener {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private  LatLng HOME, DPI, VICOSA;

    private GoogleMap map;
    private boolean isMapReady = false;

    LatLng[] markers;
    private int initialMapPos = 0;

    private LocationManager lm;
    private String locationProvider;
    private Criteria locCriteria;

    private Marker current;
    private Location currentLoc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_map);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }

        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        Intent it = getIntent();
        initialMapPos = it.getIntExtra("map", 0);

        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locCriteria = new Criteria();

        PackageManager packageManager = getPackageManager();
        boolean hasGPS = packageManager.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS);

        if (hasGPS) {
            locCriteria.setAccuracy(Criteria.ACCURACY_FINE);
            Log.i("LOCATION", "Usando GPS");
        } else {
            Log.i("LOCATION", "Usando WI-FI ou dados");
            locCriteria.setAccuracy(Criteria.ACCURACY_COARSE);
        }

        HashMap<String, LatLng> localizacoes = carregarLocalizacoes();

        HOME = localizacoes.get("Cidade Natal");
        DPI = localizacoes.get("DPI");
        VICOSA = localizacoes.get("Viçosa");
        markers = new LatLng[]{HOME, DPI, VICOSA};
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationProvider = lm.getBestProvider(locCriteria, true);

            if (locationProvider == null) {
                Log.i("PROVEDOR", "Provedor não encontrado");
            } else {
                Log.i("PROVEDOR", "Provedor encontrado " + locationProvider);
                lm.requestLocationUpdates(locationProvider, 1000, 0, this);
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            lm.removeUpdates(this);
        }
        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permissão concedida!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permissão negada. O aplicativo pode não funcionar corretamente.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void changeMarker(View view) {
        if (!isMapReady) {
            Toast.makeText(this, "Mapa não está pronto ainda!", Toast.LENGTH_SHORT).show();
            return;
        }

        Button clickedBtn = (Button) view;
        String tag = clickedBtn.getTag().toString();

        try {
            int index = Integer.parseInt(tag);

            map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
            CameraUpdate update = CameraUpdateFactory.newLatLngZoom(markers[index], 16);
            map.animateCamera(update);

        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        isMapReady = true;

        map.addMarker(new MarkerOptions().position(VICOSA).title("Meu apt Viçosa"));
        map.addMarker(new MarkerOptions().position(HOME).title("Minha casa natal"));
        map.addMarker(new MarkerOptions().position(DPI).title("DPI/UFV"));

        CameraUpdate initialView = CameraUpdateFactory.newLatLngZoom(markers[initialMapPos], 16);
        map.moveCamera(initialView);
    }

    @Override
    public void onLocationChanged(Location location) {
        if ( location != null) {
            Log.i("LOC", "RECEBIDA");
            currentLoc = location;
        }
    }

    public void currentLocation(View view) {
        if (!isMapReady || currentLoc == null) {
            Toast.makeText(this, "Não foi possível encontrar a localização do usuário", Toast.LENGTH_SHORT).show();
            return;
        }
        if (current != null){
            current.remove();
        }
        LatLng loc = new LatLng(currentLoc.getLatitude(), currentLoc.getLongitude());
        current = map.addMarker(new MarkerOptions().position(loc).title("Localização atual"));
        CameraUpdate initialView = CameraUpdateFactory.newLatLngZoom(loc, 16);
        map.moveCamera(initialView);
        final Location vicLoc = new Location(locationProvider);

        vicLoc.setLatitude(markers[2].latitude);
        vicLoc.setLongitude(markers[2].longitude);

        double dist = currentLoc.distanceTo(vicLoc);
        Toast.makeText(this, "Distância: "+dist , Toast.LENGTH_SHORT).show();
    }

    public HashMap<String, LatLng> carregarLocalizacoes() {
        HashMap<String, LatLng> localizacoes = new HashMap<>();

        // Busca todas as localizações no banco de dados
        Cursor cursor = BancoDados.getInstance().buscar("Location", new String[]{"descricao", "latitude", "longitude"}, "", "");
        while (cursor.moveToNext()) {
            // Extrai os dados do cursor
            String descricao = cursor.getString(cursor.getColumnIndex("descricao"));
            double latitude = cursor.getDouble(cursor.getColumnIndex("latitude"));
            double longitude = cursor.getDouble(cursor.getColumnIndex("longitude"));

            // Cria uma nova LatLng e adiciona ao HashMap
            localizacoes.put(descricao, new LatLng(latitude, longitude));
        }

        cursor.close();

        return localizacoes;
    }
}
