package com.alex.siriwaveview;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.media.MediaPlayer;
import android.media.audiofx.Visualizer;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.nio.ByteBuffer;

/**
 * Created by Alex on 6/25/2016.
 */
public class SiriWaveView extends View {

    private byte[] mBytes;
    private byte[] mFFTBytes;
    private float[] mFFTPoints;

    float precent[] = new float[]{0.2f,0.3f,0.5f};//bands percent
    int waveVals[] = new int[]{0,0,0};//max
    int frqIndex[] = new int[3];

    private int mDivisions = 2;
    private int mSampleRate;
    private Visualizer mVisualizer;

    private Path mPath;
    private Paint mPaint;

    private float frequency = 1.5f;
    private float IdleAmplitude = 0.00f;
    private int waveNumber = 3;
    private float phaseShift = 0.15f;
    private float initialPhaseOffset = 0.0f;
    private float waveHeight;
    private float waveVerticalPosition = 2;
    private int waveColor;
    private float phase;
    private float amplitude;
    private float level = 1.0f;


    private double[] frqPowers = null;





    public void setFrqPowers(double[] frqPowers) {
        this.frqPowers = frqPowers;
        invalidate();
    }

    ObjectAnimator mAmplitudeAnimator;

    public SiriWaveView(Context context) {
        super(context);
        if (!isInEditMode())
            init(context, null);
    }

    public SiriWaveView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (!isInEditMode())
            init(context, attrs);
    }

    public SiriWaveView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (!isInEditMode())
            init(context, attrs);
    }

    public void init(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, com.alex.siriwaveview.R.styleable.SiriWaveView);
        frequency = a.getFloat(com.alex.siriwaveview.R.styleable.SiriWaveView_waveFrequency, frequency);
        IdleAmplitude = a.getFloat(com.alex.siriwaveview.R.styleable.SiriWaveView_waveIdleAmplitude, IdleAmplitude);
        phaseShift = a.getFloat(com.alex.siriwaveview.R.styleable.SiriWaveView_wavePhaseShift, phaseShift);
        initialPhaseOffset = a.getFloat(com.alex.siriwaveview.R.styleable.SiriWaveView_waveInitialPhaseOffset, initialPhaseOffset);
        waveHeight = a.getDimension(com.alex.siriwaveview.R.styleable.SiriWaveView_waveHeight, waveHeight);
        waveColor = a.getColor(com.alex.siriwaveview.R.styleable.SiriWaveView_waveColor, waveColor);
        waveVerticalPosition = a.getFloat(com.alex.siriwaveview.R.styleable.SiriWaveView_waveVerticalPosition, waveVerticalPosition);
        waveNumber = a.getInteger(R.styleable.SiriWaveView_waveAmount, waveNumber);

        mPath = new Path();
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(2);
        mPaint.setColor(waveColor);

        a.recycle();
        initAnimation();
    }

    private void initAnimation() {
        if (mAmplitudeAnimator == null) {
            mAmplitudeAnimator = ObjectAnimator.ofFloat(this, "amplitude", 1f);
            mAmplitudeAnimator.setRepeatCount(ObjectAnimator.INFINITE);
        }
        if (!mAmplitudeAnimator.isRunning()) {
            mAmplitudeAnimator.start();
        }
    }


    int getFreqFromBin(int bins,int index){
        return (mSampleRate / bins) * index;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawPath(mPath, mPaint);
        updatePathReal(canvas);
    }

    public static double[] toDoubleArray(byte[] byteArray){
        int times = Double.SIZE / Byte.SIZE;
        double[] doubles = new double[byteArray.length / times];
        for(int i=0;i<doubles.length;i++){
            doubles[i] = ByteBuffer.wrap(byteArray, i*times, times).getDouble();
        }
        return doubles;
    }

    public void updatePathReal(Canvas canvas) {
        mPath.reset();


        if(frqPowers == null) return;

        for (int i = 0; i < frqPowers.length; i++) {
            int x = i;
            int upy = canvas.getHeight()/2;
            int downy = (int) (100 - (frqPowers[i] * 10));

            canvas.drawLine(x, downy, x, upy, mPaint);
        }


        //updatePath();
    }

    private void updatePath() {
        mPath.reset();

        phase += phaseShift;
        amplitude = Math.max(level, IdleAmplitude);

        for (int i = 0; i < waveNumber; i++) {
            float halfHeight = getHeight() / waveVerticalPosition;
            float width = getWidth();
            float mid = width / 2.0f;

            float maxAmplitude = halfHeight - (halfHeight - waveHeight);

            // Progress is a value between 1.0 and -0.5, determined by the current wave idx, which is used to alter the wave's amplitude.
            float progress = 1.0f - (float) i / waveNumber;
            float normedAmplitude = (1.5f * progress - 0.5f) * amplitude;

            float multiplier = (float) Math.min(1.0, (progress / 3.0f * 2.0f) + (1.0f / 3.0f));

            for (int x = 0; x < width; x++) {
                float scaling = (float) (-Math.pow(1 / mid * (x - mid), 2) + 1);

                float y = (float) (scaling * maxAmplitude * normedAmplitude * Math.sin(2 * Math.PI * (x / width) * frequency + phase + initialPhaseOffset) + halfHeight);

                if (x == 0) {
                    mPath.moveTo(x, y);
                } else {
                    mPath.lineTo(x, y);
                }
            }
        }
        //mPath.close();
    }

    private void setAmplitude(float amplitude) {
        this.amplitude = amplitude;
        invalidate();
    }

    private float getAmplitude() {
        return this.amplitude;
    }

    public void stopAnimation() {
        if (mAmplitudeAnimator != null) {
            mAmplitudeAnimator.removeAllListeners();
            mAmplitudeAnimator.end();
            mAmplitudeAnimator.cancel();
        }
    }

    public void startAnimation() {
        if (mAmplitudeAnimator != null) {
            mAmplitudeAnimator.start();
        }
    }

    public void setWaveColor(int waveColor) {
        mPaint.setColor(waveColor);
        invalidate();
    }

    public void setStrokeWidth(float strokeWidth) {
        mPaint.setStrokeWidth(strokeWidth);
        invalidate();
    }
}