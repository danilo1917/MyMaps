package com.example.mymapsapp;

import android.app.ListActivity;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.maps.model.LatLng;

import java.util.Arrays;
import java.util.Vector;

public class Report extends ListActivity {
    private Vector<Vector<String>> jointLogs;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

         jointLogs = getLogs();
        Vector<String> logs = new Vector<>();

        for (Vector<String> log: jointLogs){
            logs.add(log.get(0) + " - " + log.get(1));
            Log.i("CARREGADO", "ok");
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, logs);
        setListAdapter(adapter);
    }

    Vector<Vector<String>> getLogs() {
        Vector<Vector<String>> logs = new Vector<>();

        Cursor cursor = BancoDados.getInstance().buscar(
                "Logs INNER JOIN Location ON Logs.id_location = Location.id",
                new String[]{"msg", "timestamp", "latitude", "longitude"},
                "",
                ""
        );

        while (cursor.moveToNext()) {
            String msg = cursor.getString(cursor.getColumnIndexOrThrow("msg"));
            String timestamp = cursor.getString(cursor.getColumnIndexOrThrow("timestamp"));
            double latitude = cursor.getDouble(cursor.getColumnIndexOrThrow("latitude"));
            double longitude = cursor.getDouble(cursor.getColumnIndexOrThrow("longitude"));

            Vector<String> aux = new Vector<>();
            aux.add(msg);
            aux.add(timestamp);
            aux.add(Double.toString(latitude));
            aux.add(Double.toString(longitude));

            logs.add(aux);
        }
        cursor.close();


        return logs;
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        Vector<String> data = jointLogs.get(position);
        String resultText = "Coordenadas do ponto "+data.get(0) + ": " + "("+data.get(2)+ ", "+ data.get(3) + ")";

        Toast.makeText(this, resultText, Toast.LENGTH_LONG).show();

    }
}