package com.EoingormanLiveIe.ProximitytestOm7;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.EoingormanLiveIe.ProximitytestOm7.estimote.BeaconID;
import com.EoingormanLiveIe.ProximitytestOm7.estimote.EstimoteCloudBeaconDetails;
import com.EoingormanLiveIe.ProximitytestOm7.estimote.EstimoteCloudBeaconDetailsFactory;
import com.EoingormanLiveIe.ProximitytestOm7.estimote.ProximityContentManager;
import com.estimote.sdk.EstimoteSDK;
import com.estimote.sdk.SystemRequirementsChecker;
import com.estimote.sdk.cloud.model.Color;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//
// Running into any issues? Drop us an email to: contact@estimote.com
//

public class MainActivity extends AppCompatActivity {
    public final static String EXTRA_MESSAGE = "com.example.myfirstapp.MESSAGE";
    private static final String TAG = "MainActivity";
    private static final Map<Color, Integer> BACKGROUND_COLORS = new HashMap<>();

    static {
        BACKGROUND_COLORS.put(Color.ICY_MARSHMALLOW, android.graphics.Color.rgb(109, 170, 199));
        BACKGROUND_COLORS.put(Color.BLUEBERRY_PIE, android.graphics.Color.rgb(98, 84, 158));
        BACKGROUND_COLORS.put(Color.MINT_COCKTAIL, android.graphics.Color.rgb(155, 186, 160));
    }

    private static final int BACKGROUND_COLOR_NEUTRAL = android.graphics.Color.rgb(160, 169, 172);

    private ProximityContentManager proximityContentManager;

    //Our list of items to view
    BeaconXML closestBeacon = new BeaconXML();
    ArrayList<BeaconXML> parsedBeacons = new ArrayList<BeaconXML>();
    List<String> itemStrings = new ArrayList<String>();
    private ListView listView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getSupportActionBar().setTitle(R.string.mainActivityTitle);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorAccent))); // set your desired color

        listView = (ListView) findViewById(R.id.lvItems);

        //Get Beacons Info from xml file
        parsedBeacons = BeaconXMLPullParser.GetBeaconsFromFile(MainActivity.this);
        InitialiseProximityManager();

        Toast.makeText(MainActivity.this,"ON CREATE", Toast.LENGTH_SHORT).show();
    }

    private void InitialiseProximityManager() {

        //Construct BeaconID List
        List<BeaconID> ids = new ArrayList<BeaconID>();
        for(int i = 0; i < parsedBeacons.size(); i++) {
            ids.add(parsedBeacons.get(i).GetID());
        }

        proximityContentManager = new ProximityContentManager(this,
                ids,
                new EstimoteCloudBeaconDetailsFactory());

        proximityContentManager.setListener(new ProximityContentManager.Listener() {
            @Override
            public void onContentChanged(Object content) {
                String text;
                if (content != null) {
                    EstimoteCloudBeaconDetails beaconDetails = (EstimoteCloudBeaconDetails) content;
                    text = "";
                    DisplayList(beaconDetails.getBeaconName());

                } else {
                    text = "No beacons in range.";
                    DisplayList("");
                }
                ((TextView) findViewById(R.id.textView)).setText(text);
            }
        });
        proximityContentManager.startContentUpdates();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!SystemRequirementsChecker.checkWithDefaultDialogs(this)) {
            Log.e(TAG, "Can't scan for beacons, some pre-conditions were not met");
            Log.e(TAG, "Read more about what's required at: http://estimote.github.io/Android-SDK/JavaDocs/com/estimote/sdk/SystemRequirementsChecker.html");
            Log.e(TAG, "If this is fixable, you should see a popup on the app's screen right now, asking to enable what's necessary");
        } else {
            if(proximityContentManager != null) {
                Log.d(TAG, "Starting ProximityContentManager content updates");
                //proximityContentManager.startContentUpdates();
            }
        }
        Toast.makeText(MainActivity.this,"ON RESUME", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if(proximityContentManager != null) {
            Log.d(TAG, "Stopping ProximityContentManager content updates");
            proximityContentManager.stopContentUpdates();
        }
        Toast.makeText(MainActivity.this,"ON PAUSE", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(proximityContentManager != null) {
            proximityContentManager.destroy();
        }
        Toast.makeText(MainActivity.this,"ON DESTROY", Toast.LENGTH_SHORT).show();
    }

    public void DisplayList(String beaconName) {
        //-----------------------------------------------------------------------
        //Find Beacon from list and display it's Item Name list
        for (int i = 0; i < parsedBeacons.size(); i++) {
            if(parsedBeacons.get(i).GetName().equals(beaconName)) {
                closestBeacon = parsedBeacons.get(i);
                //Get List View and Create adapter
                MyAdapter adapter = new MyAdapter(this, closestBeacon.GetItemNames());
                listView.setAdapter(adapter);
                listView.setOnItemClickListener(mMessageClickedHandler);
                return;
            }
        }

        //If Beacon not found...
        //Get List View and Create adapter empty
        MyAdapter adapter = new MyAdapter(this, new ArrayList<String>());
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(mMessageClickedHandler);
    }

    //Function for when item in list is clicked
    private AdapterView.OnItemClickListener mMessageClickedHandler = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView parent, View v, int position, long id) {
            // Do something in response to the click
            String itemName = closestBeacon.GetItemNames().get(position);
            Intent intent = new Intent(getApplicationContext(), DisplayItemInfo.class);
            intent.putExtra(EXTRA_MESSAGE, itemName);
            startActivity(intent);
        }
    };

}
