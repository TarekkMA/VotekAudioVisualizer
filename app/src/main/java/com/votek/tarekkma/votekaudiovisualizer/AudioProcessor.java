package com.votek.tarekkma.votekaudiovisualizer;

import android.media.AudioRecord;
import android.util.Log;

import com.alex.siriwaveview.SiriWaveView;
import com.votek.tarekkma.votekaudiovisualizer.fft.RealDoubleFFT;

import org.apache.commons.io.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by tarekkma on 8/17/17.
 */

public class AudioProcessor {

    private static final String TAG = "AudioProcessor";

    public static final int BUFFER_SIZE = 512;
    RealDoubleFFT fft;
    SiriWaveView waveView;


    public AudioProcessor(SiriWaveView waveView) {
        fft = new RealDoubleFFT(BUFFER_SIZE);
        this.waveView = waveView;
    }

    public void prossesAudio(File file) {
        try {

            byte[] buffer = new byte[BUFFER_SIZE];
            double[] transformed = new double[BUFFER_SIZE];
            int bytesRead = 0;
            int totalBytesRead = 0;
            InputStream in = new FileInputStream(file);

            while ((bytesRead = in.read(buffer,0,BUFFER_SIZE)) != -1)
            {
                totalBytesRead+=BUFFER_SIZE;

                for (int i = 0; i < BUFFER_SIZE && i < bytesRead; i++) {
                    transformed[i] = (double) buffer[i] / 32768.0; // from byte to double
                }

                fft.ft(transformed);
                waveView.setFrqPowers(transformed);

            }


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
