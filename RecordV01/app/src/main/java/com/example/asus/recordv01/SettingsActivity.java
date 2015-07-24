package com.example.asus.recordv01;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Configuration;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.preference.RingtonePreference;
import android.text.TextUtils;
import android.widget.Toast;

import com.baidu.oauth.BaiduOAuth;

import java.util.List;


public class SettingsActivity extends PreferenceActivity {
    /**
     * Determines whether to always show the simplified settings UI, where
     * settings are presented in a single list. When false, settings are shown
     * as a master/detail two-pane view on tablets. When true, a single pane is
     * shown on tablets.
     */
    private static final boolean ALWAYS_SIMPLE_PREFS = false;

    private final String mbApiKey = "clHB9RsjML7d1GhjZ4gGqMvr";// api key for baidu service
    private Handler handler;
    private Context context;
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        context = this;
        handler = new android.os.Handler(){
            @Override
            public void handleMessage(Message msg){
                String s = String.valueOf(msg.obj);
                Toast.makeText(context, s, Toast.LENGTH_SHORT).show();
            }
        };
        setupSimplePreferencesScreen();
        PreferenceScreen backupPreference = (PreferenceScreen)getPreferenceScreen().findPreference("btn_backup");
        backupPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
//                        Intent intent = new Intent(context,BackupActiviy.class);
//                        startActivity(intent);
                // show login to baidu service
                BaiduOAuth oauthClient = new BaiduOAuth();

                oauthClient.startOAuth(context, mbApiKey, new BaiduOAuth.OAuthListener() {
                    @Override
                    public void onException(String msg) {
                        Toast.makeText(context, "Login failed " + msg, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onComplete(BaiduOAuth.BaiduOAuthResponse response) {
                        if (null != response) {
                            String userName = response.getUserName();
                            if (userName.contains("*****")) {
                                Toast.makeText(context, "Phone number is not supported,please try another account", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            CommonUploadUtils commonUploadUtils = new CommonUploadUtils(context, handler, userName);
                            String path = Environment.getExternalStorageDirectory() + "/Recordings";
                            commonUploadUtils.setUploadPath(path);
                            if (commonUploadUtils.isDownloading() || commonUploadUtils.isUploading()) {
                                Toast.makeText(context, "A Uploading or a Downloading is on processing,Please try later", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            Toast.makeText(context, response.getUserName() + " Login success", Toast.LENGTH_SHORT).show();
                            commonUploadUtils.runUpload();
                        }
                    }

                    @Override
                    public void onCancel() {
                        Toast.makeText(context, "Login cancelled", Toast.LENGTH_SHORT).show();
                    }
                });
                return false;
            }
        });

        PreferenceScreen restorePreference = (PreferenceScreen)getPreferenceScreen().findPreference("btn_restore");
        restorePreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
//                        Intent intent = new Intent(context,BackupActiviy.class);
//                        startActivity(intent);
                // show login to baidu service
                BaiduOAuth oauthClient = new BaiduOAuth();

                oauthClient.startOAuth(context, mbApiKey, new BaiduOAuth.OAuthListener() {
                    @Override
                    public void onException(String msg) {
                        Toast.makeText(context, "Login failed " + msg, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onComplete(BaiduOAuth.BaiduOAuthResponse response) {
                        if (null != response) {
                            String userName = response.getUserName();
                            if (userName.contains("*****")) {
                                Toast.makeText(context, "Phone number is not supported,please try another account", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            CommonUploadUtils commonUploadUtils = new CommonUploadUtils(context, handler, userName);
                            String path = Environment.getExternalStorageDirectory() + "/Recordings";
                            commonUploadUtils.setDownloadPath(path);
                            if (commonUploadUtils.isDownloading() || commonUploadUtils.isUploading()) {
                                Toast.makeText(context, "A Uploading or a Downloading is on processing,Please try later", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            Toast.makeText(context, response.getUserName() + " Login success", Toast.LENGTH_SHORT).show();
                            commonUploadUtils.runDownload();

                        }
                    }

                    @Override
                    public void onCancel() {
                        Toast.makeText(context, "Login cancelled", Toast.LENGTH_SHORT).show();
                    }
                });
                return false;
            }
        });
    }

    /**
     * Shows the simplified settings UI if the device configuration if the
     * device configuration dictates that a simplified, single-pane UI should be
     * shown.
     */
    private void setupSimplePreferencesScreen() {
        if (!isSimplePreferences(this)) {
            return;
        }

        // In the simplified UI, fragments are not used at all and we instead
        // use the older PreferenceActivity APIs.

        // Add 'general' preferences.
        addPreferencesFromResource(R.xml.pref_general);


        // Bind the summaries of EditText/List/Dialog/Ringtone preferences to
        // their values. When their values change, their summaries are updated
        // to reflect the new value, per the Android Design guidelines.
//        bindPreferenceSummaryToValue(findPreference("example_text"));
//        bindPreferenceSummaryToValue(findPreference("example_list"));
//        bindPreferenceSummaryToValue(findPreference("notifications_new_message_ringtone"));
//        bindPreferenceSummaryToValue(findPreference("sync_frequency"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this) && !isSimplePreferences(this);
    }

    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     */
    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    /**
     * Determines whether the simplified settings UI should be shown. This is
     * true if this is forced via {@link #ALWAYS_SIMPLE_PREFS}, or the device
     * doesn't have newer APIs like {@link PreferenceFragment}, or the device
     * doesn't have an extra-large screen. In these cases, a single-pane
     * "simplified" settings UI should be shown.
     */
    private static boolean isSimplePreferences(Context context) {
        return ALWAYS_SIMPLE_PREFS
                || Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB
                || !isXLargeTablet(context);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target) {
        if (!isSimplePreferences(this)) {
            loadHeadersFromResource(R.xml.pref_headers, target);
        }
    }

    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();

            if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                // Set the summary to reflect the new value.
                preference.setSummary(
                        index >= 0
                                ? listPreference.getEntries()[index]
                                : null);

            } else if (preference instanceof RingtonePreference) {
                // For ringtone preferences, look up the correct display value
                // using RingtoneManager.
                if (TextUtils.isEmpty(stringValue)) {
                    // Empty values correspond to 'silent' (no ringtone).
//                    preference.setSummary(R.string.pref_ringtone_silent);

                } else {
                    Ringtone ringtone = RingtoneManager.getRingtone(
                            preference.getContext(), Uri.parse(stringValue));

                    if (ringtone == null) {
                        // Clear the summary if there was a lookup error.
                        preference.setSummary(null);
                    } else {
                        // Set the summary to reflect the new ringtone display
                        // name.
                        String name = ringtone.getTitle(preference.getContext());
                        preference.setSummary(name);
                    }
                }

            } else {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                preference.setSummary(stringValue);
            }
            return true;
        }
    };

    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #sBindPreferenceSummaryToValueListener
     */
    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
//        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class GeneralPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general);

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            bindPreferenceSummaryToValue(findPreference("example_text"));
            bindPreferenceSummaryToValue(findPreference("example_list"));
        }
    }



}
