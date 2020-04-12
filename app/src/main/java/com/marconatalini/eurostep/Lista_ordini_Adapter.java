package com.marconatalini.eurostep;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Marco on 22/11/2016.
 */

public class Lista_ordini_Adapter extends RecyclerView.Adapter<Lista_ordini_Adapter.MyViewHolder> {

    private boolean show_finitura, show_cornici, show_pezzi, show_dataora;
    ArrayList<JSONObject> jsonObjectArrayList = new ArrayList<>();
    Context ctx;

    public Lista_ordini_Adapter(Context context, ArrayList<JSONObject> jsonObjectArrayList){

        this.jsonObjectArrayList = jsonObjectArrayList;
        this.ctx = context;
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this.ctx);
        show_finitura = sharedPref.getBoolean(SettingsActivity.SHOW_FINITURA, false);
        show_cornici = sharedPref.getBoolean(SettingsActivity.SHOW_CORNICI, false);
        show_pezzi = sharedPref.getBoolean(SettingsActivity.SHOW_PEZZI, false);
        show_dataora = sharedPref.getBoolean(SettingsActivity.SHOW_DATAORA, false);
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.riga_lista_ordini, parent, false);
        MyViewHolder myViewHolder = new MyViewHolder(view, ctx, jsonObjectArrayList);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        String numero = "", lotto = "", colore = "", dataora="";
        int fine = 0, cornici=0, pezzi=0;
        try {
            numero = jsonObjectArrayList.get(position).getString("numero_ordine");
            lotto = "_" + jsonObjectArrayList.get(position).getString("lotto_ordine");
            colore = jsonObjectArrayList.get(position).getString("finitura");
            fine = jsonObjectArrayList.get(position).getInt("inizio_fine");
            cornici = jsonObjectArrayList.get(position).getInt("n_cornici");
            pezzi = jsonObjectArrayList.get(position).getInt("n_complementari");
            dataora = jsonObjectArrayList.get(position).getString("timestamp");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        holder.numero_lotto.setText(numero + lotto);

        if (this.show_finitura) {
            holder.colore.setText(colore);
            holder.colore.setVisibility(View.VISIBLE);
        }
        if (this.show_cornici) {
            holder.cornici.setText(cornici + " cornici");
            holder.cornici.setVisibility(View.VISIBLE);
        }
        if (this.show_pezzi) {
            holder.cornici.setText(holder.cornici.getText()+ " | " +pezzi + " pz");
            holder.cornici.setVisibility(View.VISIBLE);
        }
        if (this.show_dataora) {
            holder.dataora.setText(dataora);
            holder.dataora.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public int getItemCount() {
        return jsonObjectArrayList.size();
    }


    public static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        TextView numero_lotto, colore, cornici, pezzi, dataora;
        Context ctx;
        ArrayList<JSONObject> jsonObjectArrayList = null;

        public MyViewHolder(View itemView, Context context, ArrayList<JSONObject> list) {
            super(itemView);
            this.ctx = context;
            this.jsonObjectArrayList = list;
            itemView.setOnClickListener(this);
            numero_lotto = (TextView)itemView.findViewById(R.id.numero_lotto);
            colore = (TextView)itemView.findViewById(R.id.colore);
            cornici = (TextView)itemView.findViewById(R.id.n_cornici);
            dataora = (TextView)itemView.findViewById(R.id.data_ora);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            JSONObject ordine = this.jsonObjectArrayList.get(position);
            String numero = "", lotto = "";
            try {
                numero = ordine.getString("numero_ordine");
                lotto = ordine.getString("lotto_ordine");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            Intent intent = new Intent(this.ctx, DettOrdineLaActivity.class);
            intent.setAction(Intent.ACTION_SEARCH);

            intent.putExtra("numero",numero);
            intent.putExtra("lotto",lotto);
            this.ctx.startActivity(intent);
        }
    }

}
