package com.EoingormanLiveIe.ProximitytestOm7;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.repackaged.retrofit_v1_9_0.retrofit.http.GET;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

import static android.R.attr.path;

public class SetupActivity extends AppCompatActivity {
    public final static String DOWNLOAD_PATH  = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/";
    public final static String BEACONS_XML = "Beacons.xml";
    private int numberOfContentDownloads;
    private int downloadCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        downloadCount = 0;
    }

//Button methods----------------------------------------------

//
// ONLY ALLOW IF A beacons.xml file exists on the device, proving that the download occurred at least once
//
    //This button starts the app and the beacon scanning process
    public void StartButton(View view) {
        //Starts the main activity
        Environment.getExternalStorageDirectory();
        if(new File(DOWNLOAD_PATH + BEACONS_XML).exists()) {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
        }
    }

    //This button will start the download of all necessary content by reading from an xml file
    public void RefreshContent(View view) {
        if(isNetworkAvailable()) {
            //Disable button, re-enable when download completes

            //Start download of all content
            GetAllContent();
        }
        else{
            //Display message to connect to internet
        }
    }

    //This button allows the user to quit the app
    public void QuitButton(View view) {
        //Insert code here later to un-lock app from "Kiosk"/"Screen pinning" mode
        finish();
    }

//Helper methods----------------------------------------------

    private void IncrementDownloadCount() {
        downloadCount++;
    }

    private void CheckIfDownloadsFinished() {
        if(downloadCount == numberOfContentDownloads) {
            ChangeButtonColor( findViewById(R.id.btn_refresh_content), Color.GREEN);
        }
    }

    private void ChangeButtonColor(View view, int color) {
        view.getBackground().setColorFilter(color, PorterDuff.Mode.DARKEN);
    }

    //Helper method to determine if Internet connection is available.
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void GetAllContent() {
        //First we download the list of the beacons we want to use
        BeaconsDownloadTask downloadBeaconList = new BeaconsDownloadTask();
        downloadBeaconList.execute();
    }

    //Using the list of beacons from the xml file, create a list of item names
    private void GetItems() {
        ArrayList<BeaconXML> beacons = BeaconXMLPullParser.GetBeaconsFromFile(SetupActivity.this);

        ArrayList<String> itemList = new ArrayList<String>();
        for(int i = 0; i < beacons.size(); i++) {
            ArrayList<String> beaconItems = beacons.get(i).GetItemNames();
            for(int j = 0; j < beaconItems.size(); j++) {
                itemList.add(beaconItems.get(j));
            }
        }

        //For each item in list, download the corresponding xml file
        ItemsDownloadTask itemsDownloadTask = new ItemsDownloadTask();
        itemsDownloadTask.execute(itemList);
    }

    private void GetItemContent(ArrayList<String> itemList) {

        //Parse items one at a time, return type and link to content source
        for(int i = 0; i < itemList.size(); i++) {

            ArrayList<Tuple<String,String>> itemInfo = ItemContentPullParser.GetContentLinksFromFile(SetupActivity.this, SetupActivity.DOWNLOAD_PATH + itemList.get(i) + ".xml");

            //Count the number of content we need to download
            for(int j = 0; j < itemInfo.size(); j++) {
                if(itemInfo.get(j).x.equals(ItemContentPullParser.KEY_I_SRC)) {
                    numberOfContentDownloads++;
                }
                else if(itemInfo.get(j).x.equals(ItemContentPullParser.KEY_A_SRC)) {
                    numberOfContentDownloads++;
                }
                else if (itemInfo.get(j).x.equals(ItemContentPullParser.KEY_V_SRC)) {
                    numberOfContentDownloads++;
                }
            }

            //Download the content
            for(int j = 0; j < itemInfo.size(); j++) {
                if(itemInfo.get(j).x.equals(ItemContentPullParser.KEY_I_SRC)) {
                    //Item is an image, use async task, pass link to image and file output
                    ContentDownloadTask imageDownloadTask = new ContentDownloadTask();
                    imageDownloadTask.execute(itemInfo.get(j).y, SetupActivity.DOWNLOAD_PATH + itemList.get(i).replaceAll("\\s", "").toLowerCase() + itemInfo.get(j).x + j +  ".jpg");
                }
                else if(itemInfo.get(j).x.equals(ItemContentPullParser.KEY_A_SRC)) {
                    //Item is an audio clip
                    ContentDownloadTask audioDownloadTask = new ContentDownloadTask();
                    audioDownloadTask.execute(itemInfo.get(j).y, SetupActivity.DOWNLOAD_PATH + itemList.get(i).replaceAll("\\s", "").toLowerCase() + itemInfo.get(j).x + j +  ".mp3");
                }
                else if (itemInfo.get(j).x.equals(ItemContentPullParser.KEY_V_SRC)) {
                    //Item is a video clip
                    ContentDownloadTask audioDownloadTask = new ContentDownloadTask();
                    audioDownloadTask.execute(itemInfo.get(j).y, SetupActivity.DOWNLOAD_PATH + itemList.get(i).replaceAll("\\s", "").toLowerCase() + itemInfo.get(j).x + j +  ".mp4");
                }
            }
        }
    }

//Async Tasks--------------------------------------------------
/*
	 * AsyncTask that will download the xml file for us and store it locally.
	 * After the download is done we'll parse the local file.
*/
private class BeaconsDownloadTask extends AsyncTask<Void, Void, Void> {

    @Override
    protected Void doInBackground(Void... arg0) {
        //Download the file
        try {
            Downloader.DownloadFromUrl("https://raw.githubusercontent.com/EoinGorman/BeaconProximityApp/master/xml/beacons.xml",
                                        new FileOutputStream(DOWNLOAD_PATH.toString() + BEACONS_XML));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void result){
        //Parse out Item Names for downloading
        ChangeButtonColor(findViewById(R.id.btn_refresh_content), Color.BLUE);
        GetItems();
    }
}

    //Accepts a list of strings as argument, downloads xml files for each string input
    private class ItemsDownloadTask extends AsyncTask<ArrayList<String>, Void, ArrayList<String>> {

        @Override
        protected ArrayList<String> doInBackground(ArrayList<String>... arg0) {
            //Download the file
            try {
                for(int i = 0; i < arg0[0].size(); i++) {
                    Downloader.DownloadFromUrl("https://raw.githubusercontent.com/EoinGorman/BeaconProximityApp/master/xml/Items/" + arg0[0].get(i) + ".xml",
                            new FileOutputStream(DOWNLOAD_PATH.toString() + arg0[0].get(i) + ".xml"));
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            return arg0[0];
        }

        @Override
        protected void onPostExecute(ArrayList<String> result){
            //Parse out Item Names for downloading
            ChangeButtonColor(findViewById(R.id.btn_refresh_content), Color.RED);
            GetItemContent(result);
        }
    }

    private class ContentDownloadTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... arg0) {
            try {
                Downloader.DownloadFromUrl(arg0[0], new FileOutputStream(arg0[1]));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result){
            IncrementDownloadCount();
            CheckIfDownloadsFinished();
        }
    }
}
