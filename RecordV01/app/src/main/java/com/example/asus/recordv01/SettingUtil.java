package com.example.asus.recordv01;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by asus on 2015/7/19.
 */
public class SettingUtil {
    private Context context;

    public SettingUtil(Context context)
    {
        this.context = context;
    }

    /**
     * 1->1 hour
     * 2->2 hour
     * 3->3 hour
     * 4->4 hour
     * @return
     */
    public String getMaxHour()
    {
        SharedPreferences sharedPreferences = getSharedPreferences();
        return sharedPreferences.getString("maxTime_list","1");
    }
    /**
     * 1->AMR
     * 2->WAV
     * @return
     */
    public String getRecordFormat()
    {
        SharedPreferences sharedPreferences = getSharedPreferences();
        return sharedPreferences.getString("example_list", "1");
    }

    public boolean getStoreInSDCard()
    {
        SharedPreferences sharedPreferences = getSharedPreferences();
        return sharedPreferences.getBoolean("checkbox_storeinsdcard",false);
    }

    private SharedPreferences getSharedPreferences()
    {
        SharedPreferences mySharedPreferences= context.getSharedPreferences("com.example.asus.recordv01_preferences", Activity.MODE_PRIVATE);
        return mySharedPreferences;
    }
}
