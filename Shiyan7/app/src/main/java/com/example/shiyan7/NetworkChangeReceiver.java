package com.example.shiyan7;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

public class NetworkChangeReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager connectionManager = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectionManager.getActiveNetworkInfo();
        boolean isConnected = networkInfo != null &&
                networkInfo.isConnectedOrConnecting();
        if(isConnected){
            Toast.makeText(context, "network is available", Toast.LENGTH_SHORT).show();
            Log.d("NetworkChangeReceiver","network is available");
            boolean isWiFi = networkInfo.getType() == ConnectivityManager.TYPE_WIFI;
            if(isWiFi){
                Log.d("NetworkChangeReceiver","已连接WIFI数据");
            }
            else{
                Log.d("NetworkChangeReceiver","已连接移动数据");
            }
        }
        else{
            Toast.makeText(context, "network is unavailable", Toast.LENGTH_SHORT).show();
            Log.d("NetworkChangeReceiver","网络连接不上！");
        }
    }
}
