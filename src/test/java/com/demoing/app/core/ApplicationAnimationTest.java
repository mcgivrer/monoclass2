package com.demoing.app.core;

import com.demoing.app.core.gfx.Animation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


public class ApplicationAnimationTest {
    public Animation anim;

    @BeforeEach
    public void setup() {
        anim = new Animation();

    }

    @AfterEach
    public void tearDown() {
        anim = null;
    }

    @Test
    public void testLoopAnimSet() {
        anim.addAnimationSet("test",
                "/images/sprites01.png",
                0, 0,
                32, 32,
                new int[]{10, 20, 30, 40, 50, 60, 70, 80, 90, 100, 110, 120, 130},
                -1);
        anim.activate("test");
        // Verify that looping 2 times on the total animation time / frame duration update (10ms)
        // let animation back to frame 0.
        for (int i = 0; i < (910 * 2) / 10; i++) {
            anim.update(10);
            System.out.println("time:" + i * 10
                    + " => frame duration: " + (anim.animationSet.get("test").durations[anim.currentFrame] * i)
                    + "=> anim frame:" + anim.currentFrame);
        }
        Assertions.assertEquals(0, anim.currentFrame, 0, "Aniomation has loop 2 times and go back to 0");
    }

    @Test
    public void testNumberOfFrames() {
        anim.addAnimationSet("test",
                "/images/sprites01.png",
                0, 0,
                32, 32,
                new int[]{10, 20, 30, 40, 50, 60, 70, 80, 90, 100, 110, 120, 130},
                -1);
        anim.activate("test");
        Assertions.assertEquals(13, anim.animationSet.get("test").frames.length, "AnimationSet is not well created");
    }

    @Test
    public void testGetFrameNotNull() {
        anim.addAnimationSet("test",
                "/images/sprites01.png",
                0, 0,
                32, 32,
                new int[]{10, 20, 30, 40, 50, 60, 70, 80, 90, 100, 110, 120, 130},
                -1);
        anim.activate("test");
        Assertions.assertNotNull(anim.getFrame(), "Frame is not null");
        Assertions.assertEquals(32, anim.getFrame().getWidth(), "THe retrieved frame has not the right width");
        Assertions.assertEquals(32, anim.getFrame().getHeight(), "THe retrieved frame has not the right height");
    }

    @Test
    public void testMultipleAnimationSetSizeAndFramesLength() {
        anim.addAnimationSet("idle",
                "/images/sprites01.png",
                0, 0,
                32, 32,
                new int[]{10, 20, 30, 40, 50, 60, 70, 80, 90, 100, 110, 120, 130},
                -1);
        anim.addAnimationSet("walk",
                "/images/sprites01.png",
                0, 32,
                32, 32,
                new int[]{60, 60, 60, 150, 60, 60, 60, 150},
                -1);
        anim.addAnimationSet("jump",
                "/images/sprites01.png",
                0, 5 * 32,
                32, 32,
                new int[]{60, 60, 250, 250, 60, 60},
                -1);
        Assertions.assertEquals(3,
                anim.animationSet.size(),
                "All animation set has not been initialized.");

        Assertions.assertEquals(13,
                anim.animationSet.get("idle").frames.length,
                "Idle animation has not been init with 13 frames");
        Assertions.assertEquals(8,
                anim.animationSet.get("walk").frames.length,
                "Walk animation has not been init with 8 frames");
        Assertions.assertEquals(6,
                anim.animationSet.get("jump").frames.length,
                "Jump animation has not been init with 6 frames");
    }

    @Test
    public void testNotLoopingAnimationCOnfiguration() {
        anim.addAnimationSet("dead",
                "/images/sprites01.png",
                0, 7 * 32,
                32, 32,
                new int[]{160, 160, 160, 160, 160, 160, 500},
                0);

        Assertions.assertEquals(0, anim.animationSet.get("dead").loop, "This Dead animation will loop !");
    }

}
