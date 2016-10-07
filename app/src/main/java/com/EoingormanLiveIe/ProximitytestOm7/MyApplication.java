package com.EoingormanLiveIe.ProximitytestOm7;

import android.app.Application;

import com.estimote.sdk.EstimoteSDK;

//
// Running into any issues? Drop us an email to: contact@estimote.com
//

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        //Enables communication with Estimote Cloud API
        //EstimoteSDK.initialize(getApplicationContext(), "proximitytest-om7", "d5f5b660bc2b8130ce9626cda4089e9a");

        // uncomment to enable debug-level logging
        // it's usually only a good idea when troubleshooting issues with the Estimote SDK
        //EstimoteSDK.enableDebugLogging(true);
    }
}
