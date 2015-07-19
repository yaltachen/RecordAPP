package com.example.asus.recordv01;

import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.RandomAccess;

/**
 * Created by asus on 2015/7/15.
 */
public class AMRRecordUtil {
    private static final String LOG_TAG = "AudioRecordTest";

    public void AMRRecordUtil()
    {

    }

    /**
     * Task: start recording
     * @param mediaRecorder    object to operator
     * @param filePath          media output path
     */
    public static void startRecording(MediaRecorder mediaRecorder, String filePath)
    {
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
        mediaRecorder.setOutputFile(filePath);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try{
            mediaRecorder.prepare();
        }catch (IOException e){
            Log.e(LOG_TAG, "prepare() failed");
        }

        mediaRecorder.start();
    }

    /**
     * task: pause recording
     * @param mediaRecorder   object to operator
     */
    public static void pauseRecording(MediaRecorder mediaRecorder)
    {
        mediaRecorder.stop();               // stop recording
        mediaRecorder.release();            // release the resource
    }

    /**
     * task:stop recording
     * @param mediaRecorder     object to operator
     */
    public static void stopRecording(MediaRecorder mediaRecorder)
    {
        mediaRecorder.stop();
        mediaRecorder.release();
    }

    /**
     * task:save recording
     * @param fileList        temp file list
     * @param mergeFileName  united file path
     */
    public static void saveRecording(ArrayList<String> fileList,String mergeFileName)
    {
        try {
            RandomAccessFile ra = null;
            FileOutputStream fos = new FileOutputStream(mergeFileName);

            for (int i = 0; i < fileList.size(); i++) {
                ra = new RandomAccessFile(fileList.get(i), "r");
                if (i != 0) {
                    ra.seek(6);
                }
                byte[] buffer = new byte[1024 * 8];
                int len = 0;

                while((len = ra.read(buffer)) != -1){
                    fos.write(buffer,0,len);
                }
                ra.close();
            }

            File[] tempFiles = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/temp").listFiles();

            for(int i = 0; i < tempFiles.length; i++)
            {
                tempFiles[i].delete();
            }

        }catch (IOException e) {

        }
    }

    /**
     * task:delete recording
     * @param filePath  file path
     */
    public static void deleteRecording(String filePath)
    {
        File file = new File(filePath);
        if(file.exists())
        {
            file.delete();
        }
    }
}
