package me.yytech.audiotrackdemo;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class MultiAudioTrackDemoActivity extends ActionBarActivity implements View.OnClickListener {

    private static final String TAG = "AudioTrackPlayerDemoActivity";
    private Button button;
    private ArrayList<AudioTrack> audioTracks = new ArrayList<AudioTrack>();
    private ExecutorService executorService; //线程池

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.activity_my);
        this.button = (Button) super.findViewById(R.id.button);
        this.button.setOnClickListener(this);
        executorService = Executors.newFixedThreadPool(2); //固定大小线程池，两条线程，背景音乐与前景音效

    }

    public void onClick(View view) {
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                startTrack(new int[]{R.raw.sound, R.raw.sound});
            }
        });
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                startTrack(new int[]{R.raw.bg});
            }
        });

    }

    private void startTrack(int[] resource) {
        // 必须使用MODE_STREAM
        // MODE_STATIC必须一次性读入
        // MODE_STREAM以流方式逐步读入
        AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, 11025,
                AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT,
                1024 * 10, AudioTrack.MODE_STREAM);
        audioTrack.play();
        InputStream is = null;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(1024 * 20);
        try {
            int b = 0;
            int i = 0;
            // 遍历资源列表
            while (i < resource.length) {
                is = getResources().openRawResource(resource[i]);
                while ((b = is.read()) != -1) {
                    outputStream.write(b);
                }
                byte[] bytes = outputStream.toByteArray();
                audioTrack.write(bytes, 0, bytes.length);
                outputStream.reset();
                i++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        audioTracks.add(audioTrack);
    }

    public void onPause() {
        super.onPause();
        // 释放资源
        for (AudioTrack audioTrack : audioTracks) {
            audioTrack.stop();
            audioTrack.release();
        }
    }

}
