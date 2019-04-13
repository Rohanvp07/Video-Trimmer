package com.example.rohan.videotrimmer;

import android.app.Activity;
import android.app.Service;
import android.arch.lifecycle.MutableLiveData;
import android.content.Intent;
import android.icu.text.StringSearch;
import android.os.Binder;
import android.os.IBinder;

import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;

import javax.security.auth.callback.Callback;

public class ffmpegservice extends Service
{

    FFmpeg fFmpeg;
    int duration;
    String[] command;
    Callbacks activity;

    public MutableLiveData<Integer> percentage;
    IBinder iBinder=new Localbinder();

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent!=null)
        {
            duration= Integer.parseInt(intent.getStringExtra("duration"));
            command=intent.getStringArrayExtra("command");
            try {
                loadffmpegbinary();
                execFFmpegCommand();

            } catch (FFmpegNotSupportedException e) {
                e.printStackTrace();
            } catch (FFmpegCommandAlreadyRunningException e) {
                e.printStackTrace();
            }
        }

        return super.onStartCommand(intent, flags, startId);

    }

    private void execFFmpegCommand() throws FFmpegCommandAlreadyRunningException {
        fFmpeg.execute(command,new ExecuteBinaryResponseHandler(){

            @Override
            public void onFailure(String message) {
                super.onFailure(message);
            }

            @Override
            public void onSuccess(String message) {
                super.onSuccess(message);
            }

            @Override
            public void onProgress(String message) {

                String arr[];
                if(message.contains("time="))
                {
                    arr= message.split("time=");
                    String y=arr[1];
                    String arr2[] = y.split(":");
                    String[] y2=arr2[2].split(" ");

                    String second= y2[0];
                    int hr=Integer.parseInt(arr2[0]);

                    hr=hr*3600;
                    int min=Integer.parseInt(arr2[1]);
                    min=min*60;

                    float sec=Float.valueOf(second);

                    float timeInsec = hr+min+sec;
                    percentage.setValue((int)((timeInsec/duration)*100));
                }
            }

            @Override
            public void onStart() {
                super.onStart();
            }

            @Override
            public void onFinish() {
                percentage.setValue(100);
            }
        });
    }

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            loadffmpegbinary();
        } catch (FFmpegNotSupportedException e) {
            e.printStackTrace();
        }
        percentage=new MutableLiveData<>();
    }

    private void loadffmpegbinary() throws FFmpegNotSupportedException {
        if(fFmpeg==null)
        {
            fFmpeg = FFmpeg.getInstance(this);
        }
        fFmpeg.loadBinary(new LoadBinaryResponseHandler()
        {
            @Override
            public void onFailure() {
                super.onFailure();
            }

            @Override
            public void onSuccess() {
                super.onSuccess();
            }
        });
    }

    public ffmpegservice()
    {
        super();
    }

    public class Localbinder extends Binder
    {
        public ffmpegservice getServiceInstance()
        {
         return ffmpegservice.this;
        }
    }

    public void registerclient(Activity activity)
    {
        this.activity  = (Callbacks)activity;
    }


    @Override
    public IBinder onBind(Intent intent) {
        return iBinder;
    }

    public MutableLiveData<Integer> getPercentage()
    {

        return percentage;
    }

    public interface Callbacks
    {
        void updateclient(float data);
    }

}
