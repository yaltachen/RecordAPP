package com.example.asus.recordv01;

import android.app.ActionBar;
import android.content.ClipData;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


public class MainActivity extends ActionBarActivity implements  Runnable{
    ActionBar actionBar;
    private ImageView IVPlayRecording;
    private ImageView IVStartRecording;
    private ImageView IVStopRecording;
    private TextView TVTextView;
    private ActionBar ABRecordingList;

    private Handler handler;
    private TextView TVDate;

    private MediaRecorder mediaRecorder;
    private MediaPlayer mediaPlayer;



    private ArrayList<String> tempFileList;
    private Chronometer timer;
    private String tempUnitedFilePath;

    private boolean isBeginRecording = false;           // flag have started recording = true
    private boolean isRecordingPause = true;            // flag recording = false
    private boolean isBeginPlaying = false;             // flag have started playing = true

    private String format;
    private String maxTime;

    private SettingUtil setting;

    private WAVRecordUtil wavRecordUtil;

    @Override
    protected void onResume() {
        super.onResume();
        setting = new SettingUtil(this);
        maxTime = setting.getMaxHour();
        format = setting.getRecordFormat();
        System.out.println("format: " + format);
        System.out.print("time: " + maxTime );
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        IVPlayRecording = (ImageView) findViewById(R.id.IVPlayRecording);
        IVStartRecording = (ImageView) findViewById(R.id.IVStartRecording);
        IVStopRecording = (ImageView) findViewById(R.id.IVStopRecording);
        TVTextView = (TextView) findViewById(R.id.textView);
        TVDate = (TextView)findViewById(R.id.textDate);

        // initialize
        timer = (Chronometer)this.findViewById(R.id.chronometer);
        this.registerForContextMenu(timer);
        wavRecordUtil = new WAVRecordUtil(getApplicationContext(),true,0);


        // get date
        handler = new Handler() {
            public void handleMessage(Message msg) {
                TVDate.setText((String) msg.obj);
            }
        };

        new Thread(this).start();

        IVStartRecording.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isBeginRecording) {
                    isBeginRecording = true;
                    tempFileList = new ArrayList<String>();         // initialize the temp file list
                    timer.setBase(SystemClock.elapsedRealtime());                //  clear the timer set time to 00:00
                }
                // click button when in pause state
                if (isRecordingPause == true) {
                    mediaRecorder = new MediaRecorder();                        // initialize the mediaRecorder
                    isRecordingPause = false;
                    IVStartRecording.setImageDrawable(getResources().getDrawable(R.drawable.recordpause));
                    // AMR
                    if(format.equals("1")) {
                        String fileDir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/temp";     // the fileDir path
                        File file = new File(fileDir);
                        if (!file.exists())      // check the fileDir is exist
                        {
                            file.mkdir();       // not exist create it
                        }
                        String filePath = fileDir + "/" + createTempFileName() + ".amr";
                        AMRRecordUtil.startRecording(mediaRecorder, filePath);       // start recording
                        tempFileList.add(filePath);
                    }
                    // WAV
                    else{
                        try {
                            wavRecordUtil.startRecord();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    timer.setBase(SystemClock.elapsedRealtime() - convert(timer.getText().toString()));
                    timer.start();                                               //  begin timing
                    TVTextView.setText("recording now...");
                }


                // click button when in recording state
                else {
                    isRecordingPause = true;
                    IVStartRecording.setImageDrawable(getResources().getDrawable(R.drawable.record1));

                    // AMR
                    if(format.equals("1")){
                        AMRRecordUtil.pauseRecording(mediaRecorder);
                    }

                    // WAV
                    else {
                        try {
                            wavRecordUtil.Pause();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    TVTextView.setText("recording pause...");
                    timer.stop();
                }
            }
        });

        IVStopRecording.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isBeginRecording) {
                    return;
                }
                if (!isRecordingPause) {
                    // AMR
                    if(format.equals("1")) {
                        AMRRecordUtil.stopRecording(mediaRecorder);
                        mediaRecorder = null;
                    }
                    // WAV
                    else{
                        try {
                            wavRecordUtil.Pause();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

                // AMR
                if (format.equals("1")) {
                    String fileDir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Recordings";
                    File file = new File(fileDir);
                    if (!file.exists()) {
                        file.mkdir();
                    }

                    String filePath = fileDir + "/" + createTempFileName() + ".amr";
                    tempUnitedFilePath = filePath;
                    AMRRecordUtil.saveRecording(tempFileList, filePath);
                }
                // WAV
                else if (format.equals("2")) {
                    wavRecordUtil.save();
                }

                isBeginRecording = false;
                isRecordingPause = true;
                IVStartRecording.setImageDrawable(getResources().getDrawable(R.drawable.record1));
                timer.setBase(SystemClock.elapsedRealtime());
                timer.stop();
                TVTextView.setText("recording finish...");

            }
        });

        IVPlayRecording.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isBeginPlaying) {
                    AMRPlayUtil.stopPlaying(mediaPlayer);
                    mediaPlayer = null;
                    mediaPlayer = new MediaPlayer();

                    AMRPlayUtil.startPlaying(mediaPlayer, tempUnitedFilePath);
                }
                if(!isBeginPlaying)
                {
                    isBeginPlaying = true;
                    mediaPlayer = new MediaPlayer();
                    AMRPlayUtil.startPlaying(mediaPlayer, tempUnitedFilePath);
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        //add actionbar action
        if (id == R.id.action_list) {
            Intent intent = new Intent(MainActivity.this,RecordingListActivity.class);
            startActivity(intent);
            return true;
        }
        if(id == R.id.action_settings){
            Intent intent = new Intent(MainActivity.this,SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public String createTempFileName()
    {
        SimpleDateFormat sdf = new SimpleDateFormat("hh-mm-ss-mmm");
        String time = sdf.format(new java.util.Date());
        return  time;
    }

    // a thread to get date
    @Override
    public void run() {
        // TODO Auto-generated method stub
        try {
            while(true){
                SimpleDateFormat sdf=new SimpleDateFormat("dd/MM/yyyy");
                String str=sdf.format(new Date());
                handler.sendMessage(handler.obtainMessage(100,str));
                Thread.sleep(1000);
            }
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private long convert(String stoptime)
    {
        long recordingTime = 0;
        int count = 1;
        String tempArray[] = stoptime.split(":");

        for(int i = tempArray.length ; i > 0; i--)
        {
            recordingTime = recordingTime + Long.parseLong(tempArray[i - 1]) * 1000 * count;
            count = count * 60;
        }
        return recordingTime;
    }
}
