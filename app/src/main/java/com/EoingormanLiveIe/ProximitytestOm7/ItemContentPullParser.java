package com.EoingormanLiveIe.ProximitytestOm7;

import android.app.Activity;
import android.content.Context;
import android.media.MediaPlayer;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Created by El Gormo on 01/10/2016.
 */

public class ItemContentPullParser {
    static final String KEY_V_SRC = "VideoSource";
    static final String KEY_A_SRC = "AudioSource";
    static final String KEY_I_SRC = "ImageSource";

    public static ArrayList<Tuple<String, String>> GetContentLinksFromFile(Context ctx, String fileName) {

        //Temp holder for text value while parsing
        String curText = "";
        ArrayList<Tuple<String, String>> itemInfo = new ArrayList<Tuple<String, String>>();

        try {
            //Get our factory instance and use it to create a pull parser
            XmlPullParserFactory pullParserFactory = XmlPullParserFactory.newInstance();
            XmlPullParser xpp = pullParserFactory.newPullParser();

            //Open InputStream and reader of our file
            FileInputStream fis = new FileInputStream(new File(fileName));
            BufferedReader reader = new BufferedReader(new InputStreamReader(fis));

            //Point the parser to our file
            xpp.setInput(reader);

            //Get initial event type
            int eventType = xpp.getEventType();

            //Loop to end of document
            while(eventType != XmlPullParser.END_DOCUMENT) {
                String tagname = xpp.getName();

                // React to different event types appropriately
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        break;

                    case XmlPullParser.TEXT:
                        //grab the current text so we can use it in END_TAG event
                        curText = xpp.getText();
                        break;

                    case XmlPullParser.END_TAG:
                        if (tagname.equalsIgnoreCase(KEY_V_SRC)) {
                            itemInfo.add(new Tuple<String, String>(KEY_V_SRC, curText));
                        }
                        else if (tagname.equalsIgnoreCase(KEY_A_SRC)) {
                            itemInfo.add(new Tuple<String, String>(KEY_A_SRC, curText));
                        }
                        else if (tagname.equalsIgnoreCase(KEY_I_SRC)) {
                            itemInfo.add(new Tuple<String, String>(KEY_I_SRC, curText));
                        }
                        break;

                    default:
                        break;
                }
                //move on to next iteration
                eventType = xpp.next();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return itemInfo;
    }
}
