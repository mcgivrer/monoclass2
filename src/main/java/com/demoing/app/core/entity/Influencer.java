package com.demoing.app.core.entity;

import com.demoing.app.core.Application;
import com.demoing.app.core.behavior.Behavior;
import com.demoing.app.core.service.physic.material.Material;
import com.demoing.app.core.math.Vec2d;

/**
 * The {@link Influencer} extending {@link Entity} to provide environmental influencer to change {@link Entity}
 * behavior has soon the Entity is contained by the {@link Influencer}.
 * An influencer can change temporarily some {@link Entity} attribute's values.
 *
 * @author Frédéric Delorme
 * @since 1.0.5
 */
public class Influencer extends Entity {

    public Influencer(String name) {
        super(name);
        addBehavior(new Behavior() {
            @Override
            public String filterOnEvent() {
                return Behavior.onCollision;
            }

            @Override
            public void update(Application a, Entity e, double elapsed) {

            }

            @Override
            public void update(Application a, double elapsed) {

            }

            @Override
            public void onCollide(Application a, Entity e1, Entity e2) {
                Influencer i1 = (Influencer) e1;
                e2.forces.add(i1.getForce());
            }
        });
    }

    /**
     * Define the {@link Influencer}'s gravity attribute, 0 means World's default gravity.
     *
     * @param g the new gravity for this {@link Influencer} zone
     * @return the updated Influencer with its new gravity.
     */
    public Influencer setGravity(Vec2d g) {
        this.attributes.put("gravity", g);
        return this;
    }

    /**
     * Define the {@link Influencer}'s attribute force to be applied to any {@link Entity}
     * contained by this {@link Influencer}..
     *
     * @param f the force to be applied in this {@link Influencer} zone.
     * @return the updated {@link Influencer} with its new force.
     */
    public Influencer setForce(Vec2d f) {
        this.attributes.put("force", f);
        return this;
    }

    /**
     * retrieve the current gravity attribute value for this {@link Influencer}
     *
     * @return value for this Influencer's gravity.
     */
    public Vec2d getGravity() {
        return (Vec2d) this.attributes.get("gravity");
    }

    /**
     * retrieve the current force attribute value for this {@link Influencer}
     *
     * @return value for this Influencer's force.
     */
    public Vec2d getForce() {
        return (Vec2d) this.attributes.get("force");
    }

    public Material getMaterial() {
        return material;
    }
}
