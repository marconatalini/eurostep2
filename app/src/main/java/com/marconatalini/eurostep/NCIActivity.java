package com.marconatalini.eurostep;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class NCIActivity extends Activity implements View.OnClickListener {

    Button btn_nci_gen, btn_nci_vern, btn_nci_ordini;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nci);

        btn_nci_gen = (Button) findViewById(R.id.btn_NCI_generica);
        btn_nci_vern = (Button) findViewById(R.id.btn_NCI_verniciatura);
        btn_nci_ordini = (Button) findViewById(R.id.btn_NCI_ordini);
        btn_nci_gen.setOnClickListener(this);
        btn_nci_vern.setOnClickListener(this);
        btn_nci_ordini.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {

        if (v == btn_nci_gen) {
            startActivity(new Intent(NCIActivity.this, NCI_generica.class));
        }
        if (v == btn_nci_vern) {
            startActivity(new Intent(NCIActivity.this, NCI_verniciatura.class));
        }
        if (v == btn_nci_ordini) {
            startActivity(new Intent(NCIActivity.this, NCI_ordini.class));
        }

    }
}
