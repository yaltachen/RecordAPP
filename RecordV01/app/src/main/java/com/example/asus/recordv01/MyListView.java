package com.example.asus.recordv01;

import android.app.ListActivity;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Created by asus on 2015/7/18.
 */
public class MyListView extends ListActivity {

    private List<Map<String, Object>> mData;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        try {
            mData = getData();
        } catch (IOException e) {
            e.printStackTrace();
        }
        MyAdapter adapter = new MyAdapter(this);
        setListAdapter(adapter);
    }

    private List<Map<String,Object>> getData() throws IOException {
        List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();


        File fileDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Recordings");
        File[] fileList = fileDir.listFiles();


        for(int i = 0; i < fileList().length; i++)
        {
            Map<String,Object> map = new HashMap<String,Object>();
            map.put("fileName",fileList[i].getName());
            map.put("recordingTime",getAmrDuration(fileList[i]));
            list.add(map);
        }
        return list;
    }


    public final class ViewHolder{
        public CheckBox CBSelect;
        public TextView TVFileName;
        public TextView TVRecordingTime;
    }
    public class MyAdapter extends BaseAdapter{

        //LayoutInflater just like findViewById() but it used to find .xml files
        private LayoutInflater mInflater;
        public MyAdapter(Context context){
            this.mInflater = LayoutInflater.from(context);
        }
        @Override
        public int getCount() {
            return mData.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            ViewHolder holder = null;
            if(convertView == null)
            {
                holder = new ViewHolder();

                convertView = mInflater.inflate(R.layout.item,null);
                holder.CBSelect = (CheckBox)convertView.findViewById(R.id.item_cb);
                holder.TVRecordingTime = (TextView)convertView.findViewById(R.id.item_tvTime);
                holder.TVFileName = (TextView)convertView.findViewById(R.id.item_tv);
                convertView.setTag(holder);
            }else{
                holder = (ViewHolder)convertView.getTag();
            }
            holder.TVFileName.setText((String)mData.get(position).get("fileName"));
            holder.TVRecordingTime.setText((String)mData.get(position).get("recordingTime"));

            return convertView;
        }
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
