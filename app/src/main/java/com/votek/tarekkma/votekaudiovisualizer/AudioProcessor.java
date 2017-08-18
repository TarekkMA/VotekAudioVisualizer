package com.votek.tarekkma.votekaudiovisualizer;

import android.graphics.Color;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.alex.siriwaveview.SiriWaveView;
import org.apache.commons.io.FileUtils;
import org.jtransforms.fft.DoubleFFT_1D;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by tarekkma on 8/17/17.
 */

public class AudioProcessor extends AsyncTask<Void,double[],Void> {

    private static final String TAG = "AudioProcessor";


    SiriWaveView waveView;
    int frequency = 44100;
    int channelConfiguration = AudioFormat.CHANNEL_IN_MONO;
    int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
    int blockSize = 512;

    RealDoubleFFT transformer;

    public AudioProcessor(SiriWaveView waveView) {
        transformer = new RealDoubleFFT(blockSize);
        this.waveView = waveView;

    }




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

            while (isCancelled()==false) {
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

        waveView.setFrqPowers(toTransform[0]);
    }

}
