package com.marconatalini.eurostep;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class ListeActivity extends Activity implements View.OnClickListener{

    Button dati_inviati, cianfrinatura, verniciatura, cnc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_liste);

        dati_inviati = (Button) findViewById(R.id.btn_dati_inviati);
        cianfrinatura = (Button) findViewById(R.id.btn_ordini_cianfrinato);
        verniciatura = (Button) findViewById(R.id.btn_verniciatura);
        cnc = (Button) findViewById(R.id.btn_ordini_cnc);

        dati_inviati.setOnClickListener(this);
        cianfrinatura.setOnClickListener(this);
        verniciatura.setOnClickListener(this);
        cnc.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent lista = new Intent(ListeActivity.this, Lista_ordini_la.class);
        lista.putExtra("json_url","http://"+ MainActivity.WEBSERVER_IP +"/lista_ordiniLA.php");

        if (v == dati_inviati) {
            lista.putExtra("titolo_lista", "Registro fine lavoro");
            lista.putExtra("phpquery", "DatiInviati");
        }
        if (v == cianfrinatura) {
            lista.putExtra("titolo_lista", "Ordini da cianfrinare");
            lista.putExtra("phpquery", "ArrivoInCianfrinatura");
        }
        if (v == verniciatura) {
            lista.putExtra("titolo_lista", "Ordini già levigati");
            lista.putExtra("phpquery", "ArrivoInVerniciatura");
        }
        if (v == cnc) {
            lista.putExtra("titolo_lista", "Ordini con lavorazioni");
            lista.putExtra("phpquery", "OrdiniCNC");
        }

        startActivity(lista);
    }
}
