package edu.nudt.netlog;

import android.util.Log;

import java.io.File;
import java.io.IOException;

import java.io.IOException;

import cz.msebera.android.httpclient.entity.mime.MultipartEntityBuilder;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class uploadFile {
    OkHttpClient client = new OkHttpClient();
    public static final String TYPE_PCAP = "application/octet-stream";
    public static final String TYPE_TEXT = "application/form-data";

    public void uploadFile(String fileName, String url, boolean is_pcap){

        Log.i("uploading",fileName+" to "+url);
        final File file = new File(fileName);

        RequestBody fileBody;
        if(is_pcap) {
            fileBody = RequestBody.create(MediaType.parse(TYPE_PCAP), file);
        }
        else{
            fileBody = RequestBody.create(MediaType.parse(TYPE_TEXT),file);
        }

        RequestBody requestBody = new MultipartBody.Builder().addFormDataPart("filename", file.getName(),fileBody).build();

        Request requestPostFile = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();
        client.newCall(requestPostFile).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i("uploadFile","upload failed.");
                System.out.println("After uploading and restarting have been finished, delete those files that are of no use");
                file.delete();
                System.out.println("Delete has been finished.");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                System.out.println("ABCDEFG"+response.toString());
                Log.i("uploadFile","BIG SUCCESS!!!");
                System.out.println("After uploading and restarting have been finished, delete those files that are of no use");
                file.delete();
                System.out.println("Delete has been finished.");
            }
        });
    }
}
