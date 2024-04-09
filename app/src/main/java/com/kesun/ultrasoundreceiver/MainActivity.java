package com.kesun.ultrasoundreceiver;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.View;

import android.os.Bundle;
import android.widget.Button;
import android.util.Log;
import android.widget.Button;
import android.widget.ProgressBar;
import android.view.View.OnClickListener;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.widget.TextView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;


public class MainActivity extends AppCompatActivity {
    private File file;
    private String pathName;

    private Button btnPlayRecord;

    private Button btnStopRecord;

    private boolean blnPlayRecord = false;

    private AudioRecord audioRecord;

    private int recBufSize = 0;

    private int playBufSize;

    private int frameSize = 512;

    private int numfreq = 1;

    private double startfreq = 20650;//17150

    private double[] wavefreqs = new double[numfreq];

    private int sampleRateInHz = 48000;

    private int freqinter = 350;

    private int channelConfig = AudioFormat.CHANNEL_IN_MONO;

    private int encodingBitrate = AudioFormat.ENCODING_PCM_16BIT;

    private String fileName = "";

    private FileOutputStream fos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        file = getExternalFilesDir(null);
        pathName = file.getAbsolutePath() + "/SensorData/";

        btnPlayRecord = (Button) findViewById(R.id.btnplayrecord);
        btnStopRecord = (Button) findViewById(R.id.btnstoprecord);

        for (int i = 0; i < numfreq; i++) {
            wavefreqs[i] = startfreq + i * freqinter;
        }

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            recBufSize = AudioRecord.getMinBufferSize(sampleRateInHz,
                    channelConfig, encodingBitrate);
            audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                    sampleRateInHz, channelConfig, encodingBitrate, recBufSize);
        }

        btnPlayRecord.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                btnPlayRecord.setEnabled(false);
                btnStopRecord.setEnabled(true);

                playBufSize = AudioTrack.getMinBufferSize(sampleRateInHz,
                        channelConfig, encodingBitrate);

                try {
                    SimpleDateFormat format = new SimpleDateFormat("MM.dd HH_mm_ss");
                    fileName =format.format(new Date()) + ".txt";
                    File path = new File(pathName);
                    file = new File(pathName + fileName);
                    if (!path.exists())
                        path.mkdir();
                    if (!file.exists())
                        file.createNewFile();
                    fos = new FileOutputStream(file);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                new ThreadInstantPlay().start();
                new ThreadInstantRecord().start();
            }
        });

        btnStopRecord.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                btnPlayRecord.setEnabled(true);
                btnStopRecord.setEnabled(false);
                blnPlayRecord=false;
            }
        });

    }


    class ThreadInstantPlay extends Thread {
        @Override
        public void run() {
            SoundPlayer Player = new SoundPlayer(sampleRateInHz, numfreq, wavefreqs);
            blnPlayRecord = true;
            Player.play();
            while (blnPlayRecord == true) {
            }
            Player.stop();
        }
    }

    class ThreadInstantRecord extends Thread {
        @Override
        public void run() {
            short[] bsRecord = new short[recBufSize * 2];
            int datacount = 0;
            long starttime,endtime,totalstarttime;

            FileOutputStream outputStream = null;
            try {
                outputStream = new FileOutputStream(fileName); //fileName is path to a file, where audio data should be written
            } catch (FileNotFoundException e) {
                // handle error
            }

            while (blnPlayRecord == false) {
            }
            audioRecord.startRecording();
            Log.i("test", "record");
            while (blnPlayRecord) {
                /*
                 *
                 */
                int line = audioRecord.read(bsRecord, 0, frameSize * 2);
                String s = "";
                for (int i=0; i < line; ++i){
                    s += " " + Short.toString(bsRecord[i]);
                }
                byte [] buffer = s.getBytes();
                try {
                    fos.write(buffer);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                datacount = datacount + line / 2;
                Log.i("test", s);
                totalstarttime=System.currentTimeMillis();
            }
            audioRecord.stop();

            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

