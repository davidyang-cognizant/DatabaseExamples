package com.example.roomsdatabase.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.widget.Toast;

import com.example.roomsdatabase.Person;
import com.example.roomsdatabase.adapter.PersonAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.Query;

public class CustomReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String intentAction = intent.getAction();
        if (intentAction != null) {
            NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            if(info != null && info.isConnected()) {
                // Do your work.
                Toast.makeText(context,"Connected", Toast.LENGTH_SHORT).show();
                // e.g. To check the Network Name or other info:
//                WifiManager wifiManager = (WifiManager)context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
//                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
//                String ssid = wifiInfo.getSSID();
            } else {
                Toast.makeText(context,"Disconnected", Toast.LENGTH_SHORT).show();
            }
            //Display the toast.
        } else {
            Toast.makeText(context, "null", Toast.LENGTH_SHORT).show();
        }
    }
}
