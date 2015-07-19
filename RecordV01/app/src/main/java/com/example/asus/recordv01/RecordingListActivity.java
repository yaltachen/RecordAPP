package com.example.asus.recordv01;


import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaMetadata;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Environment;
import android.preference.DialogPreference;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class RecordingListActivity extends ActionBarActivity {

    private ListView lv;
    private ListAdapter myAdapter;
//    private ArrayList<String> list;
    private ImageView btnGoback;
    private ImageView btnDelete;
    private ImageView btnShare;
    private int checkNum;
    private boolean isMulChoic;       // is in MulState State
    private Context context;
    private List<Map<String,Object>> mData = new ArrayList<Map<String,Object>>();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recordinglist);
        context = this;
        getView();

        initDate();

        myAdapter = new ListAdapter(mData,this);
        lv.setAdapter(myAdapter);

        btnGoback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isMulChoic = false;
                for (int i = 0; i < myAdapter.getCount(); i++) {
                    if (myAdapter.getIsSelected().get(i)) {
                        myAdapter.getIsSelected().put(i, false);
                        checkNum--;
                    }
                }
                dataChanged();

            }
        });


        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(context).setTitle("Confirm")
                        .setMessage("Do you really want to delete these recordings")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        for (int i = 0; i < myAdapter.getCount(); i++) {
                            if (myAdapter.getIsSelected().get(i)) {
                                Object filePath = ((Map<String, Object>) myAdapter.getItem(i)).get("fileName");
                                File file = new File(Environment.getExternalStorageDirectory() + "/Recordings/" + String.valueOf(filePath));
                                if (file.exists()) {
                                    file.delete();
                                }
                                myAdapter.getIsSelected().put(i, false);
                                mData.remove(i);
                                checkNum--;
                            }
                        }
                        dataChanged();
                    }})
                .setNegativeButton("cancel",null)
                .show();
            }
        });


        btnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_SUBJECT,"鍒嗕韩");
                intent.putExtra(Intent.EXTRA_TEXT,"text");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(Intent.createChooser(intent,getTitle()));

//                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//                intent.setType("audio/amr");
//                intent.setClassName("com.android.soundrecorder",
//                        "com.android.soundrecorder.SoundRecorder");
//                startActivity(Intent.createChooser(intent,getTitle()));
            }
        });


        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                ListAdapter.ViewHolder holder = (ListAdapter.ViewHolder) view.getTag();

                holder.cb.toggle();

                ListAdapter.getIsSelected().put(position, holder.cb.isChecked());

                if(!isMulChoic) {
                    Intent intent = new Intent(RecordingListActivity.this, PlayingRecordPlay.class);
                    startActivity(intent);
                    Object fileName = ((Map<String,Object>)myAdapter.getItem(position)).get("fileName");
                    intent.putExtra("fileName", String.valueOf(fileName));
                    startActivity(intent);
                }
            }
        });

        lv.setOnItemLongClickListener(
                new AdapterView.OnItemLongClickListener() {

                    @Override
                    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                        isMulChoic = true;
                        //btnDelete.callOnClick();
                        dataChanged();
                        return false;
                    }
                });

        dataChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_recordinglist, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void getView()
    {
        btnGoback = (ImageView) findViewById(R.id.btnGoBack);
        btnDelete = (ImageView) findViewById(R.id.btnDelete);
        btnShare = (ImageView) findViewById(R.id.btnShare);
        lv = (ListView) findViewById(R.id.listView);
    }

    // 鍒濆鍖栨暟鎹?
    private void initDate(){
        File fileDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Recordings");
        File[] fileList = fileDir.listFiles();

        for(int i = 0; i < fileList.length; i++)
        {
            Map<String,Object> map = new HashMap<String,Object>();
            map.put("fileName",fileList[i].getName());

            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            System.out.println(fileList[i].getAbsolutePath());
            mmr.setDataSource(fileList[i].getAbsolutePath());

            map.put("recordingTime",mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));

            mData.add(map);
        }

    }

    // 鏁版嵁鏀瑰彉
    private void dataChanged(){
        if(isMulChoic == false)
        {
            // set all buttons invisible
            btnDelete.setVisibility(View.INVISIBLE);
            btnShare.setVisibility(View.INVISIBLE);
            btnGoback.setVisibility(View.INVISIBLE);

            myAdapter.setMulChoic(false);
        }
        else
        {
            btnDelete.setVisibility(View.VISIBLE);
            btnShare.setVisibility(View.VISIBLE);
            btnGoback.setVisibility(View.VISIBLE);
            myAdapter.setMulChoic(true);
        }
        myAdapter.notifyDataSetChanged();
    }

    public static long getAmrDuration(File file) throws IOException {
        long duration = -1;
        int[] packedSize = { 12, 13, 15, 17, 19, 20, 26, 31, 5, 0, 0, 0, 0, 0, 0, 0 };
        RandomAccessFile randomAccessFile = null;
        try {
            randomAccessFile = new RandomAccessFile(file, "rw");
            long length = file.length();//文件的长度
            int pos = 6;//设置初始位置
            int frameCount = 0;//初始帧数
            int packedPos = -1;
            /////////////////////////////////////////////////////
            byte[] datas = new byte[1];//初始数据值
            while (pos <= length) {
                randomAccessFile.seek(pos);
                if (randomAccessFile.read(datas, 0, 1) != 1) {
                    duration = length > 0 ? ((length - 6) / 650) : 0;
                    break;
                }
                packedPos = (datas[0] >> 3) & 0x0F;
                pos += packedSize[packedPos] + 1;
                frameCount++;
            }
            /////////////////////////////////////////////////////
            duration += frameCount * 20;//帧数*20
        } finally {
            if (randomAccessFile != null) {
                randomAccessFile.close();
            }
        }
        return duration;
    }


}

class ViewHolder
{
    TextView tv;
    CheckBox cb;

    public void setCbVisible()
    {
        cb.setVisibility(View.VISIBLE);
    }

    public void setCBInvisible()
    {
        cb.setVisibility(View.INVISIBLE);
    }
}
