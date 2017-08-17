package com.alex.siriwaveview;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by Alex on 6/25/2016.
 */
public class SiriWaveView extends View {

    private Path mPath;
    private Paint mPaint;

    private float frequency = 1.5f;
    private float IdleAmplitude = 0.00f;
    private int waveNumber = 2;
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

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawPath(mPath, mPaint);
        updatePath();
    }

    public void updatePath() {
        if(frqPowers==null)return;

        int width = getWidth();
        int height = getHeight();

        mPath.reset();

        int centerY = height/2;
        int jumpX   = width/frqPowers.length;

        mPath.moveTo(0,centerY);


        for (int i = 0; i < frqPowers.length; i++) {
            int x = i*jumpX;
            int y = (int) (centerY - (frqPowers[i] * 1000));
            mPath.lineTo(x, y);
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