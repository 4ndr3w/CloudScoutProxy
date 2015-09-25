package com.example.andrew.cloudscoutproxy;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;


public class MainActivity extends ActionBarActivity implements Runnable {

    BluetoothServerSocket server;
    Thread listener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        try {
            if (mBluetoothAdapter == null) {
                new AlertDialog.Builder(this).setTitle("This device does not support bluetooth.").setNeutralButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        finish();
                    }
                }).show();
            } else if (!mBluetoothAdapter.isEnabled()) {
                new AlertDialog.Builder(this).setTitle("Bluetooth is not enabled").setNeutralButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        finish();
                    }
                }).show();
            } else {
                try {
                    server = mBluetoothAdapter.listenUsingRfcommWithServiceRecord("firescoutproxy", UUID.fromString("292a4b6b-baaf-4505-b42b-47552907e707"));
                    listener = new Thread(this);
                    listener.start();
                } catch (IOException e) {
                    new AlertDialog.Builder(this).setTitle("I/O Error").setNeutralButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            finish();
                        }
                    }).show();
                }
            }
        } catch (Exception e) {
            new AlertDialog.Builder(this).setTitle("Error - "+e.toString()).setNeutralButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                    finish();
                }
            }).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if ( id == R.id.push_cached_reports )
        {
            final TextView debugOut = (TextView)findViewById(R.id.textbox);
            Iterator<CachedReport> reports = CachedReport.findAll();
            ArrayList<CloudScoutPushReportsTask.CloudScoutData> packedReports = new ArrayList<CloudScoutPushReportsTask.CloudScoutData>();
            while ( reports.hasNext() )
            {
                CachedReport report = reports.next();
                try {
                    packedReports.add(CloudScoutPushReportsTask.packageData("match", report.getReport()));
                } catch (JSONException e) {
                    System.out.println("Failed to package cached data");
                    e.printStackTrace();
                }
            }

            CloudScoutPushReportsTask.CloudScoutData[] data = new CloudScoutPushReportsTask.CloudScoutData[packedReports.size()];
            data = packedReports.toArray(data);
            new CloudScoutPushReportsTask() {
                protected void onProgressUpdate(String... status) {
                    debugOut.append(status[0]+"\n");
                }
                protected void onPostExecute(Boolean result) {
                    debugOut.append("CloudScout sync finished with "+(result?"success":"fail")+"\n");
                }
            }.execute(data);

        }
        else if ( id == R.id.count_cached_reports )
        {
            long cached = CachedReport.count();
            Toast.makeText(this, "There are "+cached+" cached reports.", Toast.LENGTH_SHORT).show();
        }
        else if ( id == R.id.clear_cached_reports )
        {

            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Delete Cached Reports");
            builder.setMessage("Delete cached reports from this device?");

            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    CachedReport.deleteAll();
                    Toast.makeText(MainActivity.this, "Cache cleared", Toast.LENGTH_SHORT).show();
                }
            });
            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            builder.show();
        }


        return super.onOptionsItemSelected(item);
    }

    @Override
    public void run() {
        final TextView debugOut = (TextView)findViewById(R.id.textbox);
        debugOut.append("Listening...\n");
        while ( !isFinishing() )
        {
            String json = "";
            try {
                BluetoothSocket client = server.accept();
                try {
                    Thread.sleep(1000); // TODO: Fix this crap
                    ObjectInputStream inStream = new ObjectInputStream(client.getInputStream());


                    json = (String)inStream.readObject();
                    client.close();

                    JSONObject data = new JSONObject(json);
                    final JSONArray matches = data.getJSONArray("matches");
                    CloudScoutPushReportsTask.CloudScoutData packed_data = new CloudScoutPushReportsTask.CloudScoutData("match", matches);
                    new CloudScoutPushReportsTask() {
                        protected void onProgressUpdate(String... status) {
                            debugOut.append(status[0]+"\n");
                        }
                        protected void onPostExecute(Boolean result) {
                            debugOut.append("CloudScout sync finished with "+(result?"success":"fail")+"\n");
                            if ( !result )
                            {
                                debugOut.append("Caching report for later...\n");
                                CachedReport report = new CachedReport(matches);
                                report.save();
                            }
                        }
                    }.execute(packed_data);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } catch ( Exception e ) {

            }
        }
    }
}
