package com.demoing.app.core.gfx;

import com.demoing.app.core.entity.Entity;
import com.demoing.app.core.io.Resources;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

/**
 * {@link Animation} is the animations manager for an {@link Entity}.
 * A simple Fluent APi is provided to declare animationSet and to activate one of those set.
 * <p>
 * the {@link Animation#update(long)} process will refresh the and compute the frame according to the elapsed
 * time since previous call, while the {@link Animation#getFrame()} will return the current active frame
 * ({@link BufferedImage}) for the current active AnimationSet.
 *
 * @author Frédéric Delorme
 * @since 1.0.3
 */
public class Animation {
    Map<String, AnimationSet> animationSet = new HashMap<>();
    public String currentAnimationSet;
    public int currentFrame;
    private long internalAnimationTime;

    private boolean loop = true;

    public Animation() {
        currentAnimationSet = null;
        currentFrame = 0;
    }

    public Animation activate(String key) {
        this.currentAnimationSet = key;
        AnimationSet aSet = this.animationSet.get(key);
        if (currentFrame > aSet.frames.length) {
            this.currentFrame = 0;
            this.internalAnimationTime = 0;
            aSet.counter = 0;
        }
        return this;
    }

    public Animation addAnimationSet(String key, String imgSrc, int x, int y, int tw, int th, int[] durations, int loop) {
        AnimationSet aSet = new AnimationSet(key).setSize(tw, th);
        BufferedImage image = Resources.loadImage(imgSrc);
        aSet.frames = new BufferedImage[durations.length];
        for (int i = 0; i < durations.length; i++) {
            BufferedImage frame = image.getSubimage(x + (i * tw), y, tw, th);
            aSet.frames[i] = frame;
        }
        aSet.setFramesDuration(durations);
        aSet.setLoop(loop);
        animationSet.put(key, aSet);
        return this;
    }

    public synchronized void update(long elapsedTime) {
        internalAnimationTime += elapsedTime;
        AnimationSet aSet = animationSet.get(currentAnimationSet);
        currentFrame = aSet.durations.length > currentFrame ? currentFrame : 0;
        if (aSet.durations[currentFrame] <= internalAnimationTime) {
            internalAnimationTime = 0;
            if (currentFrame + 1 < aSet.frames.length) {
                currentFrame = currentFrame + 1;
            } else {
                if (aSet.counter + 1 < aSet.loop) {
                    aSet.counter++;
                }
                currentFrame = 0;
            }
        }
    }

    public synchronized BufferedImage getFrame() {
        if (animationSet.get(currentAnimationSet) != null
                && currentFrame < animationSet.get(currentAnimationSet).frames.length) {
            return animationSet.get(currentAnimationSet).frames[currentFrame];
        }
        return null;
    }

}
