package com.apaulling.naloxalocate.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.apaulling.naloxalocate.R;

import java.util.concurrent.TimeUnit;




/**
 * Created by simonmullaney on 09/12/2016.
 */

public class TimerBeginActivity extends AppCompatActivity {


    private static final String FORMAT = "%02d:%02d:%02d";
    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS =0 ;
    EditText number_text,message_text,select_time_val;
    long time_lng;
    String time_str;




    /*TextView Contact_number = (TextView)findViewById(R.id.Contact_number); */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer_begin);

        final TextView Remaining_time_val;


        Remaining_time_val = (TextView) findViewById(R.id.remaining_time_id);       //print to screen val
        number_text = (EditText) findViewById(R.id.Contact_number_id);
        message_text = (EditText) findViewById(R.id.emergency_message_id);


        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            time_str = getIntent().getStringExtra("time");
        } else time_lng = 0;

        try {
            time_lng = Long.parseLong(time_str);
        }catch (NumberFormatException nfe){
            time_lng = 5;
        }


        final CountDownTimer timer = new CountDownTimer((time_lng)*1000, 1000) { // adjust the milli seconds here
            public void onTick(long millisUntilFinished) {

                Remaining_time_val.setText("" + String.format(FORMAT,
                        TimeUnit.MILLISECONDS.toHours(millisUntilFinished),
                        TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) - TimeUnit.HOURS.toMinutes(
                                TimeUnit.MILLISECONDS.toHours(millisUntilFinished)),
                        TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) - TimeUnit.MINUTES.toSeconds(
                                TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished))));
            }

            public void onFinish() {
                Remaining_time_val.setText("----!");
                final MediaPlayer mMediaPlayer = new MediaPlayer();


                final CountDownTimer cntr_aCounter = new CountDownTimer(3000, 1000) {
                    public void onTick(long millisUntilFinished) {

                        //mMediaPlayer.start();
                        playSound(mMediaPlayer);
                    }

                    public void onFinish() {
                        //code fire after finish
                        mMediaPlayer.stop();
                        sendSMS();

                    };
                };cntr_aCounter.start();



                Button btnStopALarm = (Button) findViewById(R.id.btnStopALarm);
                btnStopALarm.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mMediaPlayer.stop();
                        cntr_aCounter.cancel();
                        TimerBeginActivity.this.startActivity(new Intent(TimerBeginActivity.this, TimerActivity.class));

                    }
                });


            }

        }.start();


        Button btnStopALarm = (Button) findViewById(R.id.btnStopALarm);
        btnStopALarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timer.cancel();
                TimerBeginActivity.this.startActivity(new Intent(TimerBeginActivity.this, TimerActivity.class));

            }
        });


    }

    //external function to send sms and play sound

    protected void sendSMS(){

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.SEND_SMS)) {
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.SEND_SMS},
                        MY_PERMISSIONS_REQUEST_SEND_SMS);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_SEND_SMS: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    SmsManager smsManager = SmsManager.getDefault();
                    String phoneNumber = number_text.getText().toString();
                    String message = message_text.getText().toString();
                    smsManager.sendTextMessage(phoneNumber, null, message, null, null);
                    Toast.makeText(getApplicationContext(), "SMS sent.",
                            Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(),
                            "SMS faild, please try again.", Toast.LENGTH_LONG).show();
                    return;
                }
            }
        }

    }



    public void playSound(MediaPlayer med) {

        MediaPlayer mMediaPlayer = med;

        try {
            Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            mMediaPlayer.setDataSource(this, alert);
            final AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            if (audioManager.getStreamVolume(AudioManager.STREAM_ALARM) != 0) {
                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
                mMediaPlayer.setLooping(true);
                mMediaPlayer.prepare();
                mMediaPlayer.start();
            }
        } catch (Exception e) {
        }
    }

}