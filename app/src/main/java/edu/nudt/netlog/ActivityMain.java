package edu.nudt.netlog;


import android.Manifest;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.net.VpnService;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SwitchCompat;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

// JQ Mod, for file operation
import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.Poi;
import com.baidu.mapapi.SDKInitializer;
import com.facebook.stetho.Stetho;

import java.io.File;


import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * Main Activity
 */
public class ActivityMain extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String TAG = "NetLog.Main";

    private boolean running = false;
    private ImageView ivIcon;
    private ImageView ivQueue;
    private SwitchCompat swEnabled;
    private ImageView ivMetered;
    private SwipeRefreshLayout swipeRefresh;
    private AdapterRule adapter = null;
    private MenuItem menuSearch = null;
    private AlertDialog dialogFirst = null;
    private AlertDialog dialogVpn = null;
    private AlertDialog dialogDoze = null;
    private AlertDialog dialogLegend = null;
    private AlertDialog dialogAbout = null;

    private static final int REQUEST_VPN = 1;
    private static final int REQUEST_INVITE = 2;
    private static final int REQUEST_LOGCAT = 3;
    public static final int REQUEST_ROAMING = 4;
    public static String imei="";

    private static final int MIN_SDK = Build.VERSION_CODES.LOLLIPOP_MR1;

    /**
     * @author: JQ
     * @date: 2018-04-19 16:00:00
     * Create directory for this App for further data storage
     * Modifications marked by "JQ Mod"
     */
    // JQ Mod: get path
    private static final String fileDirPath = android.os.Environment.getExternalStorageDirectory().getAbsolutePath()+"/NetLog/";

    public static final String ACTION_RULES_CHANGED = "ACTION_RULES_CHANGED";
    public static final String ACTION_QUEUE_CHANGED = "ACTION_QUEUE_CHANGED";
    public static final String EXTRA_REFRESH = "Refresh";
    public static final String EXTRA_SEARCH = "Search";
    public static final String EXTRA_RELATED = "Related";
    public static final String EXTRA_APPROVE = "Approve";
    public static final String EXTRA_LOGCAT = "Logcat";
    public static final String EXTRA_CONNECTED = "Connected";
    public static final String EXTRA_METERED = "Metered";
    public static final String EXTRA_SIZE = "Size";

    // for baidu location
    public static LocationService locationService;
    public static Vibrator mVibrator;
    public static boolean wifiConnected;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "Create version=" + Util.getSelfVersionName(this) + "/" + Util.getSelfVersionCode(this));
        Util.logExtras(getIntent());

        // Check minimum Android version
        if (Build.VERSION.SDK_INT < MIN_SDK) {
            Log.i(TAG, "SDK=" + Build.VERSION.SDK_INT);
            super.onCreate(savedInstanceState);
            setContentView(R.layout.android);
            return;
        }
        setTheme(R.style.AppThemeBlue);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        /************************************************************
         * 权限
         */
        /*
        //Check permissions for gps
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
        if (permissionCheck !=  PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 0);
        }
        permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionCheck !=  PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        }

        // Check writing permission
        permissionCheck = ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permissionCheck !=  PackageManager.PERMISSION_GRANTED) {
//            Toast.makeText(this, "请授予权限", Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        }
        permissionCheck = ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE);
        if (permissionCheck !=  PackageManager.PERMISSION_GRANTED) {
//            Toast.makeText(this, "请授予权限", Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
        }*/

        //Check permissions
        int permissionCheck1 = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
        int permissionCheck2 = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        int permissionCheck3 = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        int permissionCheck4 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int permissionCheck5 = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE);

        if (permissionCheck1 !=  PackageManager.PERMISSION_GRANTED ||
                permissionCheck2 != PackageManager.PERMISSION_GRANTED ||
                permissionCheck3 != PackageManager.PERMISSION_GRANTED ||
                permissionCheck4 != PackageManager.PERMISSION_GRANTED ||
                permissionCheck5 != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
            }, 0);
        }
        //*************************************************************

        // JQ Mod: call func to create directory
        Log.i(TAG, "create directory for this app, directory: "+fileDirPath);
        Util.createDirectory(fileDirPath);

        /*
         * SET FOR BAIDU API
         */
        locationService = new LocationService(getApplicationContext());
        mVibrator =(Vibrator)getApplicationContext().getSystemService(Service.VIBRATOR_SERVICE);
        SDKInitializer.initialize(getApplicationContext());

        //for sqlite check
        Stetho.initializeWithDefaults(this);

        running = true;

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.edit().putString("set_imei",imei).commit();
        boolean enabled = prefs.getBoolean("enabled", false);
        boolean initialized = prefs.getBoolean("initialized", false);


        if (!getIntent().hasExtra(EXTRA_APPROVE)) {
            if (enabled)
                ServiceSinkhole.start("UI", this);
            else
                ServiceSinkhole.stop("UI", this, false);
        }

        // Action bar
        final View actionView = getLayoutInflater().inflate(R.layout.actionmain, null, false);
        swEnabled = actionView.findViewById(R.id.swEnabled);

        // Title
        getSupportActionBar().setTitle("NetLog");

        // JQ Mod, Timer
        Intent intent = new Intent(this, TimerService.class);
        startService(intent);

        // JQ Mod, Check wifi
        //wifiConnected = Util.isWifiActive(this);
        wifiConnected = true;


        // On/off switch
        swEnabled.setChecked(enabled);
        if(wifiConnected){
            if(swEnabled.isChecked()){
                // -----------location config ------------
                //获取locationservice实例，建议应用中只初始化1个location实例，然后使用，可以参考其他示例的activity，都是通过此种方式获取locationservice实例的
                locationService.registerListener(mListener);
                //注册监听
                int type = getIntent().getIntExtra("from", 0);
                if (type == 0) {
                    locationService.setLocationOption(locationService.getDefaultLocationClientOption());
                } else if (type == 1) {
                    locationService.setLocationOption(locationService.getOption());
                }
                locationService.start(this);// 定位SDK
                // start之后会默认发起一次定位请求，开发者无须判断isstart并主动调用request
            }
        }else{
            swEnabled.setChecked(false);
        }
        swEnabled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.i(TAG, "Switch=" + isChecked);
                prefs.edit().putBoolean("enabled", isChecked).apply();

                if (isChecked) {
                    //wifiConnected = Util.isWifiActive(ActivityMain.this);
                    wifiConnected = true;
                    if(!wifiConnected){
                        swEnabled.setChecked(false);
                        Toast.makeText(ActivityMain.this, "Please connect to a hotspot first.", Toast.LENGTH_LONG).show();
                        Toast.makeText(ActivityMain.this, R.string.msg_always_on, Toast.LENGTH_LONG).show();
                        return;
                    }
                    // -----------location config ------------
                    //获取locationservice实例，建议应用中只初始化1个location实例，然后使用，可以参考其他示例的activity，都是通过此种方式获取locationservice实例的
                    locationService.registerListener(mListener);
                    //注册监听
                    int type = getIntent().getIntExtra("from", 0);
                    if (type == 0) {
                        locationService.setLocationOption(locationService.getDefaultLocationClientOption());
                    } else if (type == 1) {
                        locationService.setLocationOption(locationService.getOption());
                    }
                    locationService.start(ActivityMain.this);// 定位SDK
                    // start之后会默认发起一次定位请求，开发者无须判断isstart并主动调用request



                    String alwaysOn = Settings.Secure.getString(getContentResolver(), "always_on_vpn_app");
                    Log.i(TAG, "Always-on=" + alwaysOn);
                    if (!TextUtils.isEmpty(alwaysOn) && !getPackageName().equals(alwaysOn)) {
                        swEnabled.setChecked(false);
                        Toast.makeText(ActivityMain.this, R.string.msg_always_on, Toast.LENGTH_LONG).show();
                        return;
                    }

                    try {
                        final Intent prepare = VpnService.prepare(ActivityMain.this);
                        if (prepare == null) {
                            Log.i(TAG, "Prepare done");
                            onActivityResult(REQUEST_VPN, RESULT_OK, null);
                        } else {
                            // Show dialog
                            LayoutInflater inflater = LayoutInflater.from(ActivityMain.this);
                            View view = inflater.inflate(R.layout.vpn, null, false);
                            dialogVpn = new AlertDialog.Builder(ActivityMain.this)
                                    .setView(view)
                                    .setCancelable(false)
                                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (running) {
                                                Log.i(TAG, "Start intent=" + prepare);
                                                try {
                                                    // com.android.vpndialogs.ConfirmDialog required
                                                    startActivityForResult(prepare, REQUEST_VPN);
                                                } catch (Throwable ex) {
                                                    Log.e(TAG, ex.toString() + "\n" + Log.getStackTraceString(ex));
                                                    onActivityResult(REQUEST_VPN, RESULT_CANCELED, null);
                                                    prefs.edit().putBoolean("enabled", false).apply();
                                                }
                                            }
                                        }
                                    })
                                    .setOnDismissListener(new DialogInterface.OnDismissListener() {
                                        @Override
                                        public void onDismiss(DialogInterface dialogInterface) {
                                            dialogVpn = null;
                                        }
                                    })
                                    .create();
                            dialogVpn.show();
                        }
                    } catch (Throwable ex) {
                        // Prepare failed
                        Log.e(TAG, ex.toString() + "\n" + Log.getStackTraceString(ex));
                        prefs.edit().putBoolean("enabled", false).apply();
                    }

                } else {
                    locationService.stop();
                    ServiceSinkhole.stop("switch off", ActivityMain.this, false);
                }
            }
        });
        if (enabled)
            checkDoze();

        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(actionView);

        // Disabled warning
        TextView tvDisabled = findViewById(R.id.tvDisabled);
        tvDisabled.setVisibility(enabled ? View.GONE : View.VISIBLE);

        // Application list
        RecyclerView rvApplication = findViewById(R.id.rvApplication);
        rvApplication.setHasFixedSize(false);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setAutoMeasureEnabled(true);
        rvApplication.setLayoutManager(llm);
        adapter = new AdapterRule(this, findViewById(R.id.vwPopupAnchor));
        rvApplication.setAdapter(adapter);

        // Swipe to refresh
        TypedValue tv = new TypedValue();
        getTheme().resolveAttribute(R.attr.colorPrimary, tv, true);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        swipeRefresh.setColorSchemeColors(Color.WHITE, Color.WHITE, Color.WHITE);
        swipeRefresh.setProgressBackgroundColorSchemeColor(tv.data);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Rule.clearCache(ActivityMain.this);
                ServiceSinkhole.reload("pull", ActivityMain.this, false);
                updateApplicationList(null);
            }
        });

        // Hint system applications

        // Listen for preference changes
        prefs.registerOnSharedPreferenceChangeListener(this);

        // Listen for rule set changes
        IntentFilter ifr = new IntentFilter(ACTION_RULES_CHANGED);
        LocalBroadcastManager.getInstance(this).registerReceiver(onRulesChanged, ifr);

        // Listen for queue changes

        // Listen for added/removed applications
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
        intentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        intentFilter.addDataScheme("package");
        registerReceiver(packageChangedReceiver, intentFilter);

        // Fill application list
        updateApplicationList(getIntent().getStringExtra(EXTRA_SEARCH));

        // Handle intent
        checkExtras(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Log.i(TAG, "New intent");
        Util.logExtras(intent);
        super.onNewIntent(intent);

        if (Build.VERSION.SDK_INT < MIN_SDK || Util.hasXposed(this))
            return;

        setIntent(intent);

        if (Build.VERSION.SDK_INT >= MIN_SDK) {
            if (intent.hasExtra(EXTRA_REFRESH))
                updateApplicationList(intent.getStringExtra(EXTRA_SEARCH));
            else
                updateSearch(intent.getStringExtra(EXTRA_SEARCH));
            checkExtras(intent);
        }
    }

    @Override
    protected void onResume() {
        Log.i(TAG, "Resume");

        if (Build.VERSION.SDK_INT < MIN_SDK || Util.hasXposed(this)) {
            super.onResume();
            return;
        }

        DatabaseHelper.getInstance(this).addAccessChangedListener(accessChangedListener);
        if (adapter != null)
            adapter.notifyDataSetChanged();

        super.onResume();
    }

    @Override
    protected void onPause() {
        Log.i(TAG, "Pause");
        super.onPause();

        if (Build.VERSION.SDK_INT < MIN_SDK || Util.hasXposed(this))
            return;

        DatabaseHelper.getInstance(this).removeAccessChangedListener(accessChangedListener);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        Log.i(TAG, "Config");
        super.onConfigurationChanged(newConfig);

        if (Build.VERSION.SDK_INT < MIN_SDK || Util.hasXposed(this))
            return;
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "Destroy");

        if (Build.VERSION.SDK_INT < MIN_SDK || Util.hasXposed(this)) {
            super.onDestroy();
            return;
        }

        running = false;
        adapter = null;

        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);

        LocalBroadcastManager.getInstance(this).unregisterReceiver(onRulesChanged);
        unregisterReceiver(packageChangedReceiver);

        if (dialogFirst != null) {
            dialogFirst.dismiss();
            dialogFirst = null;
        }
        if (dialogVpn != null) {
            dialogVpn.dismiss();
            dialogVpn = null;
        }
        if (dialogDoze != null) {
            dialogDoze.dismiss();
            dialogDoze = null;
        }
        if (dialogLegend != null) {
            dialogLegend.dismiss();
            dialogLegend = null;
        }
        if (dialogAbout != null) {
            dialogAbout.dismiss();
            dialogAbout = null;
        }

        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        Log.i(TAG, "onActivityResult request=" + requestCode + " result=" + requestCode + " ok=" + (resultCode == RESULT_OK));
        Util.logExtras(data);

        if (requestCode == REQUEST_VPN) {
            // Handle VPN approval
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            prefs.edit().putBoolean("enabled", resultCode == RESULT_OK).apply();
            if (resultCode == RESULT_OK) {
                ServiceSinkhole.start("prepared", this);

//                Toast on = Toast.makeText(ActivityMain.this, R.string.msg_on, Toast.LENGTH_LONG);
//                on.setGravity(Gravity.CENTER, 0, 0);
//                on.show();

                checkDoze();
            } else if (resultCode == RESULT_CANCELED)
                Toast.makeText(this, R.string.msg_vpn_cancelled, Toast.LENGTH_LONG).show();

        } else if (requestCode == REQUEST_INVITE) {
            // Do nothing

        } else if (requestCode == REQUEST_LOGCAT) {
            // Send logcat by e-mail
            if (resultCode == RESULT_OK) {
                Uri target = data.getData();
                if (data.hasExtra("org.openintents.extra.DIR_PATH"))
                    target = Uri.parse(target + "/logcat.txt");
                Log.i(TAG, "Export URI=" + target);
                Util.sendLogcat(target, this);
            }

        } else {
            Log.w(TAG, "Unknown activity result request=" + requestCode);
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_ROAMING)
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                ServiceSinkhole.reload("permission granted", this, false);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String name) {
        Log.i(TAG, "Preference " + name + "=" + prefs.getAll().get(name));
        if ("enabled".equals(name)) {
            // Get enabled
            boolean enabled = prefs.getBoolean(name, false);

            // Display disabled warning
            TextView tvDisabled = findViewById(R.id.tvDisabled);
            tvDisabled.setVisibility(enabled ? View.GONE : View.VISIBLE);

            // Check switch state
            SwitchCompat swEnabled = getSupportActionBar().getCustomView().findViewById(R.id.swEnabled);
            if (swEnabled.isChecked() != enabled)
                swEnabled.setChecked(enabled);

        } else if ("whitelist_wifi".equals(name) ||
                "screen_on".equals(name) ||
                "screen_wifi".equals(name) ||
                "whitelist_other".equals(name) ||
                "screen_other".equals(name) ||
                "whitelist_roaming".equals(name) ||
                "show_user".equals(name) ||
                "show_system".equals(name) ||
                "show_nointernet".equals(name) ||
                "show_disabled".equals(name) ||
                "sort".equals(name) ||
                "imported".equals(name)) {

            updateApplicationList(null);

        } else if ("manage_system".equals(name)) {
            invalidateOptionsMenu();
            updateApplicationList(null);

        }
    }

    private DatabaseHelper.AccessChangedListener accessChangedListener = new DatabaseHelper.AccessChangedListener() {
        @Override
        public void onChanged() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (adapter != null && adapter.isLive())
                        adapter.notifyDataSetChanged();
                }
            });
        }
    };

    private BroadcastReceiver onRulesChanged = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "Received " + intent);
            Util.logExtras(intent);

            if (adapter != null)
                if (intent.hasExtra(EXTRA_CONNECTED) && intent.hasExtra(EXTRA_METERED)) {
                    if (intent.getBooleanExtra(EXTRA_CONNECTED, false)) {
                        if (intent.getBooleanExtra(EXTRA_METERED, false))
                            adapter.setMobileActive();
                        else
                            adapter.setWifiActive();
                    } else {
                        adapter.setDisconnected();
                    }
                } else
                    updateApplicationList(null);
        }
    };

    private BroadcastReceiver packageChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "Received " + intent);
            Util.logExtras(intent);
            updateApplicationList(null);
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (Build.VERSION.SDK_INT < MIN_SDK)
            return false;

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);

        // Search
        menuSearch = menu.findItem(R.id.menu_search);
        menuSearch.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                if (getIntent().hasExtra(EXTRA_SEARCH) && !getIntent().getBooleanExtra(EXTRA_RELATED, false))
                    finish();
                return true;
            }
        });

        final SearchView searchView = (SearchView) menuSearch.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (adapter != null)
                    adapter.getFilter().filter(query);
                searchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (adapter != null)
                    adapter.getFilter().filter(newText);
                return true;
            }
        });
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                Intent intent = getIntent();
                intent.removeExtra(EXTRA_SEARCH);

                if (adapter != null)
                    adapter.getFilter().filter(null);
                return true;
            }
        });
        String search = getIntent().getStringExtra(EXTRA_SEARCH);
        if (search != null) {
            menuSearch.expandActionView();
            searchView.setQuery(search, true);
        }

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        if (prefs.getBoolean("manage_system", false)) {
            menu.findItem(R.id.menu_app_user).setChecked(prefs.getBoolean("show_user", true));
            menu.findItem(R.id.menu_app_system).setChecked(prefs.getBoolean("show_system", false));
        } else {
            Menu submenu = menu.findItem(R.id.menu_filter).getSubMenu();
            submenu.removeItem(R.id.menu_app_user);
            submenu.removeItem(R.id.menu_app_system);
        }

        menu.findItem(R.id.menu_app_nointernet).setChecked(prefs.getBoolean("show_nointernet", true));
        menu.findItem(R.id.menu_app_disabled).setChecked(prefs.getBoolean("show_disabled", true));

        String sort = prefs.getString("sort", "name");
        if ("uid".equals(sort))
            menu.findItem(R.id.menu_sort_uid).setChecked(true);
        else
            menu.findItem(R.id.menu_sort_name).setChecked(true);

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i(TAG, "Menu=" + item.getTitle());

        // Handle item selection
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        switch (item.getItemId()) {
            case R.id.menu_app_user:
                item.setChecked(!item.isChecked());
                prefs.edit().putBoolean("show_user", item.isChecked()).apply();
                return true;

            case R.id.menu_app_system:
                item.setChecked(!item.isChecked());
                prefs.edit().putBoolean("show_system", item.isChecked()).apply();
                return true;

            case R.id.menu_app_nointernet:
                item.setChecked(!item.isChecked());
                prefs.edit().putBoolean("show_nointernet", item.isChecked()).apply();
                return true;

            case R.id.menu_app_disabled:
                item.setChecked(!item.isChecked());
                prefs.edit().putBoolean("show_disabled", item.isChecked()).apply();
                return true;

            case R.id.menu_sort_name:
                item.setChecked(true);
                prefs.edit().putString("sort", "name").apply();
                return true;

            case R.id.menu_sort_uid:
                item.setChecked(true);
                prefs.edit().putString("sort", "uid").apply();
                return true;

            case R.id.menu_log:
                if (Util.canFilter(this))
                    startActivity(new Intent(this, ActivityLog.class));
                else
                    Toast.makeText(this, R.string.msg_unavailable, Toast.LENGTH_SHORT).show();
                return true;

            case R.id.menu_settings:
                startActivity(new Intent(this, ActivitySettings.class));
                return true;

//            case R.id.menu_about:
//                menu_about();
//                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void checkExtras(Intent intent) {
        // Approve request
        if (intent.hasExtra(EXTRA_APPROVE)) {
            Log.i(TAG, "Requesting VPN approval");
            swEnabled.toggle();
        }

        if (intent.hasExtra(EXTRA_LOGCAT)) {
            Log.i(TAG, "Requesting logcat");
            Intent logcat = getIntentLogcat();
            if (logcat.resolveActivity(getPackageManager()) != null)
                startActivityForResult(logcat, REQUEST_LOGCAT);
        }
    }

    private void updateApplicationList(final String search) {
        Log.i(TAG, "Update search=" + search);

        new AsyncTask<Object, Object, List<Rule>>() {
            private boolean refreshing = true;

            @Override
            protected void onPreExecute() {
                swipeRefresh.post(new Runnable() {
                    @Override
                    public void run() {
                        if (refreshing)
                            swipeRefresh.setRefreshing(true);
                    }
                });
            }

            @Override
            protected List<Rule> doInBackground(Object... arg) {
                return Rule.getRules(false, ActivityMain.this);
            }

            @Override
            protected void onPostExecute(List<Rule> result) {
                if (running) {
                    if (adapter != null) {
                        adapter.set(result);
                        updateSearch(search);
                    }

                    if (swipeRefresh != null) {
                        refreshing = false;
                        swipeRefresh.setRefreshing(false);
                    }
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void updateSearch(String search) {
        if (menuSearch != null) {
            SearchView searchView = (SearchView) menuSearch.getActionView();
            if (search == null) {
                if (menuSearch.isActionViewExpanded())
                    adapter.getFilter().filter(searchView.getQuery().toString());
            } else {
                menuSearch.expandActionView();
                searchView.setQuery(search, true);
            }
        }
    }

    private void checkDoze() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            final Intent doze = new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
            if (Util.batteryOptimizing(this) && getPackageManager().resolveActivity(doze, 0) != null) {
                final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
                if (!prefs.getBoolean("nodoze", false)) {
                    LayoutInflater inflater = LayoutInflater.from(this);
                    View view = inflater.inflate(R.layout.doze, null, false);
                    final CheckBox cbDontAsk = view.findViewById(R.id.cbDontAsk);
                    dialogDoze = new AlertDialog.Builder(this)
                            .setView(view)
                            .setCancelable(true)
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    prefs.edit().putBoolean("nodoze", cbDontAsk.isChecked()).apply();
                                    startActivity(doze);
                                }
                            })
                            .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    prefs.edit().putBoolean("nodoze", cbDontAsk.isChecked()).apply();
                                }
                            })
                            .setOnDismissListener(new DialogInterface.OnDismissListener() {
                                @Override
                                public void onDismiss(DialogInterface dialogInterface) {
                                    dialogDoze = null;
                                    checkDataSaving();
                                }
                            })
                            .create();
                    dialogDoze.show();
                } else
                    checkDataSaving();
            } else
                checkDataSaving();
        }
    }

    private void checkDataSaving() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            final Intent settings = new Intent(
                    Settings.ACTION_IGNORE_BACKGROUND_DATA_RESTRICTIONS_SETTINGS,
                    Uri.parse("package:" + getPackageName()));
            if (Util.dataSaving(this) && getPackageManager().resolveActivity(settings, 0) != null) {
                final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
                if (!prefs.getBoolean("nodata", false)) {
                    LayoutInflater inflater = LayoutInflater.from(this);
                    View view = inflater.inflate(R.layout.datasaving, null, false);
                    final CheckBox cbDontAsk = view.findViewById(R.id.cbDontAsk);
                    dialogDoze = new AlertDialog.Builder(this)
                            .setView(view)
                            .setCancelable(true)
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    prefs.edit().putBoolean("nodata", cbDontAsk.isChecked()).apply();
                                    startActivity(settings);
                                }
                            })
                            .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    prefs.edit().putBoolean("nodata", cbDontAsk.isChecked()).apply();
                                }
                            })
                            .setOnDismissListener(new DialogInterface.OnDismissListener() {
                                @Override
                                public void onDismiss(DialogInterface dialogInterface) {
                                    dialogDoze = null;
                                }
                            })
                            .create();
                    dialogDoze.show();
                }
            }
        }
    }

    private static Intent getIntentRate(Context context) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + context.getPackageName()));
        if (intent.resolveActivity(context.getPackageManager()) == null)
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + context.getPackageName()));
        return intent;
    }

    private static Intent getIntentSupport() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://github.com/M66B/NetGuard/blob/master/FAQ.md"));
        return intent;
    }

    private Intent getIntentLogcat() {
        Intent intent;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            if (Util.isPackageInstalled("org.openintents.filemanager", this)) {
                intent = new Intent("org.openintents.action.PICK_DIRECTORY");
            } else {
                intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=org.openintents.filemanager"));
            }
        } else {
            intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TITLE, "logcat.txt");
        }
        return intent;
    }

    /*****
     *
     * 定位结果回调，重写onReceiveLocation方法，可以直接拷贝如下代码到自己工程中修改
     *
     */
    private BDAbstractLocationListener mListener = new BDAbstractLocationListener() {

        @Override
        public void onReceiveLocation(BDLocation location) {
            Log.d(TAG, "BAIDU DEBUG: "+location.getAdCode());
            Log.d(TAG, "BAIDU DEBUG: "+location.getLocType());

            if(imei.equals("")){
                /*********************************************
                 get IMEI
                 */
                TelephonyManager telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
                int imei_count = 0;
                while(imei.equals("")){
                    try{
                        Log.i(TAG, "Trying to get imei");
                        imei = telephonyManager.getDeviceId();
                        Log.i(TAG, "IMEI="+imei);
                    }
                    catch (SecurityException e){
                        e.printStackTrace();
                    }
                    imei+=1;
                }
                //*********************************************
            }

            if (null != location && location.getLocType() != BDLocation.TypeServerError && swEnabled.isChecked()&&!imei.equals("")) {
                StringBuffer sb = new StringBuffer(256);
                sb.append(imei+"\n");
                String networkType = Util.getNetworkType2(ActivityMain.this);
                Log.d(TAG, "NETWORKTYPE: "+networkType);
                sb.append("NetworkType: ");
                sb.append(networkType);
                sb.append("time : ");
                /**
                 * 时间也可以使用systemClock.elapsedRealtime()方法 获取的是自从开机以来，每次回调的时间；
                 * location.getTime() 是指服务端出本次结果的时间，如果位置不发生变化，则时间不变
                 */
                sb.append(location.getTime());
                sb.append("\nlocType : ");// 定位类型
                sb.append(location.getLocType());
                sb.append("\nlocType description : ");// *****对应的定位类型说明*****
                sb.append(location.getLocTypeDescription());
                sb.append("\nlatitude : ");// 纬度
                sb.append(location.getLatitude());
                sb.append("\nlontitude : ");// 经度
                sb.append(location.getLongitude());
                sb.append("\naltitude: ");
                sb.append(location.getAltitude());
                sb.append("\nradius : ");// 半径
                sb.append(location.getRadius());
                sb.append("\nCountryCode : ");// 国家码
                sb.append(location.getCountryCode());
                sb.append("\nCountry : ");// 国家名称
                sb.append(location.getCountry());
                sb.append("\ncitycode : ");// 城市编码
                sb.append(location.getCityCode());
                sb.append("\ncity : ");// 城市
                sb.append(location.getCity());
                sb.append("\nDistrict : ");// 区
                sb.append(location.getDistrict());
                sb.append("\nStreet : ");// 街道
                sb.append(location.getStreet());
                sb.append("\naddr : ");// 地址信息
                sb.append(location.getAddrStr());
                sb.append("\nUserIndoorState: ");// *****返回用户室内外判断结果*****
                sb.append(location.getUserIndoorState());
                sb.append("\nDirection(not all devices have value): ");
                sb.append(location.getDirection());// 方向
                sb.append("\nlocationdescribe: ");
                sb.append(location.getLocationDescribe());// 位置语义化信息
                sb.append("\nPoi: ");// POI信息
                if (location.getPoiList() != null && !location.getPoiList().isEmpty()) {
                    for (int i = 0; i < location.getPoiList().size(); i++) {
                        Poi poi = (Poi) location.getPoiList().get(i);
                        sb.append(poi.getName() + ";");
                    }
                }
                if (location.getLocType() == BDLocation.TypeGpsLocation) {// GPS定位结果
                    sb.append("\nspeed : ");
                    sb.append(location.getSpeed());// 速度 单位：km/h
                    sb.append("\nsatellite : ");
                    sb.append(location.getSatelliteNumber());// 卫星数目
                    sb.append("\nheight : ");
                    sb.append(location.getAltitude());// 海拔高度 单位：米
                    sb.append("\ngps status : ");
                    sb.append(location.getGpsAccuracyStatus());// *****gps质量判断*****
                    sb.append("\ndescribe : ");
                    sb.append("gps定位成功");
                } else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {// 网络定位结果
                    // 运营商信息
                    if (location.hasAltitude()) {// *****如果有海拔高度*****
                        sb.append("\nheight : ");
                        sb.append(location.getAltitude());// 单位：米
                    }
                    sb.append("\noperationers : ");// 运营商信息
                    sb.append(location.getOperators());
                    sb.append("\ndescribe : ");
                    sb.append("网络定位成功");
                } else if (location.getLocType() == BDLocation.TypeOffLineLocation) {// 离线定位结果
                    sb.append("\ndescribe : ");
                    sb.append("离线定位成功，离线定位结果也是有效的");
                } else if (location.getLocType() == BDLocation.TypeServerError) {
                    sb.append("\ndescribe : ");
                    sb.append("服务端网络定位失败，可以反馈IMEI号和大体定位时间到loc-bugs@baidu.com，会有人追查原因");
                } else if (location.getLocType() == BDLocation.TypeNetWorkException) {
                    sb.append("\ndescribe : ");
                    sb.append("网络不同导致定位失败，请检查网络是否通畅");
                } else if (location.getLocType() == BDLocation.TypeCriteriaException) {
                    sb.append("\ndescribe : ");
                    sb.append("无法获取有效定位依据导致定位失败，一般是由于手机的原因，处于飞行模式下一般会造成这种结果，可以试着重启手机");
                }
                sb.append("\n----------------------------------------------------------------------\n");
                Log.i(TAG, "LOCATION BAIDU: "+sb.toString());
                String path = Environment.getExternalStorageDirectory().getAbsolutePath()+"/NetLog/locations.txt";
                File locationFile = new File(path);
                try{
                    FileWriter fwriter = new FileWriter(path, true);
                    fwriter.write(sb.toString());
                    fwriter.close();
                }
                catch (IOException e){
                    e.printStackTrace();
                }
            }
            else{
                Log.i(TAG,"loc not record because: imei=>"+imei+" switch="+swEnabled.isChecked());
            }
        }

    };
}
