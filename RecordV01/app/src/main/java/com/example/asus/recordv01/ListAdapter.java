package com.example.asus.recordv01;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by asus on 2015/7/15.
 */
public class ListAdapter extends BaseAdapter{

    // �����ݵ�List
    private List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();
    // ��������CheckBox��ѡ��״��
    private static HashMap<Integer,Boolean> isSelected;
    // ������
    private Context context;
    // �������벼��
    private LayoutInflater inflater = null;
    private boolean isMulChoic = false;


    // ������
    public ListAdapter(List<Map<String, Object>> list, Context context){
        this.context = context;
        this.list = list;
        inflater = LayoutInflater.from(context);
        isSelected = new HashMap<Integer, Boolean>();
        // ��ʼ�����?
        initDate();
    }

    public void setMulChoic(boolean flag)
    {
        isMulChoic = flag;
    }

    private void initDate(){
        for(int i = 0; i < list.size(); i++){
            getIsSelected().put(i,false);
        }
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if(convertView == null)
        {
            // ��ȡViewHolder����
            holder = new ViewHolder();
            //���벼�ֲ���ֵ��convertview
            convertView = inflater.inflate(R.layout.item,null);
            holder.tv = (TextView)convertView.findViewById(R.id.item_tv);
            holder.cb = (CheckBox)convertView.findViewById(R.id.item_cb);
            holder.TVRecordingTime=(TextView)convertView.findViewById(R.id.item_tvTime);
            // Ϊview���ñ�ǩ
            convertView.setTag(holder);
        }
        else{
            // ȡ��holder
            holder = (ViewHolder) convertView.getTag();
        }

        holder.tv.setText((String)list.get(position).get("fileName"));
        holder.cb.setChecked(getIsSelected().get(position));
        holder.TVRecordingTime.setText((String)list.get(position).get("recordingTime"));

        if(isMulChoic == false)
        {
            holder.cb.setVisibility(View.INVISIBLE);
        }
        else
        {
            holder.cb.setVisibility(View.VISIBLE);
        }

        return convertView;
    }

    public static class ViewHolder
    {
        TextView tv;
        TextView TVRecordingTime;
        CheckBox cb;
    }

    public static HashMap<Integer,Boolean> getIsSelected(){
        return isSelected;
    }

    public static void setIsSelected(HashMap<Integer,Boolean> isSelected){
        ListAdapter.isSelected = isSelected;
    }
}

