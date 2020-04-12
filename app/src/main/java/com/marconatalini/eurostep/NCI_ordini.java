package com.marconatalini.eurostep;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.zxing.client.android.Intents;
import com.google.zxing.integration.android.IntentIntegrator;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.Map;

public class NCI_ordini extends Activity {

    Button btn_scan_ordine, btn_data_soluzione, btn_send;
    EditText numero_lotto;
    GridLayout grid_prelievo, grid_taglio, grid_lavorazioni, grid_guarnizioni, grid_imballo, grid_verniciatura, grid_controlli;
    CheckBox prelievo_profili_errati, prelievo_mancanza_barre, prelievo_qualita_estruso;
    CheckBox taglio_barre_difettose, taglio_documenti_insufficienti;
    CheckBox lavorazioni_errore_taglio, lavorazioni_difetti_qualita, lavorazioni_errore_sviluppo;
    CheckBox guarnizioni_pezzi_mancanti, guarnizioni_difetti_qualita;
    CheckBox imballo_pezzi_mancanti, imballo_altro;
    CheckBox verniciatura_errore_colore, verniciatura_profili_ammaccati, verniciatura_altro, verniciatura_bucciato,
            verniciatura_graffi, verniciatura_scarsa_polvere, verniciatura_macchie, verniciatura_aloni, verniciatura_impurita;
    CheckBox controlli_profili_errati, controlli_dimensioni_errate, controlli_altro;
    TextView nciv_note, soluzione;
    RadioButton chiusa;
    Spinner spinner_erroreIN;

    final static int GET_NUMERO_ORDINE = 1;
    private String UPLOAD_URL = "http://" + MainActivity.WEBSERVER_IP + "/registraNCO.php";

    Boolean DatiOK = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nci_ordini);

        final Intent scan = new IntentIntegrator(NCI_ordini.this).createScanIntent();
        final SocketTask socketTask = new SocketTask(NCI_ordini.this);

        spinner_erroreIN = (Spinner) findViewById(R.id.spinner_erroreIN);
        ArrayAdapter<CharSequence> lav_adapter = ArrayAdapter.createFromResource(this,
                R.array.erroreIN, android.R.layout.simple_spinner_item);
        lav_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_erroreIN.setAdapter(lav_adapter);

        grid_prelievo = (GridLayout) findViewById(R.id.checkgroup_prelievo);
        grid_taglio = (GridLayout) findViewById(R.id.checkgroup_taglio);
        grid_lavorazioni = (GridLayout) findViewById(R.id.checkgroup_lavorazioni);
        grid_guarnizioni = (GridLayout) findViewById(R.id.checkgroup_guarnizioni);
        grid_imballo = (GridLayout) findViewById(R.id.checkgroup_imballo);
        grid_verniciatura = (GridLayout) findViewById(R.id.checkgroup_verniciatura);
        grid_controlli = (GridLayout) findViewById(R.id.checkgroup_controlli);

        final GridLayout [] gridLayouts = {grid_prelievo, grid_taglio, grid_lavorazioni, grid_guarnizioni, grid_imballo, grid_verniciatura, grid_controlli};

        spinner_erroreIN.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                for(int i = 0; i<gridLayouts.length; i++) {
                    if (i == position){
                        gridLayouts[i].setVisibility(View.VISIBLE);
                    } else {
                        gridLayouts[i].setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        prelievo_profili_errati = (CheckBox) findViewById(R.id.check_prelievo_profili_errati);
        prelievo_mancanza_barre = (CheckBox) findViewById(R.id.check_prelievo_mancanza_barre);
        prelievo_qualita_estruso = (CheckBox) findViewById(R.id.check_prelievo_qualita_estruso);

        taglio_barre_difettose = (CheckBox) findViewById(R.id.check_taglio_barre_difettose);
        taglio_documenti_insufficienti = (CheckBox) findViewById(R.id.check_taglio_documenti_insufficienti);

        lavorazioni_errore_taglio = (CheckBox) findViewById(R.id.check_lavorazioni_errore_taglio);
        lavorazioni_difetti_qualita = (CheckBox) findViewById(R.id.check_lavorazioni_difetti_qualita);
        lavorazioni_errore_sviluppo =(CheckBox) findViewById(R.id.check_lavorazioni_errore_sviluppo);

        guarnizioni_pezzi_mancanti = (CheckBox) findViewById(R.id.check_guarnizioni_pezzi_mancanti);
        guarnizioni_difetti_qualita = (CheckBox) findViewById(R.id.check_guarnizioni_difetti_qualita);

        imballo_pezzi_mancanti = (CheckBox) findViewById(R.id.check_imballo_pezzi_mancanti);
        imballo_altro = (CheckBox) findViewById(R.id.check_imballo_altro);

        verniciatura_errore_colore = (CheckBox) findViewById(R.id.check_verniciatura_errore_colore);
        verniciatura_profili_ammaccati = (CheckBox) findViewById(R.id.check_verniciatura_profili_ammaccati);
        verniciatura_altro = (CheckBox) findViewById(R.id.check_verniciatura_altro);
        verniciatura_bucciato = (CheckBox) findViewById(R.id.check_verniciatura_bucciato);
        verniciatura_graffi = (CheckBox) findViewById(R.id.check_verniciatura_graffi);
        verniciatura_scarsa_polvere = (CheckBox) findViewById(R.id.check_verniciatura_scarsa_polvere);
        verniciatura_macchie = (CheckBox) findViewById(R.id.check_verniciatura_macchie);
        verniciatura_aloni = (CheckBox) findViewById(R.id.check_verniciatura_aloni);
        verniciatura_impurita = (CheckBox) findViewById(R.id.check_verniciatura_impurita);

        controlli_profili_errati = (CheckBox) findViewById(R.id.check_controlli_profili_errati);
        controlli_dimensioni_errate = (CheckBox) findViewById(R.id.check_controlli_dimensioni_errate);
        controlli_altro = (CheckBox) findViewById(R.id.check_controlli_altro) ;

        btn_send = (Button) findViewById(R.id.btn_send);
        nciv_note = (TextView) findViewById(R.id.nciv_note);
        soluzione = (TextView) findViewById(R.id.nciv_soluzione);
        numero_lotto = (EditText) findViewById(R.id.numero_lotto);
        TextWatcher numeroWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 8 && (socketTask.checkBarcodeOrdine(s.toString()))) {
                    Toast.makeText(NCI_ordini.this,"Numero OK: " + s.toString(),Toast.LENGTH_SHORT).show();
                    DatiOK = true;
                }

                if (s.length() == 6 && (socketTask.checkNumeroOrdineBarre(s.toString()))){
                    DatiOK = true;
                }

            }
        };
        numero_lotto.addTextChangedListener(numeroWatcher);

        btn_scan_ordine = (Button) findViewById(R.id.btn_scan);
        btn_scan_ordine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(scan, GET_NUMERO_ORDINE);
            }
        });

        RadioGroup ncio_stato = (RadioGroup) findViewById(R.id.nciv_stato);
        chiusa = (RadioButton) findViewById(R.id.ncio_chiusa);
        ncio_stato.check(chiusa.getId());

        btn_data_soluzione = (Button) findViewById(R.id.btn_data_soluzione);
        btn_data_soluzione.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get Current Date
                final Calendar c = Calendar.getInstance();
                int mYear = c.get(Calendar.YEAR);
                int mMonth = c.get(Calendar.MONTH);
                int mDay = c.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(NCI_ordini.this,
                        new DatePickerDialog.OnDateSetListener() {

                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {

                                if (DatiOK) {
                                    btn_send.setEnabled(true);
                                    btn_data_soluzione.setText(year + "-" + (monthOfYear + 1) + "-" + dayOfMonth);
                                } else {
                                    Toast.makeText(NCI_ordini.this, "Controlla il numero ordine.", Toast.LENGTH_SHORT).show();
                                }

                            }
                        }, mYear, mMonth, mDay);
                datePickerDialog.show();

            }
        });

        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendNCIO();
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == -1) { //scansione OK
            String result = data.getStringExtra(Intents.Scan.RESULT);
            switch (requestCode) {
                case GET_NUMERO_ORDINE:
                    numero_lotto.setText(result.substring(0,8));
                    break;
            }
        } else {
            Toast.makeText(this, "Scansione ANNULLATA", Toast.LENGTH_LONG).show();
        }
        // This is important, otherwise the result will not be passed to the fragment
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void sendNCIO() {
        //Showing the progress dialog
        final ProgressDialog loading = ProgressDialog.show(this, "Invio dati", "Attendi...", false, false);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, UPLOAD_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        //Disimissing the progress dialog
                        loading.dismiss();
                        //Showing toast message of the response
                        Toast.makeText(NCI_ordini.this, s, Toast.LENGTH_LONG).show();
                        Log.d("volleyerror", s);
                        finish();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        //Dismissing the progress dialog
                        loading.dismiss();
                        //Showing toast
                        Toast.makeText(NCI_ordini.this, volleyError.toString(), Toast.LENGTH_LONG).show();
                        Log.d("volleyerror", volleyError.toString());
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                //Getting Image Name
                String mnumero_ordine = numero_lotto.getText().toString().substring(0, 6);
                String mlotto_ordine = numero_lotto.getText().toString().substring(7, 8);
                String mcod_operatore = MainActivity.OPERATORE;
                String merrorein = spinner_erroreIN.getSelectedItem().toString();
                String mprelievo_profili_errati = String.valueOf(prelievo_profili_errati.isChecked());
                String mprelievo_mancanza_barre = String.valueOf(prelievo_mancanza_barre.isChecked());
                String mprelievo_qualita_estruso = String.valueOf(prelievo_qualita_estruso.isChecked());

                String mtaglio_barre_difettose = String.valueOf(taglio_barre_difettose.isChecked());
                String mtaglio_documenti_insufficienti= String.valueOf(taglio_documenti_insufficienti.isChecked());

                String mlavorazioni_errore_taglio = String.valueOf(lavorazioni_errore_taglio.isChecked());
                String mlavorazioni_difetti_qualita = String.valueOf(lavorazioni_difetti_qualita.isChecked());
                String mlavorazioni_errore_sviluppo = String.valueOf(lavorazioni_errore_sviluppo.isChecked());

                String mguarnizioni_pezzi_mancanti = String.valueOf(guarnizioni_pezzi_mancanti.isChecked());
                String mguarnizioni_difetti_qualita = String.valueOf(guarnizioni_difetti_qualita.isChecked());

                String mimballo_pezzi_mancanti = String.valueOf(imballo_pezzi_mancanti.isChecked());
                String mimballo_altro = String.valueOf(imballo_altro.isChecked());

                String mverniciatura_errore_colore = String.valueOf(verniciatura_errore_colore.isChecked());
                String mverniciatura_bucciato = String.valueOf(verniciatura_bucciato.isChecked());
                String mverniciatura_macchie = String.valueOf(verniciatura_macchie.isChecked());
                String mverniciatura_profili_ammaccati= String.valueOf(verniciatura_profili_ammaccati.isChecked());
                String mverniciatura_graffi = String.valueOf(verniciatura_graffi.isChecked());
                String mverniciatura_aloni = String.valueOf(verniciatura_aloni.isChecked());
                String mverniciatura_altro = String.valueOf(verniciatura_altro.isChecked());
                String mverniciatura_scarsa_polvere = String.valueOf(verniciatura_scarsa_polvere.isChecked());
                String mverniciatura_impurita = String.valueOf(verniciatura_impurita.isChecked());

                String mcontrolli_profili_errati = String.valueOf(controlli_profili_errati.isChecked());
                String mcontrolli_dimensioni_errate = String.valueOf(controlli_dimensioni_errate.isChecked());
                String mcontrolli_altro = String.valueOf(controlli_altro.isChecked());

                String mnote_nci = nciv_note.getText().toString();
                String msoluzione = soluzione.getText().toString();
                String mdata_soluzione = btn_data_soluzione.getText().toString();
                String mchiusa = String.valueOf(chiusa.isChecked());

                //Creating parameters
                Map<String, String> params = new Hashtable<String, String>();

                //Adding parameters
                params.put("numero_ordine", mnumero_ordine);
                params.put("lotto_ordine", mlotto_ordine);
                params.put("cod_operatore", mcod_operatore);

                params.put("errorein", merrorein);
                params.put("prelievo_profili_errati", mprelievo_profili_errati);
                params.put("prelievo_mancanza_barre", mprelievo_mancanza_barre);
                params.put("prelievo_qualita_estruso", mprelievo_qualita_estruso);
                params.put("taglio_barre_difettose", mtaglio_barre_difettose);
                params.put("taglio_documenti_insufficienti", mtaglio_documenti_insufficienti);
                params.put("lavorazioni_errore_taglio", mlavorazioni_errore_taglio);
                params.put("lavorazioni_difetti_qualita", mlavorazioni_difetti_qualita);
                params.put("lavorazioni_errore_sviluppo", mlavorazioni_errore_sviluppo);
                params.put("guarnizioni_pezzi_mancanti", mguarnizioni_pezzi_mancanti);
                params.put("guarnizioni_difetti_qualita", mguarnizioni_difetti_qualita);
                params.put("imballo_pezzi_mancanti", mimballo_pezzi_mancanti);
                params.put("imballo_altro", mimballo_altro);
                params.put("verniciatura_errore_colore", mverniciatura_errore_colore);
                params.put("verniciatura_bucciato", mverniciatura_bucciato);
                params.put("verniciatura_macchie", mverniciatura_macchie);
                params.put("verniciatura_profili_ammaccati", mverniciatura_profili_ammaccati);
                params.put("verniciatura_graffi", mverniciatura_graffi);
                params.put("verniciatura_aloni", mverniciatura_aloni);
                params.put("verniciatura_altro", mverniciatura_altro);
                params.put("verniciatura_scarsa_polvere", mverniciatura_scarsa_polvere);
                params.put("verniciatura_impurita", mverniciatura_impurita);

                params.put("controlli_profili_errati", mcontrolli_profili_errati);
                params.put("controlli_dimensioni_errate", mcontrolli_dimensioni_errate);
                params.put("controlli_altro", mcontrolli_altro);

                params.put("note_nci", mnote_nci);
                params.put("soluzione", msoluzione);
                params.put("data_soluzione", mdata_soluzione);
                params.put("chiusa", mchiusa);

                //returning parameters
                return params;
            }
        };

        int socketTimeout = 30000;//30 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        stringRequest.setRetryPolicy(policy);

        //Creating a Request Queue
        //RequestQueue requestQueue = Volley.newRequestQueue(this);

        //Adding request to the queue
        //requestQueue.add(stringRequest);

        MySingleton.getInstance(NCI_ordini.this).addToRequestque(stringRequest);
    }
}
