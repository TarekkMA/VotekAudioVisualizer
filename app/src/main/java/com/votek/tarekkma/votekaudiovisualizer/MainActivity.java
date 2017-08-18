package com.votek.tarekkma.votekaudiovisualizer;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.alex.siriwaveview.SiriWaveView;

import java.io.File;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {


    int frequency =  44100;
    int channelConfiguration = AudioFormat.CHANNEL_CONFIGURATION_MONO;
    int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
    int blockSize = 512;

    Bitmap bitmap;
    Canvas canvas;
    Paint paint;


    private RealDoubleFFT transformer;


    boolean started = false;

    private enum AudioSource {
        FROM_FILE,FROM_RECORD
    }

    private static final String TAG = "MainActivity";

    private AudioProcessor audioProcessor;

    //Views
    @BindView(R.id.waveView)
    SiriWaveView imageView;

    @BindView(R.id.audioMicBtn)
    FloatingActionButton audioMicBtn;
    @BindView(R.id.audioFileBtn)
    FloatingActionButton audioFileBtn;

    @BindView(R.id.audioSeekbar)
    SeekBar audioSeekbar;

    @BindView(R.id.statusText)
    TextView statusText;

    RecordAudio recordTask;


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

        //audioProcessor = new AudioProcessor(waveView);
        //waveView.startAnimation();

        transformer = new RealDoubleFFT(blockSize);

    }

    @OnClick(R.id.audioMicBtn)
    void handelAudioFromMicButton(){

        if (started) {
            started = false;
            recordTask.cancel(true);
        } else {
            started = true;
            recordTask = new RecordAudio();
            recordTask.execute();
        }
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

//       audioProcessor.prossesAudio(new File(recordFilePath));
/*        if(audioProcessor!=null)audioProcessor.cancel(true);
        audioProcessor = new AudioProcessor(waveView,new File(recordFilePath));
        audioProcessor.execute();*/
    }



    private void startPlaying(String path) {
        Log.d(TAG, "startPlaying: ");
        player = new MediaPlayer();
        try {
            player.setDataSource(path);
            player.prepare();
            player.start();
            //waveView.link(player);
        } catch (IOException e) {
            Log.e(TAG, "prepare() failed");
            Log.e(TAG, "startPlaying: ",e );
        }
    }

    private void stopPlaying() {
        Log.d(TAG, "stopPlaying: ");

        player.release();
        player = null;
    }


    public class RecordAudio extends AsyncTask<Void, double[], Void> {

        @Override
        protected Void doInBackground(Void... arg0) {

            try {
                // int bufferSize = AudioRecord.getMinBufferSize(frequency,
                // AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
                int bufferSize = AudioRecord.getMinBufferSize(frequency,
                        channelConfiguration, audioEncoding);

                AudioRecord audioRecord = new AudioRecord(
                        MediaRecorder.AudioSource.MIC, frequency,
                        channelConfiguration, audioEncoding, bufferSize);

                short[] buffer = new short[blockSize];
                double[] toTransform = new double[blockSize];

                audioRecord.startRecording();

                // started = true; hopes this should true before calling
                // following while loop

                while (started) {
                    int bufferReadResult = audioRecord.read(buffer, 0,
                            blockSize);

                    for (int i = 0; i < blockSize && i < bufferReadResult; i++) {
                        toTransform[i] = (double) buffer[i] / 32768.0; // signed
                        // 16
                    }                                       // bit
                    transformer.ft(toTransform);
                    publishProgress(toTransform);



                }

                audioRecord.stop();

            } catch (Throwable t) {
                t.printStackTrace();
                Log.e("AudioRecord", "Recording Failed");
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(double[]... toTransform) {
            imageView.setFrqPowers(toTransform[0]);


            /*
            canvas.drawColor(Color.BLACK);

            for (int i = 0; i < toTransform[0].length; i++) {
                int x = i;
                int downy = (int) (100 - (toTransform[0][i] * 10));
                int upy = 100;

                canvas.drawLine(x, downy, x, upy, paint);
            }

            imageView.invalidate();

            // TODO Auto-generated method stub
            // super.onProgressUpdate(values);
            */
        }

    }



}
