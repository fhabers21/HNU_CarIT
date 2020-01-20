package com.example.felix.carit;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cardiomood.android.controls.gauge.SpeedometerGauge;

import java.util.ArrayList;

public class dieselFragment extends Fragment {

    private SpeedometerGauge speedometer;
    private Button mButton_Connect;
    private Button mButton_Disconnect;
    private ListView mDeviceList;
    private TextView mBluetoothStatus;
    private TextView mTextAnzParcels;


    public Double rpm;
    private TextView Test;

    private long mAnzParcels = 0L;

    //for Bluetooth-connection
    private BTManager mBTManager;
    private BTMsgHandler mBTHandler; // Our main handler that will receive callback notifications

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v =  inflater.inflate(R.layout.fragment_diesel, container, false);



        mButton_Connect = (Button) v.findViewById(R.id.button_connect2);
        mButton_Disconnect = (Button) v.findViewById(R.id.button_disconnect2);

        mDeviceList = (ListView) v.findViewById(R.id.listViewDiesel);
        mBluetoothStatus = (TextView) v.findViewById(R.id.bluetoothStatus2);
        mTextAnzParcels = (TextView) v.findViewById(R.id.text_anzParcelsDiesel);
        Test = (TextView) v.findViewById(R.id.textViewTest);


        setConnectButtons(false);


        // Customize SpeedometerGauge
        speedometer = (SpeedometerGauge) v.findViewById(R.id.speedometerDiesel);


        // Add label converter
        speedometer.setLabelConverter(new SpeedometerGauge.LabelConverter() {
            @Override
            public String getLabelFor(double progress, double maxProgress) {
                return String.valueOf((int) Math.round(progress));
            }
        });

        mBTHandler = new BTMsgHandler() {
            @Override
            void receiveMessage(String msg) {
                mBluetoothStatus.setText(msg);
                mAnzParcels++;
                mTextAnzParcels.setText(mAnzParcels + "");
                Test.setText(msg);


                try {
                    rpm = Double.parseDouble(msg);
                    speedometer.setSpeed(rpm, 1000, 300);
                    new HttpGetRequest(getActivity()).execute("https://api.thingspeak.com/update?api_key=JVD1LFP9IKRNA3Y6&field1=" + rpm.toString());

                } catch (Exception e) {

                }

            }

            @Override
            void receiveConnectStatus(boolean isConnected) {

                if (isConnected) {
                    mBluetoothStatus.setText("0");
                } else {
                    mBluetoothStatus.setText("1");
                }
                setConnectButtons(isConnected);
            }

            @Override
            void handleException(Exception e) {
                mBluetoothStatus.setText("0");
                setConnectButtons(false);
                Toast.makeText(getActivity().getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            }
        };


        //create a new BTManager to handle the connections to BTdevices
        try {
            mBTManager = new BTManager((AppCompatActivity) getActivity(), mBTHandler);
        } catch (Exception e) {
            Toast.makeText(getActivity().getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
        }


        //disconnect the BT-device
        mButton_Disconnect.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (mBTManager != null) {
                    mBTManager.cancel();
                    setConnectButtons(false);
                }
            }
        });

        //click event to show the paired devices
        mButton_Connect.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                pairedDevicesList();
            }
        });


        //we don't want see the keyboard emulator on start
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);


        // configure value range and ticks
        speedometer.setMaxSpeed(6000);
        speedometer.setMajorTickStep(1000);
        speedometer.setMinorTicks(250);
        speedometer.setLabelTextSize(25);
        speedometer.addColoredRange(0, 2000, Color.GREEN);
        speedometer.addColoredRange(2000, 3500, Color.YELLOW);
        speedometer.addColoredRange(3500, 6000, Color.RED);



        return v;
    }


    private void pairedDevicesList() {
        mDeviceList.setVisibility(View.VISIBLE);

        ArrayList list = mBTManager.getPairedDeviceInfos();

        if (list.size() > 0) {
            final ArrayAdapter adapter = new ArrayAdapter(getActivity(), android.R.layout.simple_list_item_1, list);
            mDeviceList.setAdapter(adapter);
            mDeviceList.setOnItemClickListener(myListClickListener); //Method called when the device from the list is clicked
        } else {
            Toast.makeText(getActivity().getApplicationContext(), "No Paired Bluetooth Devices Found.", Toast.LENGTH_LONG).show();
        }
    }

    //reaction after selection of an BT-Device to connect
    private AdapterView.OnItemClickListener myListClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView av, View v, int arg2, long arg3) {
            // Get the device MAC address, the last 17 chars in the View
            String info = ((TextView) v).getText().toString();
            final String address = info.substring(info.length() - 17);

            mDeviceList.setVisibility(View.INVISIBLE);
            mBTManager.connect(address);
        }
    };

    //adapt GUI corresponding to connect status
    private void setConnectButtons(boolean isConnected) {
        mButton_Disconnect.setEnabled(isConnected);
        mButton_Connect.setEnabled(!isConnected);

        if (isConnected == false) {
            mBluetoothStatus.setText("0");
            mAnzParcels = 0L;
            mTextAnzParcels.setText(mAnzParcels + "");
        }

    }

}



