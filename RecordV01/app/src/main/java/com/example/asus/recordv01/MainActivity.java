package com.example.asus.recordv01;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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

    private Context context = this;
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
        clearNotification();
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
                    if (format.equals("1")) {
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
                    else {
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
                    if (format.equals("1")) {
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

                    final EditText editText = new EditText(context);
                    new android.support.v7.app.AlertDialog.Builder(context)
                            .setTitle("Name")
                            .setView(editText)
                            .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    String fileName = editText.getText().toString();
                                    File file = new File(Environment.getExternalStorageDirectory() + "/Recordings/" + fileName + ".amr");
                                    while(file.exists())
                                    {
                                        fileName = fileName + "<" + createTempFileName() + ">";
                                        file = new File(Environment.getExternalStorageDirectory() + "/Recordings/" + fileName + ".amr");
                                    }
                                    AMRRecordUtil.saveRecording(tempFileList,
                                            Environment.getExternalStorageDirectory() + "/Recordings/" + fileName + ".amr");
                                    Toast.makeText(context, "saved successfully", Toast.LENGTH_SHORT).show();

                                }
                            })
                            .setNegativeButton("Give Up", null)
                            .show();
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
    protected void onStop() {
        showNotification();
        super.onStop();
    }

    @Override
    protected void onStart() {
        clearNotification();
        super.onStart();
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

    private void showNotification(){
        // 创建一个NotificationManager的引用
        NotificationManager notificationManager = (NotificationManager)
                this.getSystemService(android.content.Context.NOTIFICATION_SERVICE);

        // 定义Notification的各种属性
        Notification notification =new Notification(R.drawable.icon,
                "Recorder", System.currentTimeMillis());
        //FLAG_AUTO_CANCEL   该通知能被状态栏的清除按钮给清除掉
        //FLAG_NO_CLEAR      该通知不能被状态栏的清除按钮给清除掉
        //FLAG_ONGOING_EVENT 通知放置在正在运行
        //FLAG_INSISTENT     是否一直进行，比如音乐一直播放，知道用户响应
        notification.flags |= Notification.FLAG_ONGOING_EVENT; // 将此通知放到通知栏的"Ongoing"即"正在运行"组中
        notification.flags |= Notification.FLAG_NO_CLEAR; // 表明在点击了通知栏中的"清除通知"后，此通知不清除，经常与FLAG_ONGOING_EVENT一起使用
        notification.flags |= Notification.FLAG_SHOW_LIGHTS;
        //DEFAULT_ALL     使用所有默认值，比如声音，震动，闪屏等等
        //DEFAULT_LIGHTS  使用默认闪光提示
        //DEFAULT_SOUNDS  使用默认提示声音
        //DEFAULT_VIBRATE 使用默认手机震动，需加上<uses-permission android:name="android.permission.VIBRATE" />权限
        notification.defaults = Notification.DEFAULT_LIGHTS;
        //叠加效果常量
        //notification.defaults=Notification.DEFAULT_LIGHTS|Notification.DEFAULT_SOUND;
        notification.ledARGB = Color.BLUE;
        notification.ledOnMS =5000; //闪光时间，毫秒

        // 设置通知的事件消息
        CharSequence contentTitle ="Recorder"; // 通知栏标题
        CharSequence contentText ="Recorder01"; // 通知栏内容
        Intent notificationIntent =new Intent(MainActivity.this, MainActivity.class); // 点击该通知后要跳转的Activity
        PendingIntent contentItent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        notification.setLatestEventInfo(this, contentTitle, contentText, contentItent);

        // 把Notification传递给NotificationManager
        notificationManager.notify(0, notification);
    }

    //删除通知
    private void clearNotification(){
        // 启动后删除之前我们定义的通知
        NotificationManager notificationManager = (NotificationManager) this
                .getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancel(0);

    }
}


