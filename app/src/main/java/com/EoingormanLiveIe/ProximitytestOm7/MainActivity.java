package com.EoingormanLiveIe.ProximitytestOm7;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.EoingormanLiveIe.ProximitytestOm7.estimote.BeaconID;
import com.EoingormanLiveIe.ProximitytestOm7.estimote.ProximityContentManager;
import com.estimote.sdk.SystemRequirementsChecker;
import java.util.ArrayList;
import java.util.List;

//
// Running into any issues? Drop us an email to: contact@estimote.com
//

public class MainActivity extends AppCompatActivity {
    public final static String EXTRA_MESSAGE = "com.example.myfirstapp.MESSAGE";
    private static final String TAG = "MainActivity";

    private static final int BACKGROUND_COLOR_NEUTRAL = android.graphics.Color.rgb(160, 169, 172);

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

        //If we haven't made a proximity manager yet
        if(!ProximityContentManager.IsInitialised()) {
            InitialiseProximityManager();
        }

        //Create a runnable that will update the list
        final Handler handler =new Handler();
        handler.post(new Runnable(){
            @Override
            public void run() {
                handler.postDelayed(this, 200);
                if(ProximityContentManager.IsInitialised()) {
                    DisplayList(0);
                }
            }
        });
    }

    private void InitialiseProximityManager() {

        //Construct BeaconID List
        List<BeaconID> ids = new ArrayList<BeaconID>();
        for(int i = 0; i < parsedBeacons.size(); i++) {
            ids.add(parsedBeacons.get(i).GetID());
        }

        ProximityContentManager.getInstance().Initialise(this, ids);

        ProximityContentManager.getInstance().setListener(new ProximityContentManager.Listener() {
            @Override
            public void onContentChanged(Object content) {

                String text;
                if (content != null) {
                    text = "";
                    Log.d("Closest", "" + (int)content);
                    //DisplayList((int)content);

                } else {
                    text = "No beacons in range.";
                    //DisplayList(0);
                }
                //((TextView) findViewById(R.id.textView)).setText(text);
            }
        });
        ProximityContentManager.getInstance().startContentUpdates();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!SystemRequirementsChecker.checkWithDefaultDialogs(this)) {
            Log.e(TAG, "Can't scan for beacons, some pre-conditions were not met");
            Log.e(TAG, "Read more about what's required at: http://estimote.github.io/Android-SDK/JavaDocs/com/estimote/sdk/SystemRequirementsChecker.html");
            Log.e(TAG, "If this is fixable, you should see a popup on the app's screen right now, asking to enable what's necessary");
        } else {
            if(ProximityContentManager.getInstance() != null) {
                Log.d(TAG, "Starting ProximityContentManager content updates");
                //ProximityContentManager.getInstance().startContentUpdates();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if(ProximityContentManager.getInstance() != null) {
            Log.d(TAG, "Stopping ProximityContentManager content updates");
            //ProximityContentManager.getInstance().stopContentUpdates();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(ProximityContentManager.getInstance() != null) {
            //ProximityContentManager.getInstance().destroy();
        }
        Toast.makeText(MainActivity.this,"ON DESTROY", Toast.LENGTH_SHORT).show();
    }

    public void DisplayList(int beaconMajorNum) {
        //-----------------------------------------------------------------------
        //Find Beacon from list and display it's Item Name list
        if (ProximityContentManager.getInstance().GetNearestBeacon() != null)
        {
            int currentClosest = ProximityContentManager.getInstance().GetNearestBeacon().getMajor();

            for (int i = 0; i < parsedBeacons.size(); i++) {
                if (parsedBeacons.get(i).GetID().getMajor() == currentClosest) {
                    closestBeacon = parsedBeacons.get(i);
                    //Get List View and Create adapter
                    MyAdapter adapter = new MyAdapter(this, closestBeacon.GetItemNames());
                    listView.setAdapter(adapter);
                    listView.setOnItemClickListener(mMessageClickedHandler);
                }
            }
        }
        else
        {
            //If Beacon not found...
            //Get List View and Create adapter empty
            MyAdapter adapter = new MyAdapter(this, new ArrayList<String>());
            listView.setAdapter(adapter);
            listView.setOnItemClickListener(mMessageClickedHandler);
        }
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
