package com.arvihealthscanner.btScan.java.arvi;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;

import com.arvihealthscanner.R;
import com.arvihealthscanner.R;

public class ArviAudioPlaybacks {

    private static MediaPlayer mediaPlayer;
    private static Context context;
    public static void init(Context con)
    {
        context=con;
    }
    public static  void play(int audio){

        try {
            if(mediaPlayer==null || !mediaPlayer.isPlaying())
            {
                mediaPlayer=MediaPlayer.create(context,audio);
                mediaPlayer.start();
            }
            else
            {

            }
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }
    public static  void forcePlay(int audio){

        try {
            if(mediaPlayer!=null && mediaPlayer.isPlaying())
            {
                mediaPlayer.stop();
            }
            mediaPlayer=MediaPlayer.create(context,audio);
            mediaPlayer.start();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

}
