package com.example.andrew.cloudscoutproxy;

import android.bluetooth.BluetoothSocket;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ObjectInputStream;

import static java.lang.Thread.*;

public class ProcessConnectionThread extends Thread {
    TextView debugOut;
    BluetoothSocket client;
    public ProcessConnectionThread(TextView debugOut, BluetoothSocket socket)
    {
        this.debugOut = debugOut;
        this.client = socket;
    }

    public void run()
    {

    }
}
