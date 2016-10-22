package com.eurotek.beaconapp;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import android.content.Context;

public class BeaconXMLPullParser {

    static final String KEY_DOWNLOAD_COUNT = "DownloadCount";
    static final String KEY_PIN = "UnlockPin";
    static final String KEY_BEACON = "Beacon";
    static final String KEY_NAME = "Name";
    static final String KEY_UUID = "UUID";
    static final String KEY_MAJOR_NUMBER = "MajorNumber";
    static final String KEY_MINOR_NUMBER = "MinorNumber";
    static final String KEY_ITEM = "Item";
    //static final String KEY_IMAGE_URL = "image";

    public static ArrayList<BeaconXML> GetBeaconsFromFile(Context ctx) {

        // List of Beacons that we will return
        ArrayList<BeaconXML> beacons;
        beacons = new ArrayList<BeaconXML>();

        // temp holder for current Beacon while parsing
        BeaconXML curBeacon = null;
        // temp holder for current text value while parsing
        String curText = "";

        try {
            // Get our factory and PullParser
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser xpp = factory.newPullParser();

            // Open up InputStream and Reader of our file.
            FileInputStream fis = new FileInputStream(new File(SetupActivity.DOWNLOAD_PATH + SetupActivity.SETTINGS_XML));
            BufferedReader reader = new BufferedReader(new InputStreamReader(fis));

            // point the parser to our file.
            xpp.setInput(reader);

            // get initial eventType
            int eventType = xpp.getEventType();

            // Loop through pull events until we reach END_DOCUMENT
            while (eventType != XmlPullParser.END_DOCUMENT) {
                // Get the current tag
                String tagname = xpp.getName();

                // React to different event types appropriately
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        if (tagname.equalsIgnoreCase(KEY_BEACON)) {
                            // If we are starting a new <Beacon> block we need
                            //a new BeaconXML object to represent it
                            curBeacon = new BeaconXML();
                        }
                        break;

                    case XmlPullParser.TEXT:
                        //grab the current text so we can use it in END_TAG event
                        curText = xpp.getText();
                        break;

                    case XmlPullParser.END_TAG:
                        if(tagname.equalsIgnoreCase(KEY_DOWNLOAD_COUNT)) {
                            SetupActivity.totalItemsToDownload = Integer.parseInt(curText) + 1;//The +1 is for the file we create at end of downloads to confirm download finished
                        }
                        else if(tagname.equalsIgnoreCase(KEY_PIN)) {
                            SetupActivity.unlockPin = curText;
                        }
                        else if (tagname.equalsIgnoreCase(KEY_BEACON)) {
                            // if </Beacon> then we are done with current Beacon
                            //make it construct its BeaconID object
                            curBeacon.ConstructBeaconID();
                            // add it to the list.
                            beacons.add(curBeacon);
                        } else if (tagname.equalsIgnoreCase(KEY_NAME)) {
                            // if </Name> use SetName() on curBeacon
                            curBeacon.SetName(curText);
                        } else if (tagname.equalsIgnoreCase(KEY_UUID)) {
                            // if </UUID> use SetUUID() on curBeacon
                            curBeacon.SetUUID(curText);
                        } else if (tagname.equalsIgnoreCase(KEY_MAJOR_NUMBER)) {
                            // if </MajorNumber> use SetMajorNumber() on curBeacon
                            curBeacon.SetMajorNumber(Integer.parseInt(curText));
                        } else if (tagname.equalsIgnoreCase(KEY_MINOR_NUMBER)) {
                            // if </MinorNumber> use SetMinorNumber() on curBeacon
                            curBeacon.SetMinorNumber(Integer.parseInt(curText));
                        } else if (tagname.equalsIgnoreCase(KEY_ITEM)) {
                            // if </Item> use AddItem() on curBeacon
                            curBeacon.AddItem(curText);
                        }
                        break;

                    default:
                        break;
                }
                //move on to next iteration
                eventType = xpp.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // return the populated list.
        return beacons;
    }
}