package com.eurotek.beaconapp.estimote;

import android.content.Context;

import java.util.List;

public class ProximityContentManager {

    private static ProximityContentManager instance = null;
    private NearestBeaconManager nearestBeaconManager;

    private Listener listener;
    private static boolean isInitialised;

    private ProximityContentManager() {
    }

    public void Initialise(Context context, List<BeaconID> beaconIDs) {
        isInitialised = true;
        nearestBeaconManager = new NearestBeaconManager(context, beaconIDs);
        nearestBeaconManager.setListener(new NearestBeaconManager.Listener() {
            @Override
            public void onNearestBeaconChanged(BeaconID beaconID) {
                if (listener == null) {
                    return;
                }

                if (beaconID != null) {
                    //
                    listener.onContentChanged(beaconID.getMajor());
                }
                else {
                    listener.onContentChanged(null);
                }
            }
        });
    }

    public static ProximityContentManager getInstance() {
        if(instance == null) {
            instance = new ProximityContentManager();
        }
        return instance;
    }

    public static boolean IsInitialised() {
        return isInitialised;
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public interface Listener {
        void onContentChanged(Object content);
    }

    public void startContentUpdates() {
        nearestBeaconManager.startNearestBeaconUpdates();
    }

    public void stopContentUpdates() {
        nearestBeaconManager.stopNearestBeaconUpdates();
    }

    public void destroy() {
        nearestBeaconManager.destroy();
        isInitialised = false;
    }

    public BeaconID GetNearestBeacon() {
        return nearestBeaconManager.CurrentNearestBeacon();
    }
}
