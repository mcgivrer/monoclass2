package com.demoing.app.demo.scenes.behaviors;

import com.demoing.app.core.Application;
import com.demoing.app.core.behavior.Behavior;
import com.demoing.app.core.entity.Entity;
import com.demoing.app.demo.scenes.DemoScene;

public final class PlayerOnCollisionBehavior implements Behavior {
    private final DemoScene demoScene;

    public PlayerOnCollisionBehavior(DemoScene demoScene) {
        this.demoScene = demoScene;
    }

    @Override
    public String filterOnEvent() {
        return ON_COLLISION;
    }

    @Override
    public void onCollide(Application a, Entity e1, Entity e2) {
        if (e2.name.contains("ball_")) {
            demoScene.reducePlayerEnergy(a, e1, e2);
        }
    }

    @Override
    public void update(Application a, Entity e, double d) {

    }

    public void update(Application a, double d) {

    }
}
