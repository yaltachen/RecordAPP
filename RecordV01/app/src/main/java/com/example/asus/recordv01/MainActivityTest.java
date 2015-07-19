package com.example.asus.recordv01;

import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class MainActivityTest extends Activity {
    private Button play_pause, reset;
    private SeekBar seekbar;
    private boolean ifplay = false;
    private MediaPlayer player = null;
    private String musicName = "united10-26-37-026.amr";
    private boolean iffirst = false;
    private Timer mTimer;
    private TimerTask mTimerTask;
    private boolean isChanging=false;//�����������ֹ��ʱ����SeekBar�϶�ʱ���ȳ�ͻ
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_processbar);
        player = new MediaPlayer();
        findViews();// �����
    }

    private void findViews() {
        play_pause = (Button) findViewById(R.id.play_pause);
        reset = (Button) findViewById(R.id.reset);
        play_pause.setOnClickListener(new MyClick());
        reset.setOnClickListener(new MyClick());

        seekbar = (SeekBar) findViewById(R.id.seekbar);
        seekbar.setOnSeekBarChangeListener(new MySeekbar());
    }

    class MyClick implements OnClickListener {
        public void onClick(View v) {
            File file = new File(Environment.getExternalStorageDirectory(),
                    musicName);
            // �ж���û��Ҫ���ŵ��ļ�
            if (file.exists()) {
                switch (v.getId()) {
                    case R.id.play_pause:
                        if (player != null && !ifplay) {
                            play_pause.setText("pause");
                            if (!iffirst) {
                                player.reset();
                                try {
                                    player.setDataSource(file.getAbsolutePath());
                                    player.prepare();// ׼��

                                } catch (IllegalArgumentException e) {
                                    e.printStackTrace();
                                } catch (IllegalStateException e) {
                                    e.printStackTrace();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                seekbar.setMax(player.getDuration());//���ý�����
                                //----------��ʱ����¼���Ž���---------//
                                mTimer = new Timer();
                                mTimerTask = new TimerTask() {
                                    @Override
                                    public void run() {
                                        if(isChanging==true) {
                                            return;
                                        }
                                        seekbar.setProgress(player.getCurrentPosition());
                                    }
                                };
                                mTimer.schedule(mTimerTask, 0, 10);
                                iffirst=true;
                            }
                            player.start();// ��ʼ
                            ifplay = true;
                        } else if (ifplay) {
                            play_pause.setText("continue");
                            player.pause();
                            ifplay = false;
                        }
                        break;
                    case R.id.reset:
                        if (ifplay) {
                            player.seekTo(0);
                        } else {
                            player.reset();
                            try {
                                player.setDataSource(file.getAbsolutePath());
                                player.prepare();// ׼��
                                player.start();// ��ʼ
                            } catch (IllegalArgumentException e) {
                                e.printStackTrace();
                            } catch (IllegalStateException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        break;
                }
            }
        }
    }
    //����������
    class MySeekbar implements OnSeekBarChangeListener {
        public void onProgressChanged(SeekBar seekBar, int progress,
                                      boolean fromUser) {
        }

        public void onStartTrackingTouch(SeekBar seekBar) {
            isChanging=true;
        }

        public void onStopTrackingTouch(SeekBar seekBar) {
            player.seekTo(seekbar.getProgress());
            isChanging=false;
        }

    }
    //���紦��
    protected void onDestroy() {
        if(player != null){
            if(player.isPlaying()){
                player.stop();
            }
            player.release();
        }
        super.onDestroy();
    }

    protected void onPause() {
        if(player != null){
            if(player.isPlaying()){
                player.pause();
            }
        }
        super.onPause();
    }

    protected void onResume() {
        if(player != null){
            if(!player.isPlaying()){
                player.start();
            }
        }
        super.onResume();
    }

}