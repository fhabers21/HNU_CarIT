package com.example.felix.carit;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public  class HttpGetRequest  extends AsyncTask<String, Void, String>  {

    private Double mRPM;

    Context mContext;
    public HttpGetRequest(Context context){
        mContext = context;

    }


    public String value;

    String[] params;
    public static final String REQUEST_METHOD = "GET";
    public static final int READ_TIMEOUT = 15000;
    public static final int CONNECTION_TIMEOUT = 15000;

    @Override
    protected String doInBackground(String... params) {

        String stringUrl = params[0];
        String result;
        String inputLine;

        try {
            //Create a URL object holding our url
            URL myUrl = new URL (stringUrl);

            //Create a connection
            HttpURLConnection connection =(HttpURLConnection)
                    myUrl.openConnection();

            connection.setRequestMethod(REQUEST_METHOD);
            connection.setReadTimeout(READ_TIMEOUT);
            connection.setConnectTimeout(CONNECTION_TIMEOUT);

            connection.connect();

            InputStreamReader streamReader = new
                    InputStreamReader(connection.getInputStream());

            //Create a new buffered reader and String Builder
            BufferedReader reader = new BufferedReader(streamReader);
            StringBuilder stringBuilder = new StringBuilder();

            //Check if the line we are reading is not null
            while((inputLine = reader.readLine()) != null){
                stringBuilder.append(inputLine);
            }

            //Close our InputStream and Buffered reader
            reader.close();
            streamReader.close();

            //Set our result equal to our stringBuilder
            result = stringBuilder.toString();
        }
        catch(IOException e){
            e.printStackTrace();
            result = null;
        }

        return result;
    }

    protected void onPostExecute(String result){
        super.onPostExecute(result);
    }
}
