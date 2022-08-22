package com.demoing.app.demo.scenes.behaviors;

import com.demoing.app.core.Application;
import com.demoing.app.core.behavior.Behavior;
import com.demoing.app.core.entity.Entity;
import com.demoing.app.core.entity.Influencer;
import com.demoing.app.core.service.physic.PhysicType;
import com.demoing.app.core.service.physic.World;

public class RainDropCollideBehavior implements Behavior {

    public World world;

    @Override
    public String filterOnEvent() {
        return Behavior.ON_COLLISION;
    }

    public RainDropCollideBehavior(World w) {
        this.world = w;
    }

    @Override
    public void update(Application a, Entity e, double elapsed) {

    }

    @Override
    public void update(Application a, double elapsed) {

    }

    @Override
    public void onCollide(Application a, Entity e1, Entity e2) {
        if (e2 instanceof Influencer) {
            return;
        }
        if (e2.physicType == PhysicType.STATIC) {
            e1.setDuration(0);
        }
    }
}
