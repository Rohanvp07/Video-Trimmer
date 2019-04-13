package com.example.rohan.videotrimmer;

import android.arch.lifecycle.Observer;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;
import android.widget.Toast;

import com.dinuscxj.progressbar.CircleProgressBar;

public class ProgressBarActivity extends AppCompatActivity {

    private CircleProgressBar circleProgressBar;
    private int duration;
    private String[] command;
    private String path;

    ServiceConnection serviceConnection;
    ffmpegservice mpegservice;
    Integer res;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progress_bar);

        circleProgressBar=(CircleProgressBar)findViewById(R.id.cirleprogress);
        circleProgressBar.setMax(100);

        final Intent i=getIntent();

        duration=i.getIntExtra("duration",0);
        command=i.getStringArrayExtra("command");
        path=i.getStringExtra("destination");

        final Intent intent=new Intent(ProgressBarActivity.this,ffmpegservice.class);
        intent.putExtra("duration",String.valueOf(duration));
        intent.putExtra("command",command);
        intent.putExtra("destination",path);
        startService(intent);


        serviceConnection=new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                ffmpegservice.Localbinder localbinder=(ffmpegservice.Localbinder)service;

                mpegservice= localbinder.getServiceInstance();
                mpegservice.registerclient(getParent());


                final Observer<Integer> resultobserver= new Observer<Integer>() {
                    @Override
                    public void onChanged(@Nullable Integer integer) {

                        res = integer;

                        if(res<100)
                        {
                            circleProgressBar.setProgress(res);
                        }
                        if(res==100)
                        {
                            circleProgressBar.setProgress(res);
                            stopService(intent);
                            Toast.makeText(getApplicationContext(),"Video trimmed successfully",Toast.LENGTH_LONG).show();

                            Intent intent1=new Intent(ProgressBarActivity.this,ThumbnailsActivity.class);
                            intent1.putExtra("path",path);
                            startActivity(intent1);
                        }
                    }
                };

                mpegservice.getPercentage().observe(ProgressBarActivity.this,resultobserver);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {


            }
        };


        bindService(intent,serviceConnection, Context.BIND_AUTO_CREATE);

    }
}
