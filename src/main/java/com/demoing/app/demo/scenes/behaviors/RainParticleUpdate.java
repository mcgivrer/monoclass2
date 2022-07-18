package com.demoing.app.demo.scenes.behaviors;

import com.demoing.app.core.Application;
import com.demoing.app.core.behavior.Behavior;
import com.demoing.app.core.entity.Entity;
import com.demoing.app.core.entity.EntityType;
import com.demoing.app.core.service.physic.World;

import java.awt.*;

public class RainParticleUpdate implements Behavior {

    private World world;

    public  RainParticleUpdate(World w) {
        this.world = w;
    }

    @Override
    public String filterOnEvent() {
        return Behavior.ON_UPDATE_ENTITY;
    }

    @Override
    public void update(Application a, Entity e, double elapsed) {
        if (e.getChild().size() < 200) {
            createDropParticleFromParent(e);
        }
    }

    private void createDropParticleFromParent(Entity parent) {
        Entity drop = new Entity(parent.name+"_drop_" +( parent.getChild().size() + 1))
                .setType(EntityType.ELLIPSE)
                .setPosition(Math.random() * world.getArea().getWidth(), 0)
                .setSize(1.0, 2.0)
                .addForce((0.5 * Math.random()) - 0.25, 0.4)
                .setColor(Color.CYAN)
                .setPriority(10)
                .addBehavior(new RainDropParticleUpdate(world));
        parent.getChild().add(drop);
    }

    @Override
    public void update(Application a, double elapsed) {

    }

    @Override
    public void onCollide(Application a, Entity e1, Entity e2) {

    }
}
