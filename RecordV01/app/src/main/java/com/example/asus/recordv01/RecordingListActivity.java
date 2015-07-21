package com.example.asus.recordv01;


import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaMetadata;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.preference.DialogPreference;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SearchView;
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
    private ImageView btnGoback;
    private ImageView btnDelete;
    private ImageView btnShare;
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
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        myAdapter = new ListAdapter(mData,this);
        lv.setAdapter(myAdapter);

        btnGoback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            isMulChoic = false;
            for (int i = 0; i < myAdapter.getCount(); i++) {
                if (myAdapter.getIsSelected().get(i)) {
                    myAdapter.getIsSelected().put(i, false);
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
                                        mData.remove(i);
                                        myAdapter.getIsSelected().put(i, false);
                                    }
                                }
                                dataChanged();
                            }
                        })
                        .setNegativeButton("cancel", null)
                        .show();
            }
        });


        btnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent share = new Intent(Intent.ACTION_SEND);
                share.putExtra(share.EXTRA_STREAM, Uri.fromFile(new File(
                        Environment.getExternalStorageDirectory()
                                + "/Recordings/" + "张靓颖 - All Of Me.mp3"
                )));
                share.setType("audio/*");
                share.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(Intent.createChooser(share,"Share Voice Memo"));
//                Intent intent = new Intent(Intent.ACTION_SEND);
//                intent.setType("text/plain");
//                intent.putExtra(Intent.EXTRA_SUBJECT,"分享");
//                intent.putExtra(Intent.EXTRA_TEXT,"text");
//
//                startActivity(Intent.createChooser(intent, getTitle()));
            }
        });


        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                ListAdapter.ViewHolder holder = (ListAdapter.ViewHolder) view.getTag();

                holder.cb.toggle();

                ListAdapter.getIsSelected().put(position, holder.cb.isChecked());

                if (!isMulChoic) {
//                    测试代码
                    Intent intent = new Intent(RecordingListActivity.this, PlayingActivity.class);
                    Object fileName = ((Map<String, Object>) myAdapter.getItem(position)).get("fileName");
                    intent.putExtra("fileName", String.valueOf(fileName));
                    startActivityForResult(intent,0);
//                    可以用的代码
//                    Intent intent = new Intent(RecordingListActivity.this, PlayingActivity.class);
//                    Object fileName = ((Map<String, Object>) myAdapter.getItem(position)).get("fileName");
//                    intent.putExtra("fileName", String.valueOf(fileName));
//                    startActivity(intent);
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
    protected  void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        switch (resultCode){
            case RESULT_OK:
                dataChanged();
                break;
            default:
                break;
        }
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_recordinglist, menu);
        MenuItem searchItem=menu.findItem(R.id.action_search);
        final SearchView searchView=(SearchView) MenuItemCompat.getActionView(searchItem);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String arg0) {
                return true;
            }

            @Override
            public boolean onQueryTextChange(String arg0) {
//                if (actionOperationListener != null) {
//                    actionOperationListener.onSearchFile(arg0)
//              }
                return false;
            }
        });
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
        if(id == android.R.id.home)
        {
            finish();
            return false;
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

    // 初始化数�?
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

    // 数据改变
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
            long length = file.length();//�ļ��ĳ���
            int pos = 6;//���ó�ʼλ��
            int frameCount = 0;//��ʼ֡��
            int packedPos = -1;
            /////////////////////////////////////////////////////
            byte[] datas = new byte[1];//��ʼ���ֵ
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
            duration += frameCount * 20;//֡��*20
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
