package com.demoing.app.core.entity;

import com.demoing.app.core.behavior.Behavior;

/**
 * {@link ParticleSystem} is an {@link Entity} that manage a bunch of sub entities named Particle.
 * This child entities are managed by this {@link ParticleSystem}'s {@link Behavior} having an
 * {@link Behavior#ON_UPDATE_ENTITY} event nature. And during this update , a specific Behavior
 * is called to generate an on-demand and following its own rules the particle new instance creation.
 * This particle is an {@link Entity} itself.
 *
 * @author Frédéric Delorme
 * @since 1.0.6
 */
public class ParticleSystem extends Entity {

    /**
     * create a new ParticleSystem
     *
     * @param name the name for this new ParticleSystem.
     */
    public ParticleSystem(String name) {
        super(name);
    }

    /**
     * Add a specific Particle Updater behavior to this ParticleSystem.
     *
     * @param pub the Particle Update Behavior to be added
     * @return the Updated ParticleSystem.
     */
    public ParticleSystem addParticleUpdate(Behavior pub) {
        this.addBehavior(Behavior.ON_UPDATE_ENTITY, pub);
        return this;
    }

    /**
     * Add a specific Particle Generator Behavior to generate new Particle on demand to the ParticleSystem.
     *
     * @param pgb the Particle Generator Behavior to be added
     * @return the Updated ParticleSystem.
     */
    public ParticleSystem addParticleGenerator(Behavior pgb) {
        this.addBehavior(Behavior.ON_UPDATE_ENTITY, pgb);
        return this;
    }

}
