package com.dashelvest.marqueeview;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.Choreographer;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.lang.ref.WeakReference;

public class MarqueeView extends RecyclerView {
    private static class Marquee {
        private static int MARQUEE_DP_PER_SECOND = 20;

        private static final byte MARQUEE_STOPPED = 0x0;
        private static final byte MARQUEE_STARTING = 0x1;
        private static final byte MARQUEE_RUNNING = 0x2;

        private final WeakReference<MarqueeView> mView;
        private final Choreographer mChoreographer;

        private byte mStatus = MARQUEE_STOPPED;
        private final float mPixelsPerMs;
        private float mMaxFadeScroll;
        private float mGhostStart;
        private float mGhostOffset;
        private float mFadeStop;
        private int mRepeatLimit;

        private float mScroll;
        private long mLastAnimationMs;

        Marquee(MarqueeView v) {
            final float density = v.getContext().getResources().getDisplayMetrics().density;
            mPixelsPerMs = MARQUEE_DP_PER_SECOND * density / 1000f;
            mView = new WeakReference<>(v);
            mChoreographer = Choreographer.getInstance();
        }

        Marquee(MarqueeView v, int dpPerSecond) {
            final float density = v.getContext().getResources().getDisplayMetrics().density;
            MARQUEE_DP_PER_SECOND = dpPerSecond;
            mPixelsPerMs = MARQUEE_DP_PER_SECOND * density / 1000f;
            mView = new WeakReference<>(v);
            mChoreographer = Choreographer.getInstance();
        }

        private Choreographer.FrameCallback mTickCallback = new Choreographer.FrameCallback() {
            @Override
            public void doFrame(long frameTimeNanos) {
                tick(frameTimeNanos / 1000000);
            }
        };

        private Choreographer.FrameCallback mStartCallback = new Choreographer.FrameCallback() {
            @Override
            public void doFrame(long frameTimeNanos) {
                mStatus = MARQUEE_RUNNING;
                mLastAnimationMs = frameTimeNanos / 1000000;
                tick(frameTimeNanos / 1000000);
            }
        };

        private Choreographer.FrameCallback mRestartCallback = new Choreographer.FrameCallback() {
            @Override
            public void doFrame(long frameTimeNanos) {
                if (mStatus == MARQUEE_RUNNING) {
                    if (mRepeatLimit >= 0) {
                        mRepeatLimit--;
                    }
                    start(mRepeatLimit);
                }
            }
        };

        void tick(long currentMs) {
            if (mStatus != MARQUEE_RUNNING) {
                return;
            }

            mChoreographer.removeFrameCallback(mTickCallback);

            final MarqueeView marqueeView = mView.get();
            if (marqueeView != null) {
                long deltaMs = currentMs - mLastAnimationMs;
                mLastAnimationMs = currentMs;
                float deltaPx = deltaMs * mPixelsPerMs;
                mScroll += deltaPx;
                /*if (mScroll > mMaxScroll) {
                    mScroll = mMaxScroll;
                    mChoreographer.postFrameCallbackDelayed(mRestartCallback, MARQUEE_DELAY);
                }*/
                mChoreographer.postFrameCallback(mTickCallback);

                LayoutManager layoutManager = marqueeView.getLayoutManager();

                if (layoutManager instanceof LinearLayoutManager) {
                    LinearLayoutManager linearLayoutManager = (LinearLayoutManager) layoutManager;
                    linearLayoutManager.scrollToPositionWithOffset(0, (int) -mScroll);
                }
            }
        }

        void stop() {
            mStatus = MARQUEE_STOPPED;
            mChoreographer.removeFrameCallback(mStartCallback);
            mChoreographer.removeFrameCallback(mRestartCallback);
            mChoreographer.removeFrameCallback(mTickCallback);
            resetScroll();
        }

        private void resetScroll() {
            mScroll = 0.0f;
            final MarqueeView marqueeView = mView.get();
            if (marqueeView != null) marqueeView.invalidate();
        }

        void start(int repeatLimit) {
            if (repeatLimit == 0) {
                stop();
                return;
            }
            mRepeatLimit = repeatLimit;
            final MarqueeView marqueeView = mView.get();
            if (marqueeView != null) {
                mStatus = MARQUEE_STARTING;
                mScroll = 0.0f;
                final int width = marqueeView.getWidth() - marqueeView.getPaddingLeft()
                        - marqueeView.getPaddingRight();
                final float lineWidth = 0;
                final float gap = width / 3.0f;
                mGhostStart = lineWidth - width + gap;
                mGhostOffset = lineWidth + gap;
                mFadeStop = lineWidth + width / 6.0f;
                mMaxFadeScroll = mGhostStart + lineWidth + lineWidth;

                marqueeView.invalidate();
                mChoreographer.postFrameCallback(mStartCallback);
            }
        }

        float getGhostOffset() {
            return mGhostOffset;
        }

        float getScroll() {
            return mScroll;
        }

        float getMaxFadeScroll() {
            return mMaxFadeScroll;
        }

        boolean shouldDrawLeftFade() {
            return mScroll <= mFadeStop;
        }

        boolean shouldDrawGhost() {
            return mStatus == MARQUEE_RUNNING && mScroll > mGhostStart;
        }

        boolean isRunning() {
            return mStatus == MARQUEE_RUNNING;
        }

        boolean isStopped() {
            return mStatus == MARQUEE_STOPPED;
        }
    }

    private Marquee mMarquee;

    private boolean customMarquee = false;

    private int mMarqueeRepeatLimit = 3;

    public void setMarqueeRepeatLimit(int marqueeRepeatLimit) {
        if(customMarquee) throw new IllegalStateException("Can't set marqueeRepeatLimit of custom Marquee. Pass a new Marquee to setMarquee to change Marquee or null to use default Marquee.");
        this.mMarqueeRepeatLimit = marqueeRepeatLimit;
    }

    @Override
    public void setLayoutManager(@Nullable LayoutManager layout) {
        if(!(layout instanceof LinearLayoutManager)) throw new IllegalArgumentException("LayoutManager must be an instance of LinearLayoutManager.");

        super.setLayoutManager(layout);
    }

    public void setMarquee(Marquee marquee) {
        stopMarquee();
        customMarquee = true;
        this.mMarquee = marquee;
        startMarquee();
    }

    public Marquee getMarquee() {
        return mMarquee;
    }

    public void setMarqueeAdapter(@Nullable MarqueeAdapter<?> adapter) {
        if(!(adapter instanceof MarqueeAdapter)) throw new IllegalArgumentException("Adapter must be an instance of MarqueeAdapter");
        super.setAdapter(new MarqueeParentAdapter<>((MarqueeAdapter<?>) adapter));
    }

    public void setAdapter(@Nullable Adapter adapter) {
        throw new RuntimeException("setAdapter(Adapter) is not supported. Use setMarqueeAdapter(MarqueeAdapter) instead.");
    }

    public MarqueeView(Context context) {
        super(context);
    }

    public MarqueeView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public MarqueeView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void start(){
        setHorizontalFadingEdgeEnabled(true);
        requestLayout();
        invalidate();

        if (mMarquee == null) {
            if (mMarquee == null){
                mMarquee = new Marquee(this);
                customMarquee = false;
            }
            mMarquee.start(mMarqueeRepeatLimit);
        }
    }

    private void startMarquee() {
        setHorizontalFadingEdgeEnabled(true);
        requestLayout();
        invalidate();

        if (mMarquee == null) {
            if (mMarquee == null){
                mMarquee = new Marquee(this);
                customMarquee = false;
            }
            mMarquee.start(mMarqueeRepeatLimit);
        }
    }

    private void stopMarquee() {
        if (mMarquee != null && !mMarquee.isStopped()) {
            mMarquee.stop();
        }

        setHorizontalFadingEdgeEnabled(false);
        requestLayout();
        invalidate();
    }

    private void startStopMarquee(boolean start) {
        if (start) {
            startMarquee();
        } else {
            stopMarquee();
        }
    }

    private void resetMarquee() {
        stopMarquee();
        mMarquee = null;
        customMarquee = false;
    }

    @Override
    public void onDraw(Canvas c) {
        super.onDraw(c);
    }
}
