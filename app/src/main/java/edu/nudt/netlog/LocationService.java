package edu.nudt.netlog;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

/**
 *
 * @author baidu
 *
 */
public class LocationService {
    private LocationClient client = null;
    private LocationClientOption mOption,DIYoption;
    private Object  objLock = new Object();

    private NotificationUtils mNotificationUtils;
    private Notification notification;
    /***
     *
     * @param locationContext
     */
    public LocationService(Context locationContext){
        synchronized (objLock) {
            if(client == null){
                client = new LocationClient(locationContext);
                client.setLocOption(getDefaultLocationClientOption());
            }
        }
    }

    /***
     *
     * @param listener
     * @return
     */

    public boolean registerListener(BDAbstractLocationListener listener){
        boolean isSuccess = false;
        if(listener != null){
            client.registerLocationListener(listener);
            isSuccess = true;
        }
        return  isSuccess;
    }

    public void unregisterListener(BDAbstractLocationListener listener){
        if(listener != null){
            client.unRegisterLocationListener(listener);
        }
    }

    /***
     *
     * @param option
     * @return isSuccessSetOption
     */
    public boolean setLocationOption(LocationClientOption option){
        boolean isSuccess = false;
        if(option != null){
            if(client.isStarted())
                client.stop();
            DIYoption = option;
            client.setLocOption(option);
        }
        return isSuccess;
    }

    /***
     *
     * @return DefaultLocationClientOption  默认O设置
     */
    public LocationClientOption getDefaultLocationClientOption(){
        if(mOption == null){
            mOption = new LocationClientOption();
            mOption.setLocationMode(LocationMode.Hight_Accuracy);//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
            mOption.setCoorType("bd09ll");//可选，默认gcj02，设置返回的定位结果坐标系，如果配合百度地图使用，建议设置为bd09ll;
            mOption.setScanSpan(10000);//可选，默认0，即仅定位一次，设置发起连续定位请求的间隔需要大于等于1000ms才是有效的
            mOption.setIsNeedAddress(true);//可选，设置是否需要地址信息，默认不需要
            mOption.setIsNeedLocationDescribe(true);//可选，设置是否需要地址描述
            mOption.setNeedDeviceDirect(false);//可选，设置是否需要设备方向结果
            mOption.setLocationNotify(false);//可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
            mOption.setIgnoreKillProcess(true);//可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
            mOption.setIsNeedLocationDescribe(true);//可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
            mOption.setIsNeedLocationPoiList(true);//可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
            mOption.SetIgnoreCacheException(false);//可选，默认false，设置是否收集CRASH信息，默认收集
            mOption.setOpenGps(true);//可选，默认false，设置是否开启Gps定位
            mOption.setIsNeedAltitude(true);//可选，默认false，设置定位时是否需要海拔信息，默认不需要，除基础定位版本都可用

        }
        return mOption;
    }


    /**
     *
     * @return DIYOption 自定义Option设置
     */
    public LocationClientOption getOption(){
        if(DIYoption == null) {
            DIYoption = new LocationClientOption();
        }
        return DIYoption;
    }

    public void start(Context locationContext){
        synchronized (objLock) {
            if(client != null && !client.isStarted()){
                client.start();


                //设置后台定位
                //android8.0及以上使用NotificationUtils
                if (Build.VERSION.SDK_INT >= 26) {
                    mNotificationUtils = new NotificationUtils(locationContext);
                    Notification.Builder builder2 = mNotificationUtils.getAndroidChannelNotification
                            ("适配android 8限制后台定位功能", "正在后台定位");
                    notification = builder2.build();
                } else {
                    //获取一个Notification构造器
                    Notification.Builder builder = new Notification.Builder(locationContext);
                    Intent nfIntent = new Intent(locationContext, ActivityMain.class);

                    builder.setContentIntent(PendingIntent.
                            getActivity(locationContext, 0, nfIntent, 0)) // 设置PendingIntent
                            .setContentTitle("适配android 8限制后台定位功能") // 设置下拉列表里的标题
                            .setSmallIcon(R.mipmap.ic_launcher)// 设置状态栏内的小图标
                            .setContentText("正在后台定位") // 设置上下文内容
                            .setWhen(System.currentTimeMillis()); // 设置该通知发生的时间

                    notification = builder.build(); // 获取构建好的Notification
                }
                notification.defaults = Notification.DEFAULT_SOUND; //设置为默认的声音

                client.enableLocInForeground(1, notification);


            }
        }
    }
    public void stop(){
        synchronized (objLock) {
            if(client != null && client.isStarted()){
                client.stop();
            }
        }
    }

    public boolean isStart() {
        return client.isStarted();
    }

    public boolean requestHotSpotState(){
        return client.requestHotSpotState();
    }

}

