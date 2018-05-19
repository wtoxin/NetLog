package edu.nudt.netlog;


import android.Manifest;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.EditTextPreference;
import android.preference.MultiSelectListPreference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.preference.TwoStatePreference;
import android.support.annotation.NonNull;
import android.support.v4.app.NavUtils;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;
import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;

import static edu.nudt.netlog.ActivityMain.imei;

/**
 * Activity for settings
 */
public class ActivitySettings extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String TAG = "NetLog.Settings";

    private boolean running = false;

    private static final int REQUEST_EXPORT = 1;
    private static final int REQUEST_IMPORT = 2;
    private static final int REQUEST_HOSTS = 3;
    private static final int REQUEST_CALL = 4;

    private AlertDialog dialogFilter = null;

    private static final Intent INTENT_VPN_SETTINGS = new Intent("android.net.vpn.SETTINGS");

    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppThemeBlue);
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new FragmentSettings()).commit();
        getSupportActionBar().setTitle(R.string.menu_settings);
        running = true;
    }

    private PreferenceScreen getPreferenceScreen() {
        return ((PreferenceFragment) getFragmentManager().findFragmentById(android.R.id.content)).getPreferenceScreen();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        final PreferenceScreen screen = getPreferenceScreen();
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

//        // VPN parameters
//        screen.findPreference("vpn4").setTitle(getString(R.string.setting_vpn4, prefs.getString("vpn4", "10.1.10.1")));
//        screen.findPreference("vpn6").setTitle(getString(R.string.setting_vpn6, prefs.getString("vpn6", "fd00:1:fd00:1:fd00:1:fd00:1")));
//        EditTextPreference pref_dns1 = (EditTextPreference) screen.findPreference("dns");
//        EditTextPreference pref_dns2 = (EditTextPreference) screen.findPreference("dns2");
//        List<String> def_dns = Util.getDefaultDNS(this);
//        pref_dns1.getEditText().setHint(def_dns.get(0));
//        pref_dns2.getEditText().setHint(def_dns.get(1));
//        pref_dns1.setTitle(getString(R.string.setting_dns, prefs.getString("dns", def_dns.get(0))));
//        pref_dns2.setTitle(getString(R.string.setting_dns, prefs.getString("dns2", def_dns.get(1))));
//
        // PCAP parameters
        screen.findPreference("pcap_record_size").setTitle(getString(R.string.setting_pcap_record_size, prefs.getString("pcap_record_size", "65536")));
        screen.findPreference("pcap_file_size").setTitle(getString(R.string.setting_pcap_file_size, prefs.getString("pcap_file_size", "1024")));

        // SERVER parameters
        screen.findPreference("set_server_ip").setTitle(getString(R.string.setting_server_ip, prefs.getString("set_server_ip","118.24.12.193")));
        screen.findPreference("set_server_port").setTitle(getString(R.string.setting_server_port, prefs.getString("set_server_port","5000")));
        TelephonyManager telephonyManager = (TelephonyManager) this.getSystemService(this.TELEPHONY_SERVICE);
        screen.findPreference("set_imei").setDefaultValue(imei);
        screen.findPreference("set_imei").setTitle(getString(R.string.setting_imei, imei));


//        // Handle technical info
//        Preference.OnPreferenceClickListener listener = new Preference.OnPreferenceClickListener() {
//            @Override
//            public boolean onPreferenceClick(Preference preference) {
//                updateTechnicalInfo();
//                return true;
//            }
//        };
//
//        // Technical info
//        Preference pref_technical_info = screen.findPreference("technical_info");
//        Preference pref_technical_network = screen.findPreference("technical_network");
//        pref_technical_info.setEnabled(INTENT_VPN_SETTINGS.resolveActivity(this.getPackageManager()) != null);
//        pref_technical_info.setIntent(INTENT_VPN_SETTINGS);
//        pref_technical_info.setOnPreferenceClickListener(listener);
//        pref_technical_network.setOnPreferenceClickListener(listener);
//        updateTechnicalInfo();


        screen.removePreference(screen.findPreference("screen_defaults"));
        screen.removePreference(screen.findPreference("screen_options"));
        screen.removePreference(screen.findPreference("screen_network_options"));
        screen.removePreference(screen.findPreference("screen_advanced_options"));
        screen.removePreference(screen.findPreference("screen_stats"));
        screen.removePreference(screen.findPreference("screen_backup"));
        screen.removePreference(screen.findPreference("screen_development"));
        screen.removePreference(screen.findPreference("screen_technical"));
    }


    @Override
    protected void onResume() {
        super.onResume();

        checkPermissions(null);

        // Listen for preference changes
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.registerOnSharedPreferenceChangeListener(this);

    }

    @Override
    protected void onPause() {
        super.onPause();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.unregisterOnSharedPreferenceChangeListener(this);

    }

    @Override
    protected void onDestroy() {
        running = false;
        if (dialogFilter != null) {
            dialogFilter.dismiss();
            dialogFilter = null;
        }
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Log.i(TAG, "Up");
                NavUtils.navigateUpFromSameTask(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    @TargetApi(Build.VERSION_CODES.M)
    public void onSharedPreferenceChanged(SharedPreferences prefs, String name) {
        Object value = prefs.getAll().get(name);
        if (value instanceof String && "".equals(value))
            prefs.edit().remove(name).apply();

        // Dependencies
        if ("manage_system".equals(name)) {
            boolean manage = prefs.getBoolean(name, false);
            if (!manage)
                prefs.edit().putBoolean("show_user", true).apply();
            prefs.edit().putBoolean("show_system", manage).apply();
            ServiceSinkhole.reload("changed " + name, this, false);

        } else if ("vpn4".equals(name)) {
            String vpn4 = prefs.getString(name, null);
            try {
                checkAddress(vpn4);
            } catch (Throwable ex) {
                prefs.edit().remove(name).apply();
                ((EditTextPreference) getPreferenceScreen().findPreference(name)).setText(null);
                if (!TextUtils.isEmpty(vpn4))
                    Toast.makeText(ActivitySettings.this, ex.toString(), Toast.LENGTH_LONG).show();
            }
            getPreferenceScreen().findPreference(name).setTitle(
                    getString(R.string.setting_vpn4, prefs.getString(name, "10.1.10.1")));
            ServiceSinkhole.reload("changed " + name, this, false);

        } else if ("vpn6".equals(name)) {
            String vpn6 = prefs.getString(name, null);
            try {
                checkAddress(vpn6);
            } catch (Throwable ex) {
                prefs.edit().remove(name).apply();
                ((EditTextPreference) getPreferenceScreen().findPreference(name)).setText(null);
                if (!TextUtils.isEmpty(vpn6))
                    Toast.makeText(ActivitySettings.this, ex.toString(), Toast.LENGTH_LONG).show();
            }
            getPreferenceScreen().findPreference(name).setTitle(
                    getString(R.string.setting_vpn6, prefs.getString(name, "fd00:1:fd00:1:fd00:1:fd00:1")));
            ServiceSinkhole.reload("changed " + name, this, false);

        } else if ("dns".equals(name) || "dns2".equals(name)) {
            String dns = prefs.getString(name, null);
            try {
                checkAddress(dns);
            } catch (Throwable ex) {
                prefs.edit().remove(name).apply();
                ((EditTextPreference) getPreferenceScreen().findPreference(name)).setText(null);
                if (!TextUtils.isEmpty(dns))
                    Toast.makeText(ActivitySettings.this, ex.toString(), Toast.LENGTH_LONG).show();
            }
            getPreferenceScreen().findPreference(name).setTitle(
                    getString(R.string.setting_dns,
                            prefs.getString(name, Util.getDefaultDNS(this).get("dns".equals(name) ? 0 : 1))));
            ServiceSinkhole.reload("changed " + name, this, false);

        } else if ("pcap_record_size".equals(name) || "pcap_file_size".equals(name)) {
            if ("pcap_record_size".equals(name))
                getPreferenceScreen().findPreference(name).setTitle(getString(R.string.setting_pcap_record_size, prefs.getString(name, "65536")));
            else
                getPreferenceScreen().findPreference(name).setTitle(getString(R.string.setting_pcap_file_size, prefs.getString(name, "1024")));

            ServiceSinkhole.setPcap(false, this);

            File pcap_file = new File(getDir("data", MODE_PRIVATE), "netguard.pcap");
            if (pcap_file.exists() && !pcap_file.delete())
                Log.w(TAG, "Delete PCAP failed");

            if (prefs.getBoolean("pcap", false))
                ServiceSinkhole.setPcap(true, this);

        }
        else if("set_server_port".equals(name) || "set_server_ip".equals(name)){

            if("set_server_port".equals(name)){
                String server_port = prefs.getString(name,null);
                //int port = Integer.parseInt(server_port);

                int port;
                if(server_port!=null&&server_port.length()<=5)
                    port = Integer.parseInt(server_port);
                else
                    port = 0;

                try{
                    checkPort(port);
                }catch (Throwable ex){
                    prefs.edit().remove(name).apply();
                    ((EditTextPreference) getPreferenceScreen().findPreference(name)).setText(null);
                    if (!TextUtils.isEmpty(server_port))
                        Toast.makeText(ActivitySettings.this, "emmm, port is wrong..", Toast.LENGTH_LONG).show();
                }

                getPreferenceScreen().findPreference(name).setTitle(getString(R.string.setting_server_port, prefs.getString(name, "5000")));
            }
            else{
                String server_ip = prefs.getString(name, null);
                try {
                    checkAddress(server_ip);
                } catch (Throwable ex) {
                    prefs.edit().remove(name).apply();
                    ((EditTextPreference) getPreferenceScreen().findPreference(name)).setText(null);
                    if (!TextUtils.isEmpty(server_ip))
                        Toast.makeText(ActivitySettings.this, ex.toString(), Toast.LENGTH_LONG).show();
                }
                getPreferenceScreen().findPreference(name).setTitle(getString(R.string.setting_server_ip, prefs.getString(name, "118.24.12.193")));
                //ServiceSinkhole.reload("changed " + name, this, false);
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private boolean checkPermissions(String name) {
        PreferenceScreen screen = getPreferenceScreen();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        // Check if permission was revoked
        if ((name == null || "disable_on_call".equals(name)) && prefs.getBoolean("disable_on_call", false))
            if (!Util.hasPhoneStatePermission(this)) {
                prefs.edit().putBoolean("disable_on_call", false).apply();
                ((TwoStatePreference) screen.findPreference("disable_on_call")).setChecked(false);

                requestPermissions(new String[]{Manifest.permission.READ_PHONE_STATE}, REQUEST_CALL);

                if (name != null)
                    return false;
            }

        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        PreferenceScreen screen = getPreferenceScreen();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        boolean granted = (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED);

        if (requestCode == REQUEST_CALL) {
            prefs.edit().putBoolean("disable_on_call", granted).apply();
            ((TwoStatePreference) screen.findPreference("disable_on_call")).setChecked(granted);
        }

        if (granted)
            ServiceSinkhole.reload("permission granted", this, false);
    }

    private void checkAddress(String address) throws IllegalArgumentException, UnknownHostException {
        if (address == null || TextUtils.isEmpty(address.trim()))
            throw new IllegalArgumentException("Bad address");
        if (!Util.isNumericAddress(address))
            throw new IllegalArgumentException("Bad address");
        InetAddress idns = InetAddress.getByName(address);
        if (idns.isLoopbackAddress() || idns.isAnyLocalAddress())
            throw new IllegalArgumentException("Bad address");
    }

    private void checkPort(int Port) throws IllegalArgumentException, UnknownHostException{
        if (Port <= 0 || Port > 65535)
            throw new IllegalArgumentException("Bad port");
    }
}
