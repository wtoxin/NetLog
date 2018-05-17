package edu.nudt.netlog;



import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;

import static edu.nudt.netlog.ActivityMain.wifiConnected;

/**
 * @author ainassine
 * @version 0.0.1
 * @date 2018-04-21
 * created for timing tasks, use this class to trigger timing msg
 */
public class TimerService extends Service {
    private String TAG="TimerService";
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d("UPLOADING TAG","Time is: " + new Date().toString());
                uploadpcap();
                //getGPSLocation(TimerService.this);
            }
        }).start();

        AlarmManager manager = (AlarmManager)getSystemService(ALARM_SERVICE);
        int time = 300000;
        //int time = 30000;
        long triggerAtTime = SystemClock.elapsedRealtime() + time;
        Intent i = new Intent(this, AlarmReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(this, 0, i, 0);
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi);

        return super.onStartCommand(intent, flags, startId);
    }

    private void updateAdapter(){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        boolean resolve = prefs.getBoolean("resolve", false);
        boolean organization = prefs.getBoolean("organization", false);

        boolean udp = prefs.getBoolean("proto_udp", true);
        boolean tcp = prefs.getBoolean("proto_tcp", true);
        boolean other = prefs.getBoolean("proto_other", true);
        boolean allowed = prefs.getBoolean("traffic_allowed", true);
        boolean blocked = prefs.getBoolean("traffic_blocked", true);
        AdapterLog adapter = new AdapterLog(this, DatabaseHelper.getInstance(this).getLog(udp, tcp, other, allowed, blocked), resolve, organization);

        if (adapter != null) {
            udp = prefs.getBoolean("proto_udp", true);
            tcp = prefs.getBoolean("proto_tcp", true);
            other = prefs.getBoolean("proto_other", true);
            allowed = prefs.getBoolean("traffic_allowed", true);
            blocked = false;
            adapter.changeCursor(DatabaseHelper.getInstance(this).getLog(udp, tcp, other, allowed, blocked));
        }
    }

    private void cleanup(){
        final File pcap_file = new File(getDir("data", MODE_PRIVATE), "netguard.pcap");

        new AsyncTask<Object, Object, Object>() {
            @Override
            protected Object doInBackground(Object... objects) {
                DatabaseHelper.getInstance(TimerService.this).clearLog(-1);
                if (true) {
                    ServiceSinkhole.setPcap(false, TimerService.this);
                    if (pcap_file.exists() && !pcap_file.delete())
                        Log.w(TAG, "Delete PCAP failed");
                    ServiceSinkhole.setPcap(true, TimerService.this);
                } else {
                    if (pcap_file.exists() && !pcap_file.delete())
                        Log.w(TAG, "Delete PCAP failed");
                }
                return null;
            }

            @Override
            protected void onPostExecute(Object result) {
                updateAdapter();
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void uploadpcap(){
        OutputStream out = null;
        OutputStream out2 = null;
        OutputStream out3 = null;
        FileInputStream in = null;
        // JQ Mod, modification according to directory change.
        String currentTime = ""+System.currentTimeMillis();
        String path = Environment.getExternalStorageDirectory().getAbsolutePath()+"/NetLog/netlog"+currentTime+".pcap";
        String path_log = path.replace(".pcap", "_log.txt");
        String path_filter = path.replace(".pcap", "_filter.txt");
        String path_location = Environment.getExternalStorageDirectory().getAbsolutePath()+"/NetLog/locations.txt";

        //rename locations.txt to netlog+currentTimeMillis+.loc
        String path_location_n = Environment.getExternalStorageDirectory().getAbsolutePath()+"/NetLog/netlog"+currentTime+".loc";
        Log.i(TAG, "Renaming locations.txt to "+path_location_n);
        File locFile = new File(path_location);
        File newLocFile = new File(path_location_n);
        locFile.renameTo(newLocFile);

        try {
            // Stop capture
            ServiceSinkhole.setPcap(false, TimerService.this);

//                    Uri target = data.getData();
//                    Uri target_log = Uri.parse(target.toString().replace(".pcap", "_log.txt"));
//                    Uri target_filter = Uri.parse(target.toString().replace(".pcap", "_filter.txt"));
//                    if (data.hasExtra("org.openintents.extra.DIR_PATH")) {
//                        target = Uri.parse(target + "/netguard.pcap");
//                        target_log = Uri.parse(target + "/netguard_log.txt");
//                        target_filter = Uri.parse(target + "/netguard_filter.txt");
//                    }
            Log.i(TAG, "Export PCAP URI=" + path);
            out = new FileOutputStream(path);
            out2 = new FileOutputStream(path_log);
            out3 = new FileOutputStream(path_filter);
            MyHandler.handleFile(TimerService.this, out2, out3);

            File pcap = new File(getDir("data", MODE_PRIVATE), "netguard.pcap");
            in = new FileInputStream(pcap);

            int len;
            long total = 0;
            byte[] buf = new byte[4096];
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
                total += len;
            }
            Log.i(TAG, "Copied bytes=" + total);

        } catch (Throwable ex) {
            Log.e(TAG, ex.toString() + "\n" + Log.getStackTraceString(ex));
        } finally {
            if (out != null)
                try {
                    out.close();
                } catch (IOException ex) {
                    Log.e(TAG, ex.toString() + "\n" + Log.getStackTraceString(ex));
                }
            if (out2 != null)
                try {
                    out2.close();
                } catch (IOException ex) {
                    Log.e(TAG, ex.toString() + "\n" + Log.getStackTraceString(ex));
                }
            if (out3 != null)
                try {
                    out3.close();
                } catch (IOException ex) {
                    Log.e(TAG, ex.toString() + "\n" + Log.getStackTraceString(ex));
                }
            if (in != null)
                try {
                    in.close();
                } catch (IOException ex) {
                    Log.e(TAG, ex.toString() + "\n" + Log.getStackTraceString(ex));
                }
            cleanup();

            File tmpfile = new File(path_filter);
            wifiConnected = Util.isWifiActive(this);

            if (tmpfile.length() != 0 && wifiConnected) {
                uploadFile uf = new uploadFile();
                //uf.uploadFile(path, "http://192.168.1.7:5000/", true);
                //uf.uploadFile(path_log, "http://192.168.1.7:5000/", false);
                //uf.uploadFile(path_filter, "http://192.168.1.7:5000/", false);
                //http://118.24.12.193/
                //http://192.168.43.137
                String ip = PreferenceManager.getDefaultSharedPreferences(this).getString("set_server_ip", "39.106.16.105");
                String port = PreferenceManager.getDefaultSharedPreferences(this).getString("set_server_port", "5000");
                String url = "http://"+ip+":"+port+"/";
                uf.uploadFile(path, url, true);
                uf.uploadFile(path_log, url, false);
                uf.uploadFile(path_filter, url, false);
                uf.uploadFile(path_location_n, url, false);
            }else{
                System.out.println("After uploading and restarting have been finished, delete those files that are of no use");
                tmpfile = new File(path);
                tmpfile.delete();
                tmpfile = new File(path_log);
                tmpfile.delete();
                tmpfile = new File(path_filter);
                tmpfile.delete();
                tmpfile = new File(path_location_n);
                tmpfile.delete();
                System.out.println("Delete has been finished.");
            }
            // Resume capture
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(TimerService.this);
            if (true)
                ServiceSinkhole.setPcap(true, TimerService.this);
        }
    }

    private void getGPSLocation(Context context){
        double latitude;
        double altitude;
        double longitude;
        Location location=null;
        List<String> providers;

        LocationManager locationManager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);

        providers = locationManager.getProviders(true);
        Location bestLocation = null;

        if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            Log.e(TAG, "GPS not enabled.");
        }
        else if(!locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
            Log.e(TAG, "NETWORK not enabled.");
        }
        else{
            try{
                while(bestLocation==null) {
                    for (String provider : providers) {
                        location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        if (location == null)
                            continue;

                        if (bestLocation == null || location.getAccuracy() < bestLocation.getAccuracy()) {
                            // Found best last known location: %s", l);
                            bestLocation = location;
                        }

                    }
                }
                latitude = bestLocation.getLatitude();
                altitude = bestLocation.getAltitude();
                longitude = bestLocation.getLongitude();
                System.out.println("####HHHH, GPS LOCATION GOT: longitude: "+longitude+", altitude: "+altitude+", latitude: "+latitude );
            }
            catch (SecurityException e){
                e.printStackTrace();
            }
        }
    }
}