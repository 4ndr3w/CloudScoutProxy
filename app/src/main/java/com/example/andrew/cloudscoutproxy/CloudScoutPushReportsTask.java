package com.example.andrew.cloudscoutproxy;

import android.os.AsyncTask;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

public class CloudScoutPushReportsTask extends AsyncTask<CloudScoutPushReportsTask.CloudScoutData, String, Boolean> {

    public static class CloudScoutData
    {
        String type;
        JSONArray data;
        public CloudScoutData(String type, JSONArray data)
        {
            this.type = type;
            this.data = data;
        }
    }

    public static CloudScoutData packageData(String type, JSONArray data)
    {
        return new CloudScoutData(type,data);
    }

    @Override
    protected Boolean doInBackground(CloudScoutData... params) {
        for ( CloudScoutData data : params )
        {
            // Create a new HttpClient and Post Header
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost("http://scouting.techfire225.org/appdata/commitMatchScouting");

            try {
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
                nameValuePairs.add(new BasicNameValuePair("json", data.data.toString()));
                nameValuePairs.add(new BasicNameValuePair("type", data.type));
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                publishProgress("Sending request");
                // Execute HTTP Post Request
                HttpResponse response = httpclient.execute(httppost);
                publishProgress("Done");
            }  catch (Exception e) {
                System.out.println(e);
                publishProgress(e.toString());
                return false;
            }
        }


        return true;
    }

}
