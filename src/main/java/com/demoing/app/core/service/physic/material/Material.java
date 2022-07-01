package com.demoing.app.core.service.physic.material;

import com.demoing.app.core.entity.Entity;
import com.demoing.app.core.entity.Influencer;
import com.demoing.app.core.service.render.Render;
import com.demoing.app.core.service.physic.PhysicEngine;

import java.awt.*;

/**
 * The {@link Material} is a new way to manage physic attributes for any {@link Entity}.
 * It is used by {@link PhysicEngine} and {@link Influencer} to compute moves for {@link Entity}.
 * {@link Influencer} is able to change temporarily {@link Material} for an {@link Entity} when
 * the {@link Influencer} contains {@link Entity}.
 *
 * @author Frédéric Delorme
 * @since 1.0.5
 */
public class Material {
    /**
     * Name for this material
     */
    public String name;
    /**
     * Material density
     */
    public double density;
    /**
     * Material elasticity. Used to compute collision effect into the {@link PhysicEngine}
     */
    public double elasticity;
    /**
     * Material friction. Used to compute friction resistance on moves into the {@link PhysicEngine}
     */
    public double friction;
    /**
     * Default Material {@link Color} to render this object material.
     */
    public Color color = Color.BLUE;
    /**
     * Default Material alpha channel (transparency) to render this material.
     */
    public float alpha = 0.0f;

    /**
     * Create a new Material with a name and with some physic characteristics.
     *
     * @param name       Name for this new material
     * @param density    material density to be used in computation (collision and attraction)
     * @param elasticity material elasticity to compute bouncing capability after collision
     * @param friction   the friction facture to be used by the {@link PhysicEngine} to compute move resistence.
     */
    public Material(String name,
                    double density,
                    double elasticity,
                    double friction) {
        this.name = name;
        this.density = density;
        this.elasticity = elasticity;
        this.friction = friction;
    }

    /**
     * Define the new Color for this Material.
     *
     * @param c the new {@link Color} used by {@link Render} to draw this {@link Material}.
     * @return the updated Material.
     */
    public Material setColor(Color c) {
        this.color = c;
        return this;
    }

    /**
     * Define the new Alpha channel (transparency) for this Material, to be used by {@link Render} at draw time.
     *
     * @param a the new alpha value to compute transparency at rendering time;
     * @return the updated Material.
     */
    public Material setAlpha(float a) {
        this.alpha = a;
        return this;
    }
}
