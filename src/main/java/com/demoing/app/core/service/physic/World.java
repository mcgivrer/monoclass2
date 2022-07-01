package com.demoing.app.core.service.physic;

import com.demoing.app.core.entity.Entity;
import com.demoing.app.core.math.Vec2d;
import com.demoing.app.core.service.physic.material.Material;

import java.awt.geom.Rectangle2D;

/**
 * The {@link World} object to define game play area limits and a default gravity and friction.
 *
 * <blockquote>May more to comes in the next release with some <code>Influencers</code> to
 * dynamically modify entity display or physic attributes</blockquote>
 */
public class World {
    /**
     * Area for this {@link World} object.
     */
    public Rectangle2D area;

    private Material material;

    public Vec2d gravity;

    public World setMaterial(Material m) {
        this.material = m;
        return this;
    }

    public Material getMaterial() {
        return material;
    }

    /**
     * Initialize the world with some default values with an area of 320.0 x 200.0.
     */
    public World() {
        area = new Rectangle2D.Double(0.0, 0.0, 320.0, 200.0);
        gravity = new Vec2d(0.0, -0.981);
    }

    /**
     * You can set your own {@link World} area dimension of width x height.
     *
     * @param width  the area width for this new {@link World}
     * @param height the area Height for this new {@link World}.
     * @return a World with ots new area of width x height.
     */
    public World setArea(double width, double height) {
        area = new Rectangle2D.Double(0.0, 0.0, width, height);
        return this;
    }

    /**
     * Yot can also set the gravity for your {@link World}.
     *
     * @param g the new gravity for this World to be applied to all {@link Entity} in this {@link World}.
     * @return the world updated with its new gravity.
     */
    public World setGravity(Vec2d g) {
        this.gravity = g;
        return this;
    }
}
