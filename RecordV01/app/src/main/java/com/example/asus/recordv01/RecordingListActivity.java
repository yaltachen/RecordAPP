package com.example.asus.recordv01;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;


public class RecordingListActivity extends ActionBarActivity {

    private ListView lv;
    private ListAdapter myAdapter;
    private ImageView btnGoback;
    private ImageView btnDelete;
    private ImageView btnShare;
    private boolean isMulChoic;       // is in MulState State
    private Context context;
    private ArrayList<Map<String,Object>> mData = new ArrayList<Map<String,Object>>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recordinglist);
        context = this;
        getView();
        initData();
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
                new AlertDialog.Builder(context)
                        .setCancelable(false)
                        .setTitle("Confirm")
                        .setMessage("Do you really want to delete these recordings")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                List<File> deleteList = new ArrayList<File>();
                                for (int i = 0; i < myAdapter.getCount(); i++) {
                                    if (myAdapter.getIsSelected().get(i)) {
                                        Object filePath = ((Map<String, Object>) myAdapter.getItem(i)).get("fileName");
                                        File file = new File(Environment.getExternalStorageDirectory() + "/Recordings/" + String.valueOf(filePath));
                                        deleteList.add(file);
                                        myAdapter.getIsSelected().put(i, false);
                                    }
                                }
                                for(int i = 0; i < deleteList.size(); i++)
                                {
                                    File tempfile = deleteList.get(i);
                                    if(tempfile.exists())
                                    {
                                        tempfile.delete();
                                    }
                                }
                                initData();
                                myAdapter = new ListAdapter(mData,context);
                                lv.setAdapter(myAdapter);
                                myAdapter.notifyDataSetChanged();
                                isMulChoic = false;
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
                ArrayList<Uri> uriList = new ArrayList<Uri>();
                for (int i = 0; i < myAdapter.getCount(); i++) {
                    if (myAdapter.getIsSelected().get(i)) {
                        Object filePath = ((Map<String, Object>) myAdapter.getItem(i)).get("fileName");
                        File file = new File(Environment.getExternalStorageDirectory() + "/Recordings/" + String.valueOf(filePath));
                        uriList.add(Uri.fromFile(file));
                    }
                }
                boolean multiple = uriList.size() > 1;
                Intent intent = new Intent(multiple ? android.content.Intent.ACTION_SEND_MULTIPLE
                        : android.content.Intent.ACTION_SEND);
                intent.setType("*/*");
                if(multiple){
                    intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uriList);
                }else{
                    intent.putExtra(Intent.EXTRA_STREAM, uriList.get(0));
                }
                startActivity(Intent.createChooser(intent, "Share"));
            }
        });

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ListAdapter.ViewHolder holder = (ListAdapter.ViewHolder) view.getTag();
                holder.cb.toggle();
                ListAdapter.getIsSelected().put(position, holder.cb.isChecked());
                if (!isMulChoic) {
                    Intent intent = new Intent(RecordingListActivity.this, PlayingActivity.class);
                    Object fileName = ((Map<String, Object>) myAdapter.getItem(position)).get("fileName");
                    intent.putExtra("fileName", String.valueOf(fileName));
                    startActivityForResult(intent,0);
                }else if(isMulChoic){
                    isMulChoic = false;
                   for(int i = 0; i < myAdapter.getCount(); i ++)
                   {
                       if(myAdapter.getIsSelected().get(i))
                       {
                           isMulChoic = true;
                           break;
                       }
                   }
                   dataChanged();
                }
            }
        });

        lv.setOnItemLongClickListener(
                new AdapterView.OnItemLongClickListener() {

                    @Override
                    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                        isMulChoic = true;
                        myAdapter.getIsSelected().put(position, false);
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
                initData();
                myAdapter = new ListAdapter(mData,context);
                lv.setAdapter(myAdapter);
                myAdapter.notifyDataSetChanged();
                break;
            case 2:
                initData();
                myAdapter = new ListAdapter(mData,context);
                lv.setAdapter(myAdapter);
                myAdapter.notifyDataSetChanged();
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
                initData(arg0);
                myAdapter = new ListAdapter(mData, context);
                lv.setAdapter(myAdapter);
                myAdapter.notifyDataSetChanged();
                return false;
            }
            @Override
            public boolean onQueryTextChange(String arg0) {
                initData(arg0);
                myAdapter = new ListAdapter(mData, context);
                lv.setAdapter(myAdapter);
                myAdapter.notifyDataSetChanged();
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

    // init the data
    private void initData() {
        initData("");
    }

    private void initData(String keyWord){
        File fileDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Recordings");
        File[] fileList = fileDir.listFiles();

        if(mData!=null) {
            mData.clear();
        } else {
            mData = new ArrayList<Map<String,Object>>();
        }
        for(int i = 0; i < fileList.length; i++)
        {
            Map<String,Object> map = new HashMap<String,Object>();
            map.put("fileName", fileList[i].getName());

            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            mmr.setDataSource(fileList[i].getAbsolutePath());
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            map.put("recordingTime",getHMSTime(Integer.parseInt(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION))));
            map.put("recordingDate",sdf.format(new Date(fileList[i].lastModified())));
            if(fileList[i].getName().contains(keyWord)||keyWord.equals(""))
                mData.add(map);
            Collections.sort(mData, new Comparator<Map<String,Object>>() {
                @Override
                public int compare(Map<String,Object> map1,Map<String,Object> map2) {
                    DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                    Date date1 = new Date();
                    Date date2 = new Date();
                    try {
                        date1 = df.parse(String.valueOf(map1.get("recordingDate")));
                        date2 = df.parse(String.valueOf(map2.get("recordingDate")));
                    }catch (Exception e)
                    {}
                    if(date1.after(date2))
                    {
                        return -1;
                    }
                    else {
                        return 1;
                    }
                }
            });
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
            //invalidateOptionsMenu();
        }
        else
        {
            btnDelete.setVisibility(View.VISIBLE);
            btnShare.setVisibility(View.VISIBLE);
            btnGoback.setVisibility(View.VISIBLE);
            myAdapter.setMulChoic(true);
            //invalidateOptionsMenu();
        }
        myAdapter.notifyDataSetChanged();
    }

    public  String getHMSTime(int ms)
    {
        String result = String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(ms),
                TimeUnit.MILLISECONDS.toMinutes(ms) % TimeUnit.HOURS.toMinutes(1),
                TimeUnit.MILLISECONDS.toSeconds(ms) % TimeUnit.MINUTES.toSeconds(1));
        return result;
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

    private String FormatFileSize(long fileS) {
        DecimalFormat df = new DecimalFormat("#.00");
        String fileSizeString = "";
        String wrongSize = "0B";
        if (fileS == 0) {
            return wrongSize;
        }
        if (fileS < 1024) {
            fileSizeString = df.format((double) fileS) + "B";
        } else if (fileS < 1048576) {
            fileSizeString = df.format((double) fileS / 1024) + "KB";
        } else if (fileS < 1073741824) {
            fileSizeString = df.format((double) fileS / 1048576) + "MB";
        } else {
            fileSizeString = df.format((double) fileS / 1073741824) + "GB";
        }
        return fileSizeString;
    }
}

class ViewHolder
{
    TextView tv;
    TextView tv_Date;
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
