package com.rair.android_serial_port;

import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.rairmmd.serialport.ByteUtil;
import com.rairmmd.serialport.OnDataReceiverListener;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import android_serialport_api.SerialPortManager;

/**
 * Created by Rair on 2017/9/25.
 * Email:rairmmd@gmail.com
 * Author:Rair
 */

public class Test extends AppCompatActivity {



    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SerialPortManager machineControl;
        try {
           machineControl = new SerialPortManager("/dev/ttyMT3", 115200);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        boolean openCOM = false;
        Log.d("SerialPort", "openCOM value0 : " + openCOM);
        try {
            openCOM = machineControl.openCOM();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d("SerialPort", "openCOM value2 : " + openCOM);

        if (openCOM) {
            Log.d("SerialPort", " Communication open");
            machineControl.sendCMD(new byte[0x00]);
            machineControl.setOnDataReceiverListener(new OnDataReceiverListener() {
                @Override
                public void onDataReceiver(byte[] buffer, int size) {
                    Log.d("SerialPort","string :- "+ String.valueOf(size));
                    Log.d("SerialPort", ByteUtil.hexBytesToString(buffer));

                    String receivedData = new String(buffer, StandardCharsets.UTF_8);
                    Log.d("SerialPort", "Received data as text: " + receivedData);
                }
            });
        } else {
            Log.d("SerialPort", "Error in Serial Port communication");
        }
    }
}