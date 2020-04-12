package com.marconatalini.eurostep;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AlertDialog;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * Created by Marco on 28/07/2016.
 */
public class SocketTask {

    Context context;
    private List<String> recordlist = new ArrayList<String>();
    TextView ServerResponse;
    String SERVER_IP;
    Integer SERVER_PORT;
    Boolean USEVOLLEY = false;
    //MyDBtask db;
    String ordine_lotto;

    public void playNotifica() {
        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Ringtone r = RingtoneManager.getRingtone(context, notification);
        r.play();
    }

    public void notifyMsg(String msg){
        android.support.v4.app.NotificationCompat.Builder mBuilder =
                new android.support.v4.app.NotificationCompat.Builder(context)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle("Eurostep")
                        .setContentText(msg);
// Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(context, MainActivity.class);

// The stack builder object will contain an artificial back stack for the
// started Activity.
// This ensures that navigating backward from the Activity leads out of
// your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
// Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(MainActivity.class);
// Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
// mId allows you to update the notification later on.
        mNotificationManager.notify(101, mBuilder.build());
    }

    public void setServerResponse(TextView serverResponse) {
        ServerResponse = serverResponse;
        db_eurostep.EurostepDbHelper mDbHelper = new db_eurostep.EurostepDbHelper(context);
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        Cursor c = db.rawQuery("SELECT count(*) FROM registro",null);
        if (c.moveToFirst()){
            Long res = c.getLong(0);
            serverResponse.setText("Hai " + res + " registrazioni in memoria.");
            if (res>0) {
                playNotifica();
            }
        }
    }

    public SocketTask(Context context) {
        this.context = context;
        PreferenceManager.setDefaultValues(context, R.xml.pref_general, false);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this.context);
        this.SERVER_IP = sharedPref.getString(SettingsActivity.SERVER_IP, "192.168.29.255");
        this.SERVER_PORT = Integer.valueOf(sharedPref.getString(SettingsActivity.SERVER_PORT, "8888"));
    }

    public void manualInput(final String last_lav_cod, final String OPERATORE, final Integer tempo){

        final EditText txtOrdine = new EditText(context);
        new AlertDialog.Builder(context)
                .setTitle("Inserimento manuale")
                .setMessage("Ordine_lotto es.847003_A")
                .setView(txtOrdine)
                .setPositiveButton("Invia", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        ordine_lotto = txtOrdine.getText().toString();
                        Boolean check = checkBarcodeOrdine(ordine_lotto);
                        if (!check) {
                            ServerResponse.setText("Scansione codice ANNULLATA");
                        } else {
                            sendDati(last_lav_cod, ordine_lotto, OPERATORE,tempo);
                        }
                    }
                })

                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                })
                .show();
    }

    public Boolean checkBarcodeOrdine (String bcode){ //No underscore
        if (bcode == null) return Boolean.FALSE;
        Pattern pOrdine = Pattern.compile("^[85]\\d{5}[ _][\\dA-Z]$");
        Matcher matchNumero= pOrdine.matcher(bcode.substring(0,8));
        if (!matchNumero.find()){
            Toast.makeText(context, bcode + " NON valido.",Toast.LENGTH_SHORT).show();
            return Boolean.FALSE;
        } else {
            return Boolean.TRUE;
        }
    }

    public Boolean checkNumeroOrdineBarre (String bcode){
        if (bcode == null) return false;
        if (bcode.length()!= 6) return false;

        Pattern pOrdine = Pattern.compile("^1\\d{5}$");
        Matcher matchNumero = pOrdine.matcher(bcode.substring(0,6));
        if (matchNumero.find()){
            return true;
        }
        return false;
    }

    public Boolean checkCodiceColore(String xcdcol){ //No underscore
        if (xcdcol == null) return Boolean.FALSE;
        Pattern pCodice= Pattern.compile("^[A-Z]{3}\\d{3}$");
        Matcher matchCodice= pCodice.matcher(xcdcol);
        if (!matchCodice.find()){
            Toast.makeText(context, "Codice " + xcdcol + " NON valido.",Toast.LENGTH_SHORT).show();
            return Boolean.FALSE;
        } else {
            return Boolean.TRUE;
        }
    }



    public void sendDati (String cod_lav, String Ordine_Lotto, String operatore, long seconds){
        //String div = ";";
        //String dati = cod_lav + div + Ordine_Lotto + div + operatore + div + seconds;
        String dati = "codice_fase="+cod_lav +";ordine_lotto="+Ordine_Lotto +";codice_operatore="+operatore +";secondi="+seconds;
        ServerResponse.setText("Invio dati " + Ordine_Lotto + " ... attendi.");
        //MyClientTask client = new MyClientTask();
        //recordlist.add(dati);
        //client.execute(recordlist);
        MySocketClientTask client = new MySocketClientTask ();
        client.execute(dati);
    }

    public void sendDati (String cod_lav, String Ordine_Lotto, String operatore, long seconds, String extras){
        //String div = ";";
        //String dati = cod_lav + div + Ordine_Lotto + div + operatore + div + seconds + div + extras;
        String dati = "codice_fase="+cod_lav +";ordine_lotto="+Ordine_Lotto +";codice_operatore="+operatore +";secondi="+seconds +";"+extras;
        ServerResponse.setText("Invio dati " + Ordine_Lotto + " ... attendi.");
        //MyClientTask client = new MyClientTask();
        //recordlist.add(dati);
        //client.execute(recordlist);
        MySocketClientTask client = new MySocketClientTask ();
        client.execute(dati);
    }

    public void startServerMsg () {
        MySocketServerTask mySocketServerTask = new MySocketServerTask();
        Toast.makeText(context,"IP serverMSG : "+ getIpAddress(),Toast.LENGTH_LONG).show();
        mySocketServerTask.execute();
    }

    public void sendFirstDBrec (){
        db_eurostep.EurostepDbHelper mDbHelper = new db_eurostep.EurostepDbHelper(context);
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        MySocketClientTask client = new MySocketClientTask ();

        Cursor c = db.rawQuery("SELECT _id, dati, timestamp FROM registro", null);

        if (c.moveToFirst()) {
            Long id = c.getLong(c.getColumnIndexOrThrow(db_eurostep.registro.COLUMN_NAME_REGISTRAZIONE_ID));
            String str_dati = c.getString(c.getColumnIndexOrThrow(db_eurostep.registro.COLUMN_NAME_DATI));
            String str_time = c.getString(c.getColumnIndexOrThrow(db_eurostep.registro.COLUMN_NAME_TIMESTAMP));
            db.execSQL(String.format("DELETE FROM registro WHERE _id =%d", id));

            if (str_dati.length() > 0) {
                client.execute(str_dati + ";registrato_il=" + str_time);
            }
        }

        db.close();
    }

    private class MySocketClientTask extends AsyncTask<String, Void, Boolean> {

        Socket socket = null;
        String response = "";
        String toSend, ordine_lotto;

        db_eurostep.EurostepDbHelper mDbHelper = new db_eurostep.EurostepDbHelper(context);
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        @Override
        protected Boolean doInBackground(String... params) {
            toSend = params[0];
            String[] dati = toSend.split(";");
            if (dati[1].equals("999999_9")){
                ordine_lotto = "pulizia";
            } else {
                ordine_lotto = dati[1];
            }

            /*try {
                socket = new Socket();
                socket.connect(new InetSocketAddress(SERVER_IP, SERVER_PORT),3000);
                PrintWriter out = new PrintWriter(socket.getOutputStream());
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                int charsRead = 0;
                char[] buffer = new char[100];
                while (true)
                {
                    charsRead = in.read(buffer);
                    String serverMessage = new String(buffer).substring(0, charsRead);
                    if (serverMessage != null) {
                        response += serverMessage.toString();
                        //Log.i("Message from Server", serverMessage.toString());
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }*/

            try {
                socket = new Socket();
                socket.connect(new InetSocketAddress(SERVER_IP, SERVER_PORT),3000);
                PrintWriter out = new PrintWriter(new BufferedWriter(
                        new OutputStreamWriter(socket.getOutputStream())), true);
                out.println(toSend);

                ByteArrayOutputStream byteArrayOutputStream =
                        new ByteArrayOutputStream(100);
                byte[] buffer = new byte[100];

                InputStream is = socket.getInputStream();

                int read = is.read(buffer); //will block if no data return
                while (read != -1) {
                    byteArrayOutputStream.write(buffer, 0, read);
                    response += byteArrayOutputStream.toString("UTF-8");
                    break;
                }

            } catch (UnknownHostException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return Boolean.FALSE;
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return Boolean.FALSE;
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return Boolean.FALSE;
            } finally {
                if (socket != null) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                        return Boolean.FALSE;
                    }
                }
            }
            return Boolean.TRUE;
        }

        @Override
        protected void onPostExecute(Boolean result) {

            if (result) {
                ServerResponse.setText("Dati " +ordine_lotto+ " inviati correttamente");
                sendFirstDBrec();
            } else {
                ContentValues values = new ContentValues();
                values.put(db_eurostep.registro.COLUMN_NAME_DATI, toSend);
                db.insert(db_eurostep.registro.TABLE_NAME, null, values);
                ServerResponse.setText("Dati " +ordine_lotto+ " salvati memoria");
                db.close();
            }
            super.onPostExecute(result);
        }
    }

    private class MySocketServerTask extends AsyncTask<Void,Void,Void> {

        @Override
        protected Void doInBackground(Void... params) {

            Thread socketServerThread = new Thread(new SocketServerThread());
            socketServerThread.start();

            return null;
        }
    }

    private class SocketServerThread extends Thread {

        static final int SocketServerPORT = 8080;
        ServerSocket serverSocket = null;
        int count = 0;

        @Override
        public void run() {
            Socket socket = null;
            DataInputStream dataInputStream = null;
            DataOutputStream dataOutputStream = null;

            try {
                serverSocket = new ServerSocket(SocketServerPORT);

                while (true) {
                    socket = serverSocket.accept();
                    dataInputStream = new DataInputStream(
                            socket.getInputStream());
                    dataOutputStream = new DataOutputStream(
                            socket.getOutputStream());

                    String messageFromClient = "";

                    ByteArrayOutputStream byteArrayOutputStream =
                            new ByteArrayOutputStream(100);
                    byte[] buffer = new byte[100];

                    //If no message sent from client, this code will block the program
                    int read = dataInputStream.read(buffer);
                    while (read != -1) {
                        byteArrayOutputStream.write(buffer, 0, read);
                        messageFromClient += byteArrayOutputStream.toString("UTF-8");
                        break;
                    }

                    playNotifica();
                    notifyMsg(messageFromClient);

                    count++;
                    /*message += "#" + count + " from " + socket.getInetAddress()
                            + ":" + socket.getPort() + "\n"
                            + "Msg from client: " + messageFromClient + "\n";*/

                    String msgReply =  MainActivity.NOME_OPERATORE + " ha ricevuto il tuo #" + count + " msg.";
                    dataOutputStream.writeUTF(msgReply);

                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                final String errMsg = e.toString();

            } finally {
                if (socket != null) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }

                if (dataInputStream != null) {
                    try {
                        dataInputStream.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }

                if (dataOutputStream != null) {
                    try {
                        dataOutputStream.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        }

    }

    private String getIpAddress() {
        String ip = "";
        try {
            Enumeration<NetworkInterface> enumNetworkInterfaces = NetworkInterface
                    .getNetworkInterfaces();
            while (enumNetworkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = enumNetworkInterfaces
                        .nextElement();
                Enumeration<InetAddress> enumInetAddress = networkInterface
                        .getInetAddresses();
                while (enumInetAddress.hasMoreElements()) {
                    InetAddress inetAddress = enumInetAddress.nextElement();

                    if (inetAddress.isSiteLocalAddress()) {
                        ip += "SiteLocalAddress: "
                                + inetAddress.getHostAddress() + "\n";
                    }

                }

            }

        } catch (SocketException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            ip += "Something Wrong! " + e.toString() + "\n";
        }

        return ip;
    }

}
