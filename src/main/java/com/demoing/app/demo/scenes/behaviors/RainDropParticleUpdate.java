package com.demoing.app.demo.scenes.behaviors;

import com.demoing.app.core.Application;
import com.demoing.app.core.behavior.Behavior;
import com.demoing.app.core.entity.Entity;
import com.demoing.app.core.service.physic.World;

public class RainDropParticleUpdate implements Behavior {
    private final World world;

    public RainDropParticleUpdate(World world) {
        this.world = world;
    }

    @Override
    public String filterOnEvent() {
        return Behavior.ON_UPDATE_ENTITY;
    }

    @Override
    public void update(Application a, Entity e, double elapsed) {
        if(e.pos.y>world.area.getHeight()){
            e.duration=0;
        }
    }

    @Override
    public void update(Application a, double elapsed) {

    }

    @Override
    public void onCollide(Application a, Entity e1, Entity e2) {

    }
}
