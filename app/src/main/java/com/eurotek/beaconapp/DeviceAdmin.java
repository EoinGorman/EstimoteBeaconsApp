package com.eurotek.beaconapp;

import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

/**
 * Created by El Gormo on 11/10/2016.
 */
public class DeviceAdmin extends DeviceAdminReceiver {

    void showToast(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onEnabled(Context context, Intent intent) {
        showToast(context, "DeviceAdmin: onEnabled");
    }

    @Override
    public CharSequence onDisableRequested(Context context, Intent intent) {
        return "DeviceAdmin: onDisableRequested";
    }

    @Override
    public void onDisabled(Context context, Intent intent) {
        showToast(context, "DeviceAdmin: onDisabled");
    }

    @Override
    public void onPasswordChanged(Context context, Intent intent) {
        showToast(context, "DeviceAdmin: onPasswordChanged");
    }
}