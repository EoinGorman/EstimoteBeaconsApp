package com.eurotek.boibeaconapp;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class SetupActivity extends AppCompatActivity {
    public static String unlockPin = "";
    public static int totalItemsToDownload;
    public final static String DOWNLOAD_PATH  = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/com.eurotek.boibeaconapp/";
    public final static String SETTINGS_XML = "boisettings.xml";
    public final static String FINISHED_FILE = "downloadComplete.txt";
    private int numberOfContentDownloads;
    private int downloadCount;
    public static boolean locked;

    private ArrayList<AsyncTask> threads;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        threads = new ArrayList<>();
        downloadCount = 0;
        numberOfContentDownloads = 0;

        //Set app to screen pinning mode on start up
        DevicePolicyManager mDPM = (DevicePolicyManager)getSystemService(Context.DEVICE_POLICY_SERVICE);
        ComponentName mDeviceAdminSample = new ComponentName(this, DeviceAdmin.class);
        //Check if is device owner, if so, allow this app to lock
        if (mDPM.isDeviceOwnerApp(this.getPackageName())) {
            Log.d("Locking", "isDeviceOwnerApp: YES");
            String[] packages = {this.getPackageName()};
            mDPM.setLockTaskPackages(mDeviceAdminSample, packages);
        } else {
            Log.d("Locking", "isDeviceOwnerApp: NO");
        }

        //If this app is allowed to lock, then lock it
        if (mDPM.isLockTaskPermitted(this.getPackageName()) && !locked) {
            Log.d("Locking", "isLockTaskPermitted: ALLOWED");
            startLockTask();
            locked = true;
        } else {
            Log.d("Locking", "isLockTaskPermitted: NOT ALLOWED");
        }

    }

    @Override
    public void onBackPressed() {
        QuitButton(null);
    }

    @Override
    protected void onPause() {
        super.onPause();
        CancelDownload();
    }

    @Override
    protected void onStop() {
        super.onStop();
        CancelDownload();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CancelDownload();
    }

//Button methods----------------------------------------------

//
// ONLY ALLOW IF A settings.xml file exists on the device, proving that the download occurred at least once
//
    //This button starts the app and the beacon scanning process
    public void StartButton(View view) {
        //Starts the main activity
        Environment.getExternalStorageDirectory();
        if(new File(DOWNLOAD_PATH + SETTINGS_XML).exists()) {
            //If total to download == 0 then double check by re-parsing file
            if(totalItemsToDownload == 0) {
                BeaconXMLPullParser.GetBeaconsFromFile(SetupActivity.this);
            }

            File[] paths = new File(DOWNLOAD_PATH).listFiles();
            //Check to make sure the correct number of files have been downloaded
            if(paths.length == totalItemsToDownload) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }
            else {
                ShowAlert("Files Missing", "Please Refresh Content");
            }
        }
        else {
            ShowAlert("Files Missing", "Please Refresh Content");
        }
    }

    //This button will start the download of all necessary content by reading from an xml file
    public void RefreshContent(View view) {
        if (isNetworkAvailable()) {

            //Cancel all active downloads
            CancelDownload();

            //Delete all previously downloaded content + download directory (I think)
            DeleteRecursive(new File(DOWNLOAD_PATH.substring(0, DOWNLOAD_PATH.length())));

            //Create our download directory, if it does not exist yet
            File downloadDir = new File(DOWNLOAD_PATH.substring(0, DOWNLOAD_PATH.length()));
            if (!downloadDir.exists()) {
                boolean result = downloadDir.mkdirs();
                //getApplicationContext().getDir(DOWNLOAD_PATH.substring(0, DOWNLOAD_PATH.length()-1), MODE_PRIVATE);
            }

            //Start download of all content
            Toast.makeText(SetupActivity.this,"Starting Download...", Toast.LENGTH_SHORT).show();

            ProgressBar progressBar = (ProgressBar)findViewById(R.id.progressBar);
            progressBar.setProgress(0);
            progressBar.setMax(100);
            ProgressBarVisibility(true);

            GetAllContent();
        } else {
            //Display message to connect to internet
            ShowAlert("Internet Connection Required", "Please Connect To The Internet");
        }
    }

    //This button allows the user to quit the app
    public void QuitButton(View view) {
        //Insert code here later to un-lock app from "Kiosk"/"Screen pinning" mode
        if (locked) {
            stopLockTask();
            locked = false;
        }
        finishAffinity();
    }

//Helper methods----------------------------------------------

    private void IncrementDownloadCount() {
        downloadCount++;
        ProgressBar progressBar = (ProgressBar)findViewById(R.id.progressBar);
        progressBar.setProgress((int)(((float) downloadCount / (float)numberOfContentDownloads) * 100) );
    }

    private void CheckIfDownloadsFinished() {
        if(downloadCount == numberOfContentDownloads) {
            File file = new File(DOWNLOAD_PATH + FINISHED_FILE);
            try {
                file.createNewFile();
                ShowAlert("Download Complete!", "Your download has finished successfully.");
            }
            catch (IOException e) {
                ShowAlert("Download Error", "A download error has occured.\nPlease try again.");
            }
            ProgressBarVisibility(false);
        }
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
        SettingsDownloadTask download = new SettingsDownloadTask();
        download.execute();
        threads.add(download);
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
        threads.add(itemsDownloadTask);
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
                    threads.add(imageDownloadTask);

                }
                else if(itemInfo.get(j).x.equals(ItemContentPullParser.KEY_A_SRC)) {
                    //Item is an audio clip
                    ContentDownloadTask audioDownloadTask = new ContentDownloadTask();
                    audioDownloadTask.execute(itemInfo.get(j).y, SetupActivity.DOWNLOAD_PATH + itemList.get(i).replaceAll("\\s", "").toLowerCase() + itemInfo.get(j).x + j +  ".mp3");
                    threads.add(audioDownloadTask);
                }
                else if (itemInfo.get(j).x.equals(ItemContentPullParser.KEY_V_SRC)) {
                    //Item is a video clip
                    ContentDownloadTask videoDownloadTask = new ContentDownloadTask();
                    videoDownloadTask.execute(itemInfo.get(j).y, SetupActivity.DOWNLOAD_PATH + itemList.get(i).replaceAll("\\s", "").toLowerCase() + itemInfo.get(j).x + j +  ".mp4");
                    threads.add(videoDownloadTask);
                }
            }
        }
    }



    public boolean DeleteRecursive(File fileOrDirectory)
    {
        if (fileOrDirectory.isDirectory())
        {
            for (File child : fileOrDirectory.listFiles())
            {
                DeleteRecursive(child);
            }
        }

        if(!fileOrDirectory.isDirectory()) {
            return fileOrDirectory.delete();
        }
        return false;
    }

    public void CancelDownload() {
        for (AsyncTask task : threads) {
            task.cancel(true);
        }
        threads = new ArrayList<>();
        downloadCount = 0;
        numberOfContentDownloads = 0;
    }

    public void ProgressBarVisibility(boolean active) {
        ProgressBar progressBar = (ProgressBar)findViewById(R.id.progressBar);
        ProgressBar progressAnim = (ProgressBar)findViewById(R.id.progressAnimation);

        if(!active) {
            progressBar.setVisibility(View.INVISIBLE);
            progressAnim.setVisibility(View.INVISIBLE);
        }
        else{
            progressBar.setVisibility(View.VISIBLE);
            progressAnim.setVisibility(View.VISIBLE);
        }
    }

    public void ShowAlert(String title, String body) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(title);
        dialog.setMessage(body);
        dialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // continue with delete
            }
        });
        dialog.setIcon(android.R.drawable.ic_dialog_alert);

        AlertDialog alert = dialog.create();
        alert.show();
    }

    public void MakeToast(String message) {
        Toast.makeText(SetupActivity.this, message, Toast.LENGTH_SHORT).show();
    }

//Async Tasks--------------------------------------------------
/*
	 * AsyncTask that will download the xml file for us and store it locally.
	 * After the download is done we'll parse the local file.
*/
private class SettingsDownloadTask extends AsyncTask<Void, Void, Boolean> {

    @Override
    protected Boolean doInBackground(Void... arg0) {
        //Download the file
        try {
            return Downloader.DownloadFromUrl("https://raw.githubusercontent.com/EoinGorman/BeaconProximityApp/master/xml/" + SETTINGS_XML,
                                        new FileOutputStream(DOWNLOAD_PATH.toString() + SETTINGS_XML), getApplicationContext());
        }
        catch (FileNotFoundException e)
        {
            return false;
        }
    }

    @Override
    protected void onPostExecute(Boolean result){
        if(result) {
            //Parse out Item Names for downloading
            GetItems();
        }
        else {
            ProgressBarVisibility(false);
            ShowAlert("Download Error", "A download error has occured.\nPlease try again.");
        }
    }
}

    //Accepts a list of strings as argument, downloads xml files for each string input
    private class ItemsDownloadTask extends AsyncTask<ArrayList<String>, Void, ArrayList<String>> {

        @Override
        protected ArrayList<String> doInBackground(ArrayList<String>... arg0) {
            //Download the file
            try {
                for(int i = 0; i < arg0[0].size(); i++) {
                    if(!Downloader.DownloadFromUrl("https://raw.githubusercontent.com/EoinGorman/BeaconProximityApp/master/xml/Items/" + arg0[0].get(i) + ".xml",
                            new FileOutputStream(DOWNLOAD_PATH.toString() + arg0[0].get(i) + ".xml"), getApplicationContext()))
                    {
                        return null;
                    }
                }

                return arg0[0];
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return null;
            }

        }

        @Override
        protected void onPostExecute(ArrayList<String> result) {
            //Parse out Item Names for downloading
            if (result != null) {
                GetItemContent(result);
            }
            else {
                ProgressBarVisibility(false);
                ShowAlert("Download Error", "A download error has occured.\nPlease try again.");
            }
        }
    }

    private class ContentDownloadTask extends AsyncTask<String, String, Boolean> {

        @Override
        protected Boolean doInBackground(String... arg0) {
            try {
                return Downloader.DownloadFromUrl(arg0[0], new FileOutputStream(arg0[1]), getApplicationContext());
            } catch (FileNotFoundException e) {
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                IncrementDownloadCount();
                CheckIfDownloadsFinished();
            }
            else{
                ProgressBarVisibility(false);
                ShowAlert("Download Error", "A download error has occured.\nPlease try again.");
            }
        }
    }
}
