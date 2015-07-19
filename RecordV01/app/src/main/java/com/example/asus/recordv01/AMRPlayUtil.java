package com.example.asus.recordv01;

import android.media.MediaPlayer;

import java.io.IOException;

/**
 * Created by asus on 2015/7/17.
 */
public class AMRPlayUtil {

    public void AMRPlayUtil(){

    }

    public static void startPlaying(MediaPlayer mediaPlayer,String filePath){
        try{
            mediaPlayer.setDataSource(filePath);
            mediaPlayer.prepare();
        }catch (IOException e)
        {
            e.printStackTrace();
        }
        mediaPlayer.start();
    }


    public static void pausePlaying(MediaPlayer mediaPlayer){
        mediaPlayer.pause();
    }

    public  static void stopPlaying(MediaPlayer mediaPlayer){
        mediaPlayer.stop();
        mediaPlayer.release();
    }

}
