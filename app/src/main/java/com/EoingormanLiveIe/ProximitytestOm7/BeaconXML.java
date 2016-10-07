package com.EoingormanLiveIe.ProximitytestOm7;

import com.EoingormanLiveIe.ProximitytestOm7.estimote.BeaconID;

import java.util.ArrayList;

public class BeaconXML {
    private BeaconID beaconID;
    private String uuid;
    private int majorNumber;
    private int minorNumber;

    private ArrayList<String> itemNames;
    private String beaconName;

    public BeaconXML() {
        this.beaconName = "";
        this.itemNames = new ArrayList<String>();
    }

    public void AddItem(String itemName) {
        itemNames.add(itemName);
    }

    public void ConstructBeaconID() {
        beaconID = new BeaconID(uuid, majorNumber, minorNumber);
    }

    //Set Methods
    public void SetName(String name) {
        beaconName = name;
    }
    public void SetUUID(String UUID) {
        uuid = UUID;
    }
    public void SetMajorNumber(int value) {
        majorNumber = value;
    }
    public void SetMinorNumber(int value) {
        minorNumber = value;
    }
    public void SetItemNames(ArrayList<String> itemNames) {
        this.itemNames = itemNames;
    }

    //Get Methods
    public final String GetName() {
        return beaconName;
    }

    public final BeaconID GetID() {
        return beaconID;
    }

    public final ArrayList<String> GetItemNames() {
        return itemNames;
    }
}
