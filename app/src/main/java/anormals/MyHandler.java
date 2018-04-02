package anormals;

import android.content.Context;
import android.database.Cursor;

import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eu.faircode.netguard.DatabaseHelper;
import eu.faircode.netguard.FilterObject;
import eu.faircode.netguard.Util;

/**
 * Created by techartisan on 30/03/2018.
 */

public class MyHandler {
    public static void handleFile(Context activityLog, OutputStream out2, OutputStream out3) throws IOException {

        Cursor cursor = DatabaseHelper.getInstance(activityLog).getLog(true, true, true, true, false);
        int colTime = cursor.getColumnIndex("time");
        int colVersion = cursor.getColumnIndex("version");
        int colProtocol = cursor.getColumnIndex("protocol");
        int colSAddr = cursor.getColumnIndex("saddr");
        int colSPort = cursor.getColumnIndex("sport");
        int colDAddr = cursor.getColumnIndex("daddr");
        int colDPort = cursor.getColumnIndex("dport");
        int colUid = cursor.getColumnIndex("uid");

        List<FilterObject> filterObjects = new ArrayList<>();

        byte[] buf2;
        while (cursor.moveToNext()) {
            int version = (cursor.isNull(colVersion) ? -1 : cursor.getInt(colVersion));
            int protocol = (cursor.isNull(colProtocol) ? -1 : cursor.getInt(colProtocol));
            String protocol_name = Util.getProtocolName(protocol, version, false);
            String daddr = cursor.getString(colDAddr);
            int dport = (cursor.isNull(colDPort) ? -1 : cursor.getInt(colDPort));
            String saddr = cursor.getString(colSAddr);
            int sport = (cursor.isNull(colSPort) ? -1 : cursor.getInt(colSPort));
            String app = Util.getPackageNameByUid(activityLog, cursor.getInt(colUid));
            String a = new SimpleDateFormat("HH:mm:ss").format(cursor.getLong(colTime))
                    + ", " + app
                    + ", " + protocol_name
                    + ", " + saddr + ":" + sport + "==>>" + daddr + ":" + dport;

            FilterObject filterObject = new FilterObject(app, saddr, daddr, sport, dport, protocol_name.toLowerCase().contains("tcp"));
            if (!filterObjects.contains(filterObject)) {
                filterObjects.add(filterObject);
            }

            buf2 = a.getBytes();
            out2.write(buf2);
            out2.write("\n".getBytes());
        }

        Map<String, String> filterMap = new HashMap<>();
        for (FilterObject filterObject : filterObjects) {
            String filter_str = filterObject.toString();
            String app = filterObject.getApp();
            if (filterMap.containsKey(app)) {
                filterMap.put(app, filterMap.get(app)+"||"+filter_str);
            } else
                filterMap.put(app, filter_str);
        }

        for (String i : filterMap.keySet()) {
            String filter = i+":"+filterMap.get(i);
            buf2 = filter.getBytes();
            out3.write(buf2);
            out3.write("\n".getBytes());
        }
    }
}
