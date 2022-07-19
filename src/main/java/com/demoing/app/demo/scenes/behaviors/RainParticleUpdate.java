package com.demoing.app.demo.scenes.behaviors;

import com.demoing.app.core.Application;
import com.demoing.app.core.behavior.Behavior;
import com.demoing.app.core.entity.Entity;
import com.demoing.app.core.entity.EntityType;
import com.demoing.app.core.service.physic.World;

import java.awt.*;

public class RainParticleUpdate implements Behavior {

    private World world;

    public RainParticleUpdate(World w) {
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
        } else {
            e.getChild().stream()
                .filter(child -> child.isAlive())
                .forEach(child -> {
                    initDropParticle(child);
                });
        }
    }

    private void initDropParticle(Entity child) {
        child.setDuration(-1)
                .setPosition(Math.random() * world.getArea().getWidth(), 0)
                .addForce((0.5 * Math.random()) - 0.25, 0.4);
    }

    private void createDropParticleFromParent(Entity parent) {
        Entity drop = new Entity(parent.name + "_drop_" + (parent.getChild().size() + 1))
                .setType(EntityType.ELLIPSE)
                .setSize(1.0, 2.0)
                .setColor(Color.CYAN)
                .setPriority(10)
                .addBehavior(new RainDropParticleUpdate(world));
        initDropParticle(drop);
        parent.getChild().add(drop);
    }

    @Override
    public void update(Application a, double elapsed) {

    }

    @Override
    public void onCollide(Application a, Entity e1, Entity e2) {

    }
}
