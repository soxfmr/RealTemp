package com.soxfmr.realtemp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.soxfmr.realtemp.R;
import com.soxfmr.realtemp.utils.ProcessHelper;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        super.onCreate(savedInstanceState);

        View view = getLayoutInflater().inflate(R.layout.activity_splash, null);
        setContentView(view);

        if (ProcessHelper.needStartApp(getApplicationContext())) {
            Animation animAlpha = AnimationUtils.loadAnimation(this, R.anim.alpha);
            animAlpha.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {}

                @Override
                public void onAnimationRepeat(Animation animation) {}

                @Override
                public void onAnimationEnd(Animation animation) {
                    Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                    startActivity(intent);

                    finish();
                }
            });
            view.startAnimation(animAlpha);
        } else {
            finish();
        }
    }
}
