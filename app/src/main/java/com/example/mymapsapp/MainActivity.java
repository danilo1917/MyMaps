package com.example.mymapsapp;

import android.app.ListActivity;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.time.Instant;

public class MainActivity extends ListActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String [] places = {"Minha casa na cidade natal", "Meu departamento","Minha casa em Viçosa", "Relatório", "Fechar Aplicação"};

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, places);
        setListAdapter(adapter);

    }


    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        if (position == 4){
            finish();
            return;
        }
        if (position == 3){
//            faz outra coisa
            Intent it = new Intent(this, Report.class);
            startActivity(it);
        }
        else {
            String clicked = l.getItemAtPosition(position).toString();
            ContentValues cv = new ContentValues();
            cv.put("msg", clicked);
            cv.put("timestamp", "" + Instant.now());
            cv.put("id_location", position);

            BancoDados.getInstance().inserir("Logs", cv);
            Toast.makeText(this, "" + clicked + " selecionado", Toast.LENGTH_SHORT).show();

            Intent it = new Intent(this, MyMap.class);

            it.putExtra("map", position);
            startActivity(it);
        }
    }
}