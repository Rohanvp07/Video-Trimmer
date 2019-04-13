package com.example.rohan.videotrimmer;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.VideoView;

import org.florescu.android.rangeseekbar.RangeSeekBar;

import java.io.File;

public class TrimActivity extends AppCompatActivity {

    Uri uri;
    private ImageView imageView;
    private VideoView videoView;
    private TextView left;
    private TextView right;
    private RangeSeekBar rangeSeekBar;

    private int duration;
    private String fileprefix;
    private String[] command;
    private File dest;
    private String originalpath;
    private boolean isplaying=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trim);

        imageView=(ImageView)findViewById(R.id.pausebtn);
        videoView=(VideoView)findViewById(R.id.videoview);
        left=(TextView)findViewById(R.id.left);
        right=(TextView)findViewById(R.id.right);
        rangeSeekBar=(RangeSeekBar)findViewById(R.id.seekbar);


        String path = getIntent().getStringExtra("uri");
        uri=Uri.parse(path);
        isplaying=true;
        videoView.setVideoURI(uri);
        videoView.start();

        settingalllisteners();


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
         super.onOptionsItemSelected(item);

         if(item.getItemId()==R.id.trim)
         {
             AlertDialog.Builder builder=new AlertDialog.Builder(TrimActivity.this);

             LinearLayout linearLayout=new LinearLayout(TrimActivity.this);
             linearLayout.setOrientation(LinearLayout.VERTICAL);
             LinearLayout.LayoutParams layoutParams=new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
             layoutParams.setMargins(50,0,50,100);

             final EditText newvideoname=new EditText(TrimActivity.this);
             newvideoname.setLayoutParams(layoutParams);
             newvideoname.setGravity(Gravity.TOP|Gravity.START);
             newvideoname.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);

             linearLayout.addView(newvideoname,layoutParams);

             builder.setMessage("Wanna set a new video name?");
             builder.setTitle("Change video name");
             builder.setView(linearLayout);



             builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                 @Override
                 public void onClick(DialogInterface dialog, int which) {

                     dialog.dismiss();
                 }
             });

             builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                 @Override
                 public void onClick(DialogInterface dialog, int which) {


                     fileprefix=newvideoname.getText().toString();

                     trimmingvideo(rangeSeekBar.getSelectedMinValue().intValue()*1000,rangeSeekBar.getAbsoluteMaxValue().intValue()*1000,fileprefix);


                     Intent intent=new Intent(TrimActivity.this,ProgressBarActivity.class);
                     intent.putExtra("duration",duration);
                     intent.putExtra("command",command);
                     intent.putExtra("destination",dest.getAbsolutePath());
                     startActivity(intent);
                     finish();
                     dialog.dismiss();

                 }
             });
             builder.show();
          }
         return true;
    }

    private void trimmingvideo(int start, int end, String filename)
    {
        File folder=new File(Environment.getExternalStorageDirectory()+"/VideoTrimmer");
        if(!folder.exists())
        {
            folder.mkdir();
        }

        fileprefix=filename;
        String filext=".mp4";
        dest=new File(folder,fileprefix + filext);
        originalpath=getRealPathfromuri(getApplicationContext(),uri);

        duration= (end - start)/1000;

        command=new String[]{"-ss", "" + start / 1000, "-y", "-i", originalpath, "-t", "" + (end - start) / 1000, "-vcodec", "mpeg4", "-b:v", "2097152", "-b:a", "48000", "-ac", "2", "-ar", "22050", dest.getAbsolutePath()};


    }

    private String getRealPathfromuri(Context context, Uri uri)
    {
        Cursor cursor=null;

        try
        {
            String[] proj = {MediaStore.Images.Media.DATA};

            cursor = context.getContentResolver().query(uri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

            cursor.moveToFirst();

            return cursor.getString(column_index);


        }
        catch (Exception e)
        {
            e.printStackTrace();
            return "";
        }
        finally
        {
            if(cursor!=null)
            {
                cursor.close();
            }
        }
    }

    private void settingalllisteners()
    {
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                if(isplaying) {
                    imageView.setImageResource(R.drawable.ic_play_circle_filled_black_24dp);
                    videoView.pause();
                    isplaying = false;
                }
                else
                {
                    videoView.start();
                    imageView.setImageResource(R.drawable.ic_pause_circle_filled_black_24dp);
                    isplaying=true;
                }
            }

        });



        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {

                videoView.start();
                duration=mp.getDuration()/1000;
                left.setText("00:00:00");
                right.setText(Time(duration));

                mp.setLooping(true);

                rangeSeekBar.setRangeValues(0,duration);
                rangeSeekBar.setSelectedMaxValue(duration);
                rangeSeekBar.setSelectedMinValue(0);
                rangeSeekBar.setEnabled(true);

                rangeSeekBar.setOnRangeSeekBarChangeListener(new RangeSeekBar.OnRangeSeekBarChangeListener() {
                    @Override
                    public void onRangeSeekBarValuesChanged(RangeSeekBar bar, Object minValue, Object maxValue) {

                        videoView.seekTo((int)minValue*1000);

                        left.setText(Time((int)bar.getSelectedMinValue()));
                        right.setText(Time((int)bar.getSelectedMaxValue()));

                    }
                });

                Handler handler=new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        if(videoView.getCurrentPosition()>=rangeSeekBar.getSelectedMaxValue().intValue()*1000)
                            videoView.seekTo(rangeSeekBar.getSelectedMinValue().intValue()*1000);



                    }
                },1000);

            }


        });


    }

    private String Time(int seconds)
    {
        int hr=seconds/3600;
        int rem=seconds % 3600;
        int min=rem/60;
        int sec=rem % 60;

        return String.format("%02d",hr)+ ":"+ String.format("%02d",min)+ ":"+ String.format("%02d",sec);
    }
}
