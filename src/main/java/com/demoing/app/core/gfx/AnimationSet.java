package com.demoing.app.core.gfx;

import java.awt.image.BufferedImage;


/**
 * {@link AnimationSet} defining a series of Frames and their duration for a specific animation name.
 * This animationSet object is used in the {@link Animation#animationSet} attributes, to defined all the possible
 * animation into an Entity (see {@link com.demoing.app.core.entity.Entity#animations}.
 * <p>
 * Here is a Fluent API to ease the Animation set definition.
 *
 * @author Frédéric Delorme
 * @since 1.0.3
 */
public class AnimationSet {
    String name;


    BufferedImage[] frames;
    int[] durations;


    int loop;
    int counter;
    private int width;
    private int height;


    public BufferedImage[] getFrames() {
        return frames;
    }

    public int getDurations(int i) {
        return durations[i];
    }

    public int getLoop() {
        return loop;
    }

    public AnimationSet(String key) {
        this.name = key;
    }

    public AnimationSet setFramesDuration(int[] d) {
        this.durations = d;
        return this;
    }

    public AnimationSet setSize(int w, int h) {
        this.width = w;
        this.height = h;
        return this;
    }

    public AnimationSet setLoop(int l) {
        this.loop = l;
        return this;
    }
}
