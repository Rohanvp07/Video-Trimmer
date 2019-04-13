package com.example.rohan.videotrimmer;

import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

public class ThumbnailsActivity extends AppCompatActivity {

    private String path;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thumbnails);

        path=getIntent().getStringExtra("path");

        ImageView thumbnail_mini = (ImageView)findViewById(R.id.Thumbnail);

        Bitmap bitmap=ThumbnailUtils.createVideoThumbnail(path,MediaStore.Video.Thumbnails.MINI_KIND);

        thumbnail_mini.setImageBitmap(bitmap);



    }
}
