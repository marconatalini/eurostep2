package com.marconatalini.eurostep;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.zxing.client.android.Intents;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.Calendar;
import java.util.Hashtable;
import java.util.Map;

public class NCI_verniciatura extends Activity {

    Button btn_data_vern, btn_ora_vern, btn_scan_ordine, btn_data_soluzione, btn_send;
    ToggleButton btn_difetto;
    Button btn_agg1, btn_agg2, btn_agg3, btn_ver1, btn_ver2, btn_ver3;
    EditText numero_lotto, marca_polvere;
    TextView xcdcol, xdecol;
    GridLayout grid_entrata, grid_uscita;
    CheckBox difetto_telai, difetto_complementari, difetto_lamiere, difetto_bugne, difetto_barre;
    CheckBox de_profili_errati, de_profili_deformati, de_profili_ammaccati, de_levigatura, de_mancanza_fori;
    CheckBox du_troppa_polvere, du_bucciato, du_aloni, du_scarsa_polvere, du_impurita, du_errore_colore,
            du_angolo_interno_non_verniciato, du_cava_telaio_non_verniciata, du_bolle_vernice, du_crateri,
            du_tracce_silicone, du_macchie_acqua, du_occhi_pernice, du_punte_spillo, du_graffi,
            du_cadute_impianto, du_altro;
    TextView nciv_note, soluzione;
    RadioButton chiusa;

    private int mYear, mMonth, mDay, mHour, mMinute;
    final static int GET_NUMERO_ORDINE = 1;
    final static int GET_AGG1 = 2;
    final static int GET_AGG2 = 3;
    final static int GET_AGG3 = 4;
    final static int GET_VER1 = 5;
    final static int GET_VER2 = 6;
    final static int GET_VER3 = 7;

    private String UPLOAD_URL = "http://" + MainActivity.WEBSERVER_IP + "/registraNCV.php";
    private String COLORE_URL = "http://" + MainActivity.WEBSERVER_IP + "/getColoreAS.php";

    Boolean DatiOK = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nci_verniciatura);
        final Intent scan = new IntentIntegrator(NCI_verniciatura.this).createScanIntent();
        final SocketTask socketTask = new SocketTask(NCI_verniciatura.this);

        difetto_telai = (CheckBox) findViewById(R.id.check_telai);
        difetto_complementari = (CheckBox) findViewById(R.id.check_complementari);
        difetto_lamiere = (CheckBox) findViewById(R.id.check_lamiere);
        difetto_bugne = (CheckBox) findViewById(R.id.check_bugne);
        difetto_barre = (CheckBox) findViewById(R.id.check_Barre);

        de_profili_errati = (CheckBox) findViewById(R.id.check_profili_errati);
        de_profili_deformati = (CheckBox) findViewById(R.id.check_profili_deformati);
        de_profili_ammaccati = (CheckBox) findViewById(R.id.check_profili_ammaccati);
        de_levigatura = (CheckBox) findViewById(R.id.check_difetti_levigatura);
        de_mancanza_fori = (CheckBox) findViewById(R.id.check_mancanza_foratura);

        du_troppa_polvere = (CheckBox) findViewById(R.id.check_troppa_polvere);
        du_bucciato = (CheckBox) findViewById(R.id.check_bucciato);
        du_aloni = (CheckBox) findViewById(R.id.check_aloni);
        du_scarsa_polvere = (CheckBox) findViewById(R.id.check_scarsa_polvere);
        du_impurita = (CheckBox) findViewById(R.id.check_impurita);
        du_errore_colore = (CheckBox) findViewById(R.id.check_errore_colore);
        du_angolo_interno_non_verniciato = (CheckBox) findViewById(R.id.check_angolo_interno);
        du_cava_telaio_non_verniciata = (CheckBox) findViewById(R.id.check_cava_telaio);
        du_bolle_vernice = (CheckBox) findViewById(R.id.check_bolle_vernice);
        du_crateri = (CheckBox) findViewById(R.id.check_crateri);
        du_tracce_silicone = (CheckBox) findViewById(R.id.check_tracce_silicone);
        du_macchie_acqua = (CheckBox) findViewById(R.id.check_macchie_acqua);
        du_occhi_pernice = (CheckBox) findViewById(R.id.check_occhi_pernice);
        du_punte_spillo = (CheckBox) findViewById(R.id.check_punte_spillo);
        du_graffi = (CheckBox) findViewById(R.id.check_graffi);
        du_cadute_impianto = (CheckBox) findViewById(R.id.check_cadute_impianto);
        du_altro = (CheckBox) findViewById(R.id.check_altro_difetto_uscita);

        btn_send = (Button) findViewById(R.id.btn_send);
        xcdcol = (TextView) findViewById(R.id.xcdcol);
        xdecol = (TextView) findViewById(R.id.xdecol);
        marca_polvere = (EditText) findViewById(R.id.marca_polvere);
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
                    //Toast.makeText(NCI_generica.this,"Numero OK: " + s.toString(),Toast.LENGTH_SHORT).show();
                    getDescrizioneColore();
                    DatiOK = true;
                }

                if (s.length() == 6 && (socketTask.checkNumeroOrdineBarre(s.toString()))){
                    DatiOK = true;
                }

            }
        };

        numero_lotto.addTextChangedListener(numeroWatcher);

        grid_entrata = (GridLayout) findViewById(R.id.checkgroup_entrata);
        grid_uscita = (GridLayout) findViewById(R.id.checkgroup_uscita);

        btn_difetto = (ToggleButton) findViewById(R.id.btn_difetto);
        btn_difetto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ToggleButton btn = (ToggleButton) v;
                if (btn.isChecked()) {
                    grid_entrata.setVisibility(View.VISIBLE);
                    grid_uscita.setVisibility(View.GONE);
                } else {
                    grid_entrata.setVisibility(View.GONE);
                    grid_uscita.setVisibility(View.VISIBLE);
                }
            }
        });

        btn_scan_ordine = (Button) findViewById(R.id.btn_scan);
        btn_scan_ordine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(scan, GET_NUMERO_ORDINE);
            }
        });

        btn_agg1 = (Button) findViewById(R.id.btn_agganciatore1);
        btn_agg1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(scan, GET_AGG1);
            }
        });

        btn_agg2 = (Button) findViewById(R.id.btn_agganciatore2);
        btn_agg2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(scan, GET_AGG2);
            }
        });

        btn_agg3 = (Button) findViewById(R.id.btn_agganciatore3);
        btn_agg3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(scan, GET_AGG3);
            }
        });

        btn_ver1 = (Button) findViewById(R.id.btn_verniciatore1);
        btn_ver1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(scan, GET_VER1);
            }
        });
        btn_ver2 = (Button) findViewById(R.id.btn_verniciatore2);
        btn_ver2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(scan, GET_VER2);
            }
        });
        btn_ver3 = (Button) findViewById(R.id.btn_verniciatore3);
        btn_ver3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(scan, GET_VER3);
            }
        });


        btn_data_vern = (Button) findViewById(R.id.btn_data_verniciatura);
        btn_data_vern.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get Current Date
                final Calendar c = Calendar.getInstance();
                mYear = c.get(Calendar.YEAR);
                mMonth = c.get(Calendar.MONTH);
                mDay = c.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(NCI_verniciatura.this,
                        new DatePickerDialog.OnDateSetListener() {

                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {

                                btn_data_vern.setText(year + "-" + (monthOfYear + 1) + "-" + dayOfMonth);

                            }
                        }, mYear, mMonth, mDay);
                datePickerDialog.show();

            }
        });

        btn_ora_vern = (Button) findViewById(R.id.btn_ora_verniciatura);
        btn_ora_vern.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get Current Time
                final Calendar c = Calendar.getInstance();
                mHour = c.get(Calendar.HOUR_OF_DAY);
                mMinute = c.get(Calendar.MINUTE);

                // Launch Time Picker Dialog
                TimePickerDialog timePickerDialog = new TimePickerDialog(NCI_verniciatura.this,
                        new TimePickerDialog.OnTimeSetListener() {

                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay,
                                                  int minute) {

                                btn_ora_vern.setText(" " + hourOfDay + ":" + minute); //spazio iniziale per concatenarla
                            }
                        }, mHour, mMinute, false);
                timePickerDialog.show();
            }
        });

        btn_data_soluzione = (Button) findViewById(R.id.btn_data_soluzione);
        btn_data_soluzione.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get Current Date
                final Calendar c = Calendar.getInstance();
                mYear = c.get(Calendar.YEAR);
                mMonth = c.get(Calendar.MONTH);
                mDay = c.get(Calendar.DAY_OF_MONTH) + 1;

                DatePickerDialog datePickerDialog = new DatePickerDialog(NCI_verniciatura.this,
                        new DatePickerDialog.OnDateSetListener() {

                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {

                                if (DatiOK) {
                                    btn_send.setEnabled(true);
                                    btn_data_soluzione.setText(year + "-" + (monthOfYear + 1) + "-" + dayOfMonth);
                                } else {
                                    Toast.makeText(NCI_verniciatura.this, "Controlla il numero ordine.", Toast.LENGTH_SHORT).show();
                                }

                            }
                        }, mYear, mMonth, mDay);
                datePickerDialog.show();

            }
        });

        RadioGroup nciv_stato = (RadioGroup) findViewById(R.id.nciv_stato);
        chiusa = (RadioButton) findViewById(R.id.nciv_chiusa);
        nciv_stato.check(chiusa.getId());

        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendNCIV();
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
                case GET_AGG1:
                    btn_agg1.setText(result.substring(2));
                    break;
                case GET_AGG2:
                    btn_agg2.setText(result.substring(2));
                    break;
                case GET_AGG3:
                    btn_agg3.setText(result.substring(2));
                    break;
                case GET_VER1:
                    btn_ver1.setText(result.substring(2));
                    break;
                case GET_VER2:
                    btn_ver2.setText(result.substring(2));
                    break;
                case GET_VER3:
                    btn_ver3.setText(result.substring(2));
                    break;
            }
        } else {
            Toast.makeText(this, "Scansione ANNULLATA", Toast.LENGTH_LONG).show();
        }
        // This is important, otherwise the result will not be passed to the fragment
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void sendNCIV() {
        //Showing the progress dialog
        final ProgressDialog loading = ProgressDialog.show(this, "Invio dati", "Attendi...", false, false);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, UPLOAD_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        //Disimissing the progress dialog
                        loading.dismiss();
                        //Showing toast message of the response
                        Toast.makeText(NCI_verniciatura.this, s, Toast.LENGTH_LONG).show();
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
                        Toast.makeText(NCI_verniciatura.this, volleyError.toString(), Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                //Getting Image Name
                String mnumero_ordine = numero_lotto.getText().toString().substring(0, 6);
                String mlotto_ordine = numero_lotto.getText().toString().substring(7, 8);
                String mcod_operatore = MainActivity.OPERATORE;
                String mxcdcol = xcdcol.getText().toString();
                String mdataora_verniciatura = btn_data_vern.getText().toString() + btn_ora_vern.getText().toString();
                String mmarca_polvere = marca_polvere.getText().toString();
                String mcod_agganciatore1 = btn_agg1.getText().toString().equalsIgnoreCase("OPERATORE") ? "" : btn_agg1.getText().toString();
                String mcod_agganciatore2 = btn_agg2.getText().toString().equalsIgnoreCase("OPERATORE") ? "" : btn_agg2.getText().toString();
                String mcod_agganciatore3 = btn_agg3.getText().toString().equalsIgnoreCase("OPERATORE") ? "" : btn_agg3.getText().toString();
                String mcod_verniciatore1 = btn_ver1.getText().toString().equalsIgnoreCase("OPERATORE") ? "" : btn_ver1.getText().toString();
                String mcod_verniciatore2 = btn_ver2.getText().toString().equalsIgnoreCase("OPERATORE") ? "" : btn_ver2.getText().toString();
                String mcod_verniciatore3 = btn_ver3.getText().toString().equalsIgnoreCase("OPERATORE") ? "" : btn_ver3.getText().toString();
                String mdifetto_telai = String.valueOf(difetto_telai.isChecked());
                String mdifetto_complementari = String.valueOf(difetto_complementari.isChecked());
                String mdifetto_lamiere = String.valueOf(difetto_lamiere.isChecked());
                String mdifetto_bugne = String.valueOf(difetto_bugne.isChecked());
                String mdifetto_barre = String.valueOf(difetto_barre.isChecked());
                String mdifettoin = String.valueOf(btn_difetto.getText());
                String mde_profili_errati = String.valueOf(de_profili_errati.isChecked());
                String mde_profili_deformati = String.valueOf(de_profili_deformati.isChecked());
                String mde_profili_ammaccati = String.valueOf(de_profili_ammaccati.isChecked());
                String mde_levigatura = String.valueOf(de_levigatura.isChecked());
                String mde_mancanza_fori = String.valueOf(de_mancanza_fori.isChecked());
                String mdu_troppa_polvere = String.valueOf(du_troppa_polvere.isChecked());
                String mdu_bucciato = String.valueOf(du_bucciato.isChecked());
                String mdu_aloni = String.valueOf(du_aloni.isChecked());
                String mdu_scarsa_polvere = String.valueOf(du_scarsa_polvere.isChecked());
                String mdu_impurita = String.valueOf(du_impurita.isChecked());
                String mdu_errore_colore = String.valueOf(du_errore_colore.isChecked());
                String mdu_angolo_interno_non_verniciato = String.valueOf(du_angolo_interno_non_verniciato.isChecked());
                String mdu_cava_telaio_non_verniciata = String.valueOf(du_cava_telaio_non_verniciata.isChecked());
                String mdu_bolle_vernice = String.valueOf(du_bolle_vernice.isChecked());
                String mdu_crateri = String.valueOf(du_crateri.isChecked());
                String mdu_tracce_silicone = String.valueOf(du_tracce_silicone.isChecked());
                String mdu_macchie_acqua = String.valueOf(du_macchie_acqua.isChecked());
                String mdu_occhi_pernice = String.valueOf(du_occhi_pernice.isChecked());
                String mdu_punte_spillo = String.valueOf(du_punte_spillo.isChecked());
                String mdu_graffi = String.valueOf(du_graffi.isChecked());
                String mdu_cadute_impianto = String.valueOf(du_cadute_impianto.isChecked());
                String mdu_altro = String.valueOf(du_altro.isChecked());
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
                params.put("xcdcol", mxcdcol);
                params.put("dataora_verniciatura", mdataora_verniciatura);
                params.put("marca_polvere", mmarca_polvere);
                params.put("cod_agganciatore1", mcod_agganciatore1);
                params.put("cod_agganciatore2", mcod_agganciatore2);
                params.put("cod_agganciatore3", mcod_agganciatore3);
                params.put("cod_verniciatore1", mcod_verniciatore1);
                params.put("cod_verniciatore2", mcod_verniciatore2);
                params.put("cod_verniciatore3", mcod_verniciatore3);
                params.put("difetto_telai", mdifetto_telai);
                params.put("difetto_complementari", mdifetto_complementari);
                params.put("difetto_lamiere", mdifetto_lamiere);
                params.put("difetto_bugne", mdifetto_bugne);
                params.put("difetto_barre", mdifetto_barre);
                params.put("difettoin", mdifettoin);
                params.put("de_profili_errati", mde_profili_errati);
                params.put("de_profili_deformati", mde_profili_deformati);
                params.put("de_profili_ammaccati", mde_profili_ammaccati);
                params.put("de_levigatura", mde_levigatura);
                params.put("de_mancanza_fori", mde_mancanza_fori);
                params.put("du_troppa_polvere", mdu_troppa_polvere);
                params.put("du_bucciato", mdu_bucciato);
                params.put("du_aloni", mdu_aloni);
                params.put("du_scarsa_polvere", mdu_scarsa_polvere);
                params.put("du_impurita", mdu_impurita);
                params.put("du_errore_colore", mdu_errore_colore);
                params.put("du_angolo_interno_non_verniciato", mdu_angolo_interno_non_verniciato);
                params.put("du_cava_telaio_non_verniciata", mdu_cava_telaio_non_verniciata);
                params.put("du_bolle_vernice", mdu_bolle_vernice);
                params.put("du_crateri", mdu_crateri);
                params.put("du_tracce_silicone", mdu_tracce_silicone);
                params.put("du_macchie_acqua", mdu_macchie_acqua);
                params.put("du_occhi_pernice", mdu_occhi_pernice);
                params.put("du_punte_spillo", mdu_punte_spillo);
                params.put("du_graffi", mdu_graffi);
                params.put("du_cadute_impianto", mdu_cadute_impianto);
                params.put("du_altro", mdu_altro);
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

        MySingleton.getInstance(NCI_verniciatura.this).addToRequestque(stringRequest);
    }

    private void getDescrizioneColore() {
        //Showing the progress dialog
        StringRequest stringRequest = new StringRequest(Request.Method.POST, COLORE_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        //Showing toast message of the response
                        if (s.contains(";")){
                            String [] data = s.split(";");
                            xcdcol.setText(data[0]);
                            xdecol.setText(data[1]);
                        } else {
                            xdecol.setText("Colore non trovato.");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        //Showing toast
                        Toast.makeText(NCI_verniciatura.this, "Codice NON trovato. Tel 156.", Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                //Getting Image Name
                String mnumero = numero_lotto.getText().toString().substring(0,6);
                String mlotto = numero_lotto.getText().toString().substring(7,8);

                //Creating parameters
                Map<String, String> params = new Hashtable<String, String>();

                //Adding parameters
                params.put("numero_ordine", mnumero);
                params.put("lotto_ordine", mlotto);

                //returning parameters
                return params;
            }
        };

        MySingleton.getInstance(NCI_verniciatura.this).addToRequestque(stringRequest);
    }
}
