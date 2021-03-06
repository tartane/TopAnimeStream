package com.topanimestream.views;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.topanimestream.App;
import com.topanimestream.R;
import com.topanimestream.custom.TouchImageView;

public class FullScreenImage extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_full_screen_image);

        Intent intent = getIntent();
        String ImageUrl = intent.getExtras().getString("ImageUrl");
        TouchImageView imageView = (TouchImageView) findViewById(R.id.imgFullScreen);

        imageView.setLayoutParams(new RelativeLayout.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT));
        App.imageLoader.displayImage(ImageUrl, imageView);

    }

}
