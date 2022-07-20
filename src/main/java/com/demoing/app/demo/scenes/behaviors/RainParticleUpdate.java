package com.demoing.app.demo.scenes.behaviors;

import com.demoing.app.core.Application;
import com.demoing.app.core.behavior.Behavior;
import com.demoing.app.core.entity.Entity;
import com.demoing.app.core.entity.EntityType;
import com.demoing.app.core.service.physic.World;
import com.demoing.app.core.utils.Logger;

import java.awt.*;

/**
 * the RainParticleUpdate wil perform the Particle rain drop generation regarding a max number of particle.
 *
 * @author Frédéric Delorme.
 * @since 1.0.6
 */
public class RainParticleUpdate implements Behavior {

    /**
     * THe World defining the world area limit.
     */
    private final World world;

    /**
     * Create the Rain drop generator effect
     *
     * @param w the World object to take in account for Rain drop generation.
     */
    public RainParticleUpdate(World w) {
        this.world = w;
    }

    /**
     * This Behavior is only an Entity update event
     *
     * @return
     */
    @Override
    public String filterOnEvent() {
        return Behavior.ON_UPDATE_ENTITY;
    }

    /**
     * Generate new particle until a max number is reach, then update the not alive one.
     *
     * @param a       The parent Application.
     * @param e       The parent entity, hee the ParticleEffect
     * @param elapsed The elapsed time since previous call.
     */
    @Override
    public void update(Application a, Entity e, double elapsed) {
        if (e.getChild().size() < 200) {
            createDropParticleFromParent(e);
        } else {
            e.getChild().stream()
                    .filter(child -> !child.isAlive())
                    .forEach(this::initDropParticle);
        }
    }

    /**
     * Initialize particle attribute to renew position, velocity  and life duration
     *
     * @param child the particle to be renewed (here is a Rain drop one)
     */
    private void initDropParticle(Entity child) {
        child.setDuration(3000)
                .setPosition(Math.random() * world.getArea().getWidth(), 0)
                .addForce((0.5 * Math.random()) - 0.25, 1.6);
    }

    /**
     * Create a brand-new particle with color, type and name, and then initialize it with duration, position and speed.
     *
     * @param parent the parent {@link ParticleSystem}.
     */
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

    /**
     * Nothing to do with the Scene update.
     *
     * @param a
     * @param elapsed
     */
    @Override
    public void update(Application a, double elapsed) {

    }

    /**
     * Nothing to do with e ParticleSystem collision.
     *
     * @param a
     * @param e1
     * @param e2
     */
    @Override
    public void onCollide(Application a, Entity e1, Entity e2) {
    }
}
