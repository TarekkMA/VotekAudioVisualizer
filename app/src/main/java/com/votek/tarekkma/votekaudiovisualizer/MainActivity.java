package com.votek.tarekkma.votekaudiovisualizer;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.TextView;

import com.alex.siriwaveview.SiriWaveView;

import java.io.File;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    private enum AudioSource {
        FROM_FILE,FROM_RECORD
    }

    private static final String TAG = "MainActivity";

    private AudioProcessor audioProcessor;

    //Views
    @BindView(R.id.waveView)
    SiriWaveView waveView;

    @BindView(R.id.audioMicBtn)
    FloatingActionButton audioMicBtn;
    @BindView(R.id.audioFileBtn)
    FloatingActionButton audioFileBtn;

    @BindView(R.id.audioSeekbar)
    SeekBar audioSeekbar;

    @BindView(R.id.statusText)
    TextView statusText;


    //Permissions From 6.0+ devices
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private boolean permissionToRecordAccepted = false;
    private String [] permissions = {Manifest.permission.RECORD_AUDIO};


    private AudioSource audioSource = null;

    //From Mic
    private boolean isRecording = false;
    private final String recordFileName = "recordedAudio.3gp";
    private String recordFilePath = null;
    private MediaRecorder recorder = null;

    //From File
    private String filePath = null;
    private MediaPlayer player = null;


    //
    private static final int BUFFER_SIZE = 512;


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case REQUEST_RECORD_AUDIO_PERMISSION:
                permissionToRecordAccepted  = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
        }


        if (!permissionToRecordAccepted ) finish();

    }




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Bind Views and ViewListeners
        ButterKnife.bind(this);

        //Request Permissions
        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);

        //Setup temp file to store recording
        recordFilePath = getExternalCacheDir().getAbsolutePath()
                        + File.separator + recordFileName;

        audioProcessor = new AudioProcessor(waveView);


        //waveView.startAnimation();

    }

    @OnClick(R.id.audioMicBtn)
    void handelAudioFromMicButton(){
        audioSource = AudioSource.FROM_RECORD;
        if(!isRecording){
            startRecording();
            statusText.setText("Recoreding Audio From Mic");
        }
        else {
            stopRecording();
            statusText.setText("Playing Audio Recorded");
        }
        isRecording=!isRecording;
    }

    @OnClick(R.id.audioFileBtn)
    void handelAudioFromFileButton(){

    }



    private void startRecording() {
        Log.d(TAG, "startRecording: ");
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setOutputFile(recordFilePath);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            recorder.prepare();
        } catch (IOException e) {
            Log.e(TAG, "prepare() failed");
        }

        recorder.start();
    }

    private void stopRecording() {
        Log.d(TAG, "stopRecording: ");
        recorder.stop();
        recorder.release();
        recorder = null;
        startPlaying(recordFilePath);

        //audioProcessor.prossesAudio(new File(recordFilePath));
    }



    private void startPlaying(String path) {
        Log.d(TAG, "startPlaying: ");
        player = new MediaPlayer();
        try {
            player.setDataSource(path);
            player.prepare();
            player.start();
        } catch (IOException e) {
            Log.e(TAG, "prepare() failed");
        }
    }

    private void stopPlaying() {
        Log.d(TAG, "stopPlaying: ");

        player.release();
        player = null;
    }



}
