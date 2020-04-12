package com.marconatalini.eurostep;

import android.app.Application;
import android.app.SearchManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.*;
import android.support.design.widget.FloatingActionButton;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.Hashtable;
import java.util.Map;

public class MainActivity extends AppCompatActivity{

    private int SERVER_PORT = 0;
    private String SERVER_IP = "";
    public static String WEBSERVER_IP = "";
    public static String OPERATORE = "", NOME_OPERATORE = "";
    Button[] buttonlist;
    FloatingActionButton btn_login;
    Animation fab_close;
    SocketTask socketTask;

    @Override
    protected void onResume() {
        super.onResume();
        PreferenceManager.setDefaultValues(this, R.xml.pref_general, false);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        SERVER_IP = sharedPref.getString(SettingsActivity.SERVER_IP, "192.168.29.5");
        WEBSERVER_IP = sharedPref.getString(SettingsActivity.WEBSERVER_IP, "192.168.29.100");
        SERVER_PORT = Integer.valueOf(sharedPref.getString(SettingsActivity.SERVER_PORT, "8888"));
        OPERATORE = sharedPref.getString(SettingsActivity.OPERATORE, "");
        //USEVOLLEY = sharedPref.getBoolean(SettingsActivity.USEVOLLEY, false);

        if (!OPERATORE.equals("")) {
            unhide_btn(buttonlist);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        socketTask = new SocketTask(MainActivity.this);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        myToolbar.setLogo(R.mipmap.ic_launcher2);
        myToolbar.setTitle("");
        myToolbar.setSubtitle("by Marco N.");
        setSupportActionBar(myToolbar);

        btn_login = (FloatingActionButton) findViewById(R.id.fab_login);
        fab_close = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.fab_close);
        Button btn_LA = (Button) findViewById(R.id.btn_LA);
        Button btn_vern = (Button) findViewById(R.id.btn_verniciatura);
        Button btn_cianfrinato = (Button) findViewById(R.id.btn_Cianfrinatura);
        Button btn_liste = (Button) findViewById(R.id.btn_liste);
        Button btn_centriLavoro = (Button) findViewById(R.id.btn_CentriLavoro);
        Button btn_problema = (Button) findViewById(R.id.btn_problema);

        buttonlist = new Button[]{btn_LA, btn_cianfrinato, btn_vern, btn_centriLavoro, btn_problema, btn_liste}; //, btn_persiane, btn_imballo};

        SharedPreferences.Editor editPref = PreferenceManager.getDefaultSharedPreferences(this).edit();
        editPref.putString(SettingsActivity.OPERATORE, "");
        editPref.commit();

        btn_LA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (OPERATORE != "") {
                    //Intent openRepartoLA = new Intent(MainActivity.this, LegnoAlluminioActivity.class);
                    Intent openRepartoLA = new Intent(MainActivity.this, LavorazioniActivity.class);
                    startActivity(openRepartoLA);
                }
            }
        });


        btn_liste.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (OPERATORE != "") {
                    Intent openListe = new Intent(MainActivity.this, ListeActivity.class);
                    startActivity(openListe);
                }
            }
        });

        btn_problema.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (OPERATORE != "") {
                    Intent reclamo = new Intent(MainActivity.this, NCIActivity.class);
                    startActivity(reclamo);

                }
            }
        });

        final IntentIntegrator integrator = new IntentIntegrator(this);

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btn_login.startAnimation(fab_close);

                v.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        integrator.setPrompt("Leggi il tuo cartellino");
                        integrator.initiateScan();
                    }
                }, 750);
            }
        });

        SocketTask msgserver = new SocketTask(this);
        msgserver.startServerMsg();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null) {
            if(result.getContents() == null) {
                Toast.makeText(this, "Scansione ANNULLATA", Toast.LENGTH_LONG).show();
            } else {
                String op = result.getContents().substring(2);
                loginOperatore(op);
            }
        } else {
            // This is important, otherwise the result will not be passed to the fragment
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager =
                (SearchManager) getSystemService(Application.SEARCH_SERVICE);
        SearchView searchView =
                (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (query.length() == 8){
                    if (socketTask.checkBarcodeOrdine(query)){
                        return false;
                    }
                }
                Toast.makeText(MainActivity.this,query + " non è valido",Toast.LENGTH_SHORT).show();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent apriSetting = new Intent(getApplicationContext(),SettingsActivity.class);
            startActivity(apriSetting);
        }

        return super.onOptionsItemSelected(item);
    }


    private void unhide_btn (Button[] btnlist){

        //CoordinatorLayout cl = (CoordinatorLayout) findViewById(R.id.fab_layout);
        //cl.setVisibility(View.GONE);

        btn_login.setVisibility(View.INVISIBLE);

        for (Button btn:btnlist){
            //btn.setVisibility(View.VISIBLE);
            btn.setEnabled(true);
        }
    }

    private void loginOperatore(final String cartellino){
        String LOGINAPP_URL ="http://"+ WEBSERVER_IP +"/loginapp.php";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, LOGINAPP_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {

                        if (s.equals("Update")) {
                            Toast.makeText(MainActivity.this, "Chiudi l'applicazione aggiorna",Toast.LENGTH_LONG).show();
                            Uri updatepage = Uri.parse("http://"+ WEBSERVER_IP +"/eurostep/app.apk");
                            Intent update = new Intent(Intent.ACTION_VIEW, updatepage);
                            startActivity(update);
                            finish();
                            return;
                        }

                        if (s.equals(cartellino)) {
                            Toast.makeText(MainActivity.this, s+" NON trovato. Riprova!" , Toast.LENGTH_LONG).show();
                        }else{
                            //Showing toast message of the response
                            Toast.makeText(MainActivity.this, "Benvenuto "+s , Toast.LENGTH_LONG).show();
                            SharedPreferences.Editor editPref = PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit();
                            editPref.putString(SettingsActivity.OPERATORE, cartellino);
                            editPref.commit();
                            OPERATORE = cartellino;
                            NOME_OPERATORE = s;
                            unhide_btn(buttonlist);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        //Showing toast
                        Toast.makeText(MainActivity.this, "Errore server: riprova..", Toast.LENGTH_LONG).show(); //volleyError.getMessage().toString()
                    }
                }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                //Creating parameters
                Map<String,String> params = new Hashtable<String, String>();

                //Adding parameters
                params.put("cartellino", cartellino);
                params.put("appversion", String.valueOf(BuildConfig.VERSION_CODE));

                //returning parameters
                return params;
            }
        };

        MySingleton.getInstance(MainActivity.this).addToRequestque(stringRequest);
    }



}
