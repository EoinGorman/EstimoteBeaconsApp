package com.eurotek.boibeaconapp;

import android.content.Context;
import android.net.Uri;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

/**
 * Created by El Gormo on 26/09/2016.
 */

public class ItemXMLPullParser {
    static final String KEY_NAME = "Name";
    static final String KEY_DESC = "Description";

    public static void DisplayItemFromFile(Context ctx, String fileName, String itemName) {

        //Temp holder for text value while parsing
        String curText = "";
        int contentIndex = 0;   //Index of content not including text e.g. is it image 1 or image 2

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
                        if (tagname.equalsIgnoreCase(ItemContentPullParser.KEY_V_SRC)) {
                            Uri uriFromPath = Uri.fromFile(new File(SetupActivity.DOWNLOAD_PATH + itemName.replaceAll("\\s", "").toLowerCase() + ItemContentPullParser.KEY_V_SRC + contentIndex + ".mp4"));
                            ((DisplayItemInfo)ctx).PlayVideo(uriFromPath);

                            contentIndex++;
                        }
                        else if (tagname.equalsIgnoreCase(ItemContentPullParser.KEY_A_SRC)) {
                            //Consctruct file path and tell activity to play audio
                            Uri uriFromPath = Uri.fromFile(new File(SetupActivity.DOWNLOAD_PATH + itemName.replaceAll("\\s", "").toLowerCase() + ItemContentPullParser.KEY_A_SRC + contentIndex + ".mp3"));
                            ((DisplayItemInfo)ctx).PlayAudio(uriFromPath);

                            contentIndex++;
                        }
                        else if (tagname.equalsIgnoreCase(ItemContentPullParser.KEY_I_SRC)) {
                            //Construct file path and tell activity to display the image
                            Uri uriFromPath = Uri.fromFile(new File(SetupActivity.DOWNLOAD_PATH + itemName.replaceAll("\\s", "").toLowerCase() + ItemContentPullParser.KEY_I_SRC + contentIndex + ".jpg"));
                            ((DisplayItemInfo)ctx).DisplayImage(uriFromPath);

                            contentIndex++;
                        }
                        else if (tagname.equalsIgnoreCase(KEY_NAME)) {
                            ((DisplayItemInfo)ctx).DisplayText(curText, KEY_NAME);
                        }
                        else if (tagname.equalsIgnoreCase(KEY_DESC)) {
                            ((DisplayItemInfo)ctx).DisplayText(curText, KEY_DESC);
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
    }
}
