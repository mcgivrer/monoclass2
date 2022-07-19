package com.demoing.app.demo.scenes.behaviors;

import com.demoing.app.core.Application;
import com.demoing.app.core.behavior.Behavior;
import com.demoing.app.core.entity.Entity;

public class RainDropCollideBehavior implements Behavior {
    @Override
    public String filterOnEvent() {
        return Behavior.ON_COLLISION;
    }

    @Override
    public void update(Application a, Entity e, double elapsed) {

    }

    @Override
    public void update(Application a, double elapsed) {

    }

    @Override
    public void onCollide(Application a, Entity e1, Entity e2) {
        e1.setDuration(0);
    }
}
