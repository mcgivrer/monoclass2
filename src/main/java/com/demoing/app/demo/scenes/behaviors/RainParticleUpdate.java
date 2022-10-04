package com.demoing.app.demo.scenes.behaviors;

import java.awt.Color;

import com.demoing.app.core.Application;
import com.demoing.app.core.behavior.Behavior;
import com.demoing.app.core.entity.Entity;
import com.demoing.app.core.entity.helpers.EntityType;
import com.demoing.app.core.service.physic.PhysicType;
import com.demoing.app.core.service.physic.World;

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
     * Nb max of particles to simulate rain.
     */
    private int nbMaxParticle = 300;

    /**
     * Duration for on rain drop (in ms)
     */
    private int particleDuration = 5000;

    /**
     * The rain force to generate rain drop.
     */
    private double rainForce = 2.5;

    /**
     * Create the Rain drop generator effect
     *
     * @param w the World object to take in account for Rain drop generation.
     */
    public RainParticleUpdate(World w) {
        this.world = w;
    }

    @Override
    public void initialization(Entity e) {
        for (int i = 0; i < nbMaxParticle; i++) {
            createDropParticleFromParent(e);
        }
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
        e.getChild()
                .stream()
                .filter(child -> !child.isAlive())
                .forEach(this::initDropParticle);
    }

    /**
     * Create a brand-new particle with color, type and name, and then initialize it with duration, position and speed.
     *
     * @param parent the parent {@link com.demoing.app.core.entity.ParticleSystem}.
     */
    private void createDropParticleFromParent(Entity parent) {
        Entity drop = new Entity(parent.name + "_drop_" + (parent.getChild().size() + 1))
                .setType(EntityType.ELLIPSE)
                .setPhysicType(PhysicType.DYNAMIC)
                .setSize(1.0, 2.0)
                .setColor(Color.CYAN)
                .setMass(0.01)
                .setPriority(10)
                .addBehavior(
                        new RainDropParticleUpdate(world))
                .addBehavior(
                        new RainDropCollideBehavior(world));
        initDropParticle(drop);
        parent.getChild().add(drop);
    }

    /**
     * Initialize particle attribute to renew position, velocity  and life duration
     *
     * @param child the particle to be renewed (here is a Rain drop one)
     */
    private void initDropParticle(Entity child) {
        child.setDuration(particleDuration)
            .setPosition(Math.random() * (world.getArea().getWidth()*0.8)+(world.getArea().getWidth()*0.1), 0)
            .addForce(0.05 , rainForce);
    }

    /**
     * Nothing to do with the Scene update.
     *
     * @param a       the parent application.
     * @param elapsed the elapsed time since previous call.
     */
    @Override
    public void update(Application a, double elapsed) {
        // No need to implement update for Scene.
    }

    /**
     * Nothing to do with the ParticleSystem collision.
     *
     * @param a
     * @param e1
     * @param e2
     */
    @Override
    public void onCollide(Application a, Entity e1, Entity e2) {
    }
}
