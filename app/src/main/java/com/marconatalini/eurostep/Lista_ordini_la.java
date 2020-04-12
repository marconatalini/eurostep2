package com.marconatalini.eurostep;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;

public class Lista_ordini_la extends AppCompatActivity {

    RecyclerView recyclerView;
    TextView titolo, totali;
    RecyclerView.Adapter adapter;
    RecyclerView.LayoutManager layoutManager;
    ArrayList<JSONObject> arrayList = new ArrayList<>();
    String json_url, phpquery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_ordini_la);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        recyclerView = (RecyclerView) findViewById(R.id.lista_ordini_la);
        titolo = (TextView) findViewById(R.id.TitoloListaOrdiniLA);
        totali = (TextView) findViewById(R.id.TotaliListaOrdiniLA);

        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            phpquery = extras.getString("phpquery");
            json_url = extras.getString("json_url");
            titolo.setText(extras.getString("titolo_lista"));
        }

        arrayList = getListaOrdiniLA();
        adapter = new Lista_ordini_Adapter(this, arrayList);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_lista_ordini, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.setting_lista) {
            Intent apriSetting = new Intent(getApplicationContext(),SettingsActivity.class);
            startActivity(apriSetting);
        }

        return super.onOptionsItemSelected(item);
    }

    private ArrayList<JSONObject> getListaOrdiniLA() {
        final ArrayList<JSONObject> jsonObjectsList = new ArrayList<>();
        StringRequest jsonArrayRequest = new StringRequest(Request.Method.POST, json_url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        int count = 0;
                        JSONArray jsonArray = null;
                        try {
                            jsonArray = new JSONArray(response);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        int c = 0, s = 0, t = 0;
                        while (count < jsonArray.length()) {

                            try {
                                JSONObject jsonObject = jsonArray.getJSONObject(count);
                                c += jsonObject.getInt("n_cornici");
                                s += jsonObject.getInt("n_complementari");
                                t += jsonObject.getInt("n_tagli");
                                jsonObjectsList.add(jsonObject);
                                count++;
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        totali.setText(c + " cornici | " + s + " pz | " + t + " tagli");
                        adapter.notifyDataSetChanged();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //Toast.makeText(Lista_ordini_la.this, "Errore connessione server... riprova", Toast.LENGTH_SHORT).show();
                Toast.makeText(Lista_ordini_la.this, error.toString(),Toast.LENGTH_LONG).show();
                error.printStackTrace();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                //Creating parameters
                Map<String, String> params = new Hashtable<>();

                //Adding parameters
                params.put("query", phpquery);
                params.put("codice_operatore", MainActivity.OPERATORE);

                //returning parameters
                return params;
            }
        };

        MySingleton.getInstance(Lista_ordini_la.this).addToRequestque(jsonArrayRequest);
        return jsonObjectsList;
    }

}
