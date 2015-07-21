package com.example.asus.recordv01;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.ant.liao.GifView;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by asus on 2015/7/20.
 */
public class PlayingActivity extends ActionBarActivity {
    private ImageView play_pause;
    private ImageView stop;
    private ImageView edit;
    private TextView txtFileName;
    private GifView playing;

    private SeekBar seekbar;
    private Timer mTimer;
    private TimerTask mTimeTask;

    private MediaPlayer player;

    private String fileName;

    private boolean isFirst;        // 是否是第一次点击
    private boolean isPlay;         // 是否播放
    private boolean isChanging;    // 互斥变量，防止定时器与SeekBar拖动时进度冲突

    private Context context = this;
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playrecording);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        fileName = getIntent().getStringExtra("fileName");
        findView();
        initVariable();
        clickPlay_Pause();
        clickStop();
        clickEdit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == android.R.id.home)
        {
            finish();
            return false;
        }
        if(id == R.id.action_delete)
        {
            new AlertDialog.Builder(context).setTitle("Confirm")
                    .setMessage("Do you really want to delete these recordings")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            File file = new File(Environment.getExternalStorageDirectory() + "/Recordings/" + fileName);
                            file.delete();
                            Intent intent = new Intent(PlayingActivity.this,
                                    RecordingListActivity.class);
                            // 1-->delete file need to update the list
                            setResult(RESULT_OK,intent);
                            finish();
                        }})
                    .setNegativeButton("cancel",null)
                    .show();
        }
        return false;
        //return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_playrecordings, menu);
        return true;
    }

    private void findView()
    {
        play_pause = (ImageView) findViewById(R.id.play_pause);
        stop = (ImageView)findViewById(R.id.reset);
        edit = (ImageView)findViewById(R.id.edit);
        seekbar = (SeekBar) findViewById(R.id.seekbar);
        playing = (GifView)findViewById(R.id.playing);
        txtFileName = (TextView)findViewById(R.id.record_name);
    }

    private void initVariable()
    {
        isFirst = true;
        isPlay = false;
        isChanging = false;
        playing.setGifImage(R.drawable.playing);
        playing.setShowDimension(600, 600);
        playing.setGifImageType(GifView.GifImageType.COVER);
        txtFileName.setText(fileName);
        seekbar.setOnSeekBarChangeListener(new MySeekbar());
        context = this;
    }

   // play_pause clickListener
    private void clickPlay_Pause()
    {
        play_pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 是否是首次播放
                if (isFirst) {
                    play_pause.setImageDrawable(getResources().getDrawable(R.drawable.pausebutton));
                    isFirst = false;
                    File file = new File(Environment.getExternalStorageDirectory()
                            + "/Recordings", fileName);
                    if (file.exists()) {
                        player = new MediaPlayer();
                        try {
                            player.setDataSource(file.getAbsolutePath());
                            playerFinish();
                            player.prepare();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        isFirst = true;
                        return;
                    }
                }
                // 暂停状态按下play_pause
                if (!isPlay) {
                    play_pause.setImageDrawable(getResources().getDrawable(R.drawable.pausebutton));
                    isPlay = true;
                    player.start();
                    seekbar.setMax(player.getDuration());
                    mTimer = new Timer();
                    mTimeTask = new TimerTask() {
                        @Override
                        public void run() {
                            if (isChanging == true) {
                                return;
                            }
                            seekbar.setProgress(player.getCurrentPosition());
                        }
                    };
                    mTimer.schedule(mTimeTask, 0, 10);
                    player.start();
                }
                // 播放状态按下pause_play
                else if (isPlay) {
                    play_pause.setImageDrawable(getResources().getDrawable(R.drawable.play1));
                    isPlay = false;
                    player.pause();
                }
            }
        });
    }

    // stop clickListener
    private void clickStop()
    {
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isFirst) {
                    play_pause.setImageDrawable(getResources().getDrawable(R.drawable.play1));
                    isPlay = false;
                    isFirst = false;
                    player.seekTo(0);
                    player.pause();
                }
                else{
                    return;
                }
            }
        });
    }

    // play finish listener
    private void playerFinish()
    {
        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if (!isFirst) {
                    play_pause.setImageDrawable(getResources().getDrawable(R.drawable.play1));
                    isPlay = false;
                    isFirst = false;
                    player.seekTo(0);
                    player.pause();
                }
            }
        });
    }

    // edit clickListener
    private void clickEdit()
    {
        edit.setOnClickListener(new View.OnClickListener() {
            EditText editText = new EditText(context);

            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(context)
                        .setTitle("Name")
                        .setView(editText)
                        .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String newFileName = editText.getText().toString();
                                File oldFile = new File(Environment.getExternalStorageDirectory() + "/Recordings/" + fileName);
                                File newFile = new File(Environment.getExternalStorageDirectory() + "/Recordings/" + newFileName + getFileFormat());
                                oldFile.renameTo(newFile);
                                txtFileName.setText(newFileName);
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        });
    }

    private String getFileFormat()
    {
        return fileName.substring(fileName.length() - 4,fileName.length());
    }

    // 进度条处理
    class MySeekbar implements SeekBar.OnSeekBarChangeListener {
        public void onProgressChanged(SeekBar seekBar, int progress,
                                      boolean fromUser) {
        }

        public void onStartTrackingTouch(SeekBar seekBar) {
            isChanging = true;
        }

        public void onStopTrackingTouch(SeekBar seekBar) {
            player.seekTo(seekbar.getProgress());
            isChanging = false;
        }
    }
}
