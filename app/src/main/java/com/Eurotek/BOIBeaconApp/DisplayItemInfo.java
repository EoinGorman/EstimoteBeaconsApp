package com.eurotek.boibeaconapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.VideoView;

import com.eurotek.boibeaconapp.estimote.ProximityContentManager;

import java.io.FileNotFoundException;
import java.util.ArrayList;

public class DisplayItemInfo extends AppCompatActivity implements MediaPlayer.OnPreparedListener, MediaController.MediaPlayerControl{

    //Add empty variables for potentially different UI's
    ArrayList<View> views;
    MediaPlayer player;
    MediaController mediaController;

    private RelativeLayout layout;
    private String itemName;

    //--Activity methods----------------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.display_item_info);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        layout = (RelativeLayout)findViewById(R.id.relativeLayout);

        views = new ArrayList<View>();

        //Get the name of the item passed through
        Intent intent = getIntent();
        itemName = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);

        getSupportActionBar().setTitle(itemName);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorAccent))); // set your desired color
        getSupportActionBar().setHomeButtonEnabled(true);

        //Parse xml file using item name
        ItemXMLPullParser.DisplayItemFromFile(this, SetupActivity.DOWNLOAD_PATH + itemName + ".xml", itemName);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mediaController != null) {
            mediaController.hide();
            mediaController = null;
        }
        if (player != null) {
            player.stop();
            player.release();
            player = null;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mediaController != null) {
            mediaController.hide();
            mediaController = null;
        }
        if (player != null) {
            player.stop();
            player.release();
            player = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mediaController != null) {
            mediaController.hide();
            mediaController = null;
        }
        if (player != null) {
            player.stop();
            player.release();
            player = null;
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        int eventaction=event.getAction();
        if(mediaController != null) {
            mediaController.show();
        }
        switch(eventaction) {
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                break;
            default:
                break;
        }

        return super.dispatchTouchEvent(event);
    }
    //--------------------------------------------------------------------------------

    public void DisplayText(String text, String textCategory) {
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);

        if(textCategory == ItemXMLPullParser.KEY_NAME) {
            DisplayItemName(text, params);
        }
        else if(textCategory == ItemXMLPullParser.KEY_DESC) {
            DisplayItemDescription(text, params);
        }
    }

    public void DisplayItemName(String text, RelativeLayout.LayoutParams params) {
        TextView textView = new TextView(getApplicationContext());
        textView.setTextSize(40);
        textView.setTextAlignment(ScrollView.TEXT_ALIGNMENT_CENTER);
        textView.setText(text);
        textView.setId(RelativeLayout.generateViewId());

        if(views.size() > 0) {
            params.addRule(RelativeLayout.BELOW, views.get(views.size() - 1).getId());
        }
        params.addRule(RelativeLayout.CENTER_HORIZONTAL);

        textView.setLayoutParams(params);
        textView.setPadding(10,0,10,0);
        textView.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
        textView.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        layout.addView(textView);
        views.add(textView);
    }

    public void DisplayItemDescription(String text, RelativeLayout.LayoutParams params) {
        TextView textView = new TextView(getApplicationContext());
        textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        textView.setText(text);
        textView.setId(RelativeLayout.generateViewId());

        if(views.size() > 0) {
            params.addRule(RelativeLayout.BELOW, views.get(views.size() - 1).getId());
        }
        params.addRule(RelativeLayout.CENTER_HORIZONTAL);

        textView.setLayoutParams(params);
        textView.setPadding(10,0,10,0);
        textView.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
        textView.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        layout.addView(textView);
        views.add(textView);
    }

    public void DisplayImage(Uri filePath) {
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);

        ImageView imageView = new ImageView(getApplicationContext());
        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeStream(this.getContentResolver().openInputStream(filePath));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        imageView.setPadding(10,10,10,10);
        imageView.setId(RelativeLayout.generateViewId());
        imageView.setImageBitmap(bitmap);
        params.addRule(RelativeLayout.CENTER_HORIZONTAL);
        imageView.setAdjustViewBounds(true);
        imageView.setLayoutParams(params);
        imageView.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        layout.addView(imageView);
        views.add(imageView);
    }

    public void PlayAudio(Uri filePath) {
        try {
            player = new MediaPlayer();
            player.setAudioStreamType(AudioManager.STREAM_MUSIC);
            player.setDataSource(getApplicationContext(), filePath);
            player.setOnPreparedListener(this);

            mediaController = new MediaController(this);
            player.prepare();
            player.start();

            setVolumeControlStream(AudioManager.STREAM_MUSIC);
        }
        catch (Exception e) {
            //TODO: handle exception
        }
    }

    public void PlayVideo(Uri filePath) {
        RelativeLayout parentLayout = (RelativeLayout)findViewById(R.id.outerLayout);
        parentLayout.removeView(findViewById(R.id.scrollView));
        layout = parentLayout;

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);

        final VideoView videoView = new VideoView(getApplicationContext());
        videoView.setId(RelativeLayout.generateViewId());
        videoView.setPadding(10,10,10,10);
        params.addRule(RelativeLayout.CENTER_HORIZONTAL);
        params.addRule(RelativeLayout.ALIGN_TOP);

        mediaController = new MediaController(this);
        videoView.setOnPreparedListener(this);
        videoView.setMediaController(mediaController);

        videoView.setVideoURI(filePath);
        layout.addView(videoView);

        videoView.requestFocus();
        videoView.start();
        views.add(videoView);
    }

    //--MediaPlayerControl methods----------------------------------------------------
    public void start() {
        player.start();
    }

    public void pause() {
        player.pause();
    }

    public int getDuration() {
        return player.getDuration();
    }

    public int getCurrentPosition() {
        return player.getCurrentPosition();
    }

    public void seekTo(int i) {
        player.seekTo(i);
    }

    public boolean isPlaying() {
        return player.isPlaying();
    }

    public int getBufferPercentage() {
        return 0;
    }

    public boolean canPause() {
        return true;
    }

    public boolean canSeekBackward() {
        return true;
    }

    public boolean canSeekForward() {
        return true;
    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        if(player != null) {
            mediaController.setMediaPlayer(this);
        }

        mediaController.setAnchorView(findViewById(R.id.outerLayout));

        Handler handler = new Handler();
        handler.post(new Runnable() {
            public void run() {
                mediaController.setEnabled(true);
                mediaController.show();
            }
        });
    }
    //--------------------------------------------------------------------------------
}
