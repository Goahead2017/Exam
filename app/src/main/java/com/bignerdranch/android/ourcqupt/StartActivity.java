package com.bignerdranch.android.ourcqupt;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class StartActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        ValueAnimator anim = ValueAnimator.ofFloat(0,360);
        anim.setDuration(3000);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float currentValue = (Float)animation.getAnimatedValue();
                if(currentValue == 360){
                    Intent intent = new Intent(StartActivity.this,LoginActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        });
        anim.start();

    }
}
