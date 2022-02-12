package com.example.firechat.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.Animator;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.firechat.R;

public class Splash_Activity extends AppCompatActivity {

    private static final String TAG = Splash_Activity.class.getSimpleName();

    final int ANIM_DURATION = 2900;

    private ImageView logo,star;
    private MediaPlayer musicSound;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        findViews();
        star.setVisibility(View.INVISIBLE);
        showViewSlideDown(logo);

    }
    public void showViewSlideDown(final View v) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        v.animate()
                .scaleY(1.0f)
                .scaleX(1.0f)
                .translationY(0)
                .setDuration(ANIM_DURATION)
                .setInterpolator(new AccelerateInterpolator())
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        playMusicSoundAndToast();
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        animationDone();
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });
    }

    private void animationDone() {
        openMainActivity();
    }

    private void openMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
        finish();
    }
    private void playMusicSoundAndToast() {
        musicSound = MediaPlayer.create(this,R.raw.splash_sound);
        musicSound.start();
        Toast.makeText(this,"Loading...",Toast.LENGTH_LONG).show();
    }
    private void findViews() {
        logo = findViewById(R.id.splash_IMG_logo);
        star = findViewById(R.id.splash_IMG_star);
    }
}