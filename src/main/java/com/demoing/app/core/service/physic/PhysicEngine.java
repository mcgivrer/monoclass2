package com.demoing.app.core.service.physic;

import com.demoing.app.core.Application;
import com.demoing.app.core.behavior.Behavior;
import com.demoing.app.core.config.Configuration;
import com.demoing.app.core.entity.Entity;
import com.demoing.app.core.entity.Influencer;
import com.demoing.app.core.math.Vec2d;
import com.demoing.app.core.service.physic.material.DefaultMaterial;
import com.demoing.app.core.service.physic.material.Material;
import com.demoing.app.core.service.render.Render;
import com.demoing.app.core.utils.Logger;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * A Physic computation engine to process Object moves according to their resulting process acceleration and speed
 * from the applied forces to each Entity.
 *
 * @author Frédéric Delorme
 * @since 1.0.2
 */
public class PhysicEngine {
    private final Application app;
    private final World world;
    private final Configuration config;
    public long updateTime;
    private final Map<String, Influencer> influencers = new ConcurrentHashMap<>();

    /**
     * Initialize the Physic Engine for the parent Application a, with the Configuration c
     * and in a World w.
     *
     * @param a the parent Application
     * @param c the Configuration where to find Physic Engine initialization parameters
     * @param w the World where the Entities will evolve in.
     */
    public PhysicEngine(Application a, Configuration c, World w) {
        this.app = a;
        this.config = c;
        this.world = w;
    }

    /**
     * Update and process physics attributes the Entity (acceleration, speed and position) from the
     * <code>app.entities map</code>, with  the elapsed time since previous call.
     *
     * @param elapsed the elapsed time since previous call.
     */
    public synchronized void update(double elapsed) {
        long start = System.nanoTime();

        // update entities
        app.getEntities().values().forEach((e) -> {
            if (e.physicType.equals(PhysicType.DYNAMIC)) {
                updateEntity(e, elapsed);
            }
            e.update(elapsed);

            // TODO update Entity Behavior
            e.behaviors.values().stream()
                    .filter(b -> b.filterOnEvent()
                            .contains(Behavior.updateEntity))
                    .toList()
                    .forEach(b -> b.update(app, e, elapsed));

            // Reset all collision for the Entity e
            e.collide = false;
            e.colliders.clear();
        });

        // TODO update Scene Behaviors
        if (Optional.ofNullable(app.getSceneManager().getActiveScene().getBehaviors()).isPresent()) {
            app.getSceneManager()
                    .getActiveScene()
                    .getBehaviors()
                    .values().stream()
                    .filter(b -> b.filterOnEvent()
                            .contains(Behavior.updateScene))
                    .toList()
                    .forEach(b -> b.update(app, elapsed));
        }
        //  update active camera if presents.
        Render r = app.getRender();
        if (Optional.ofNullable(r.getActiveCamera()).isPresent()) {
            r.getActiveCamera().update(elapsed);
        }
        updateTime = System.nanoTime() - start;
    }

    /**
     * Update one Entity
     *
     * @param e       The Entity to be updated.
     * @param elapsed the elapsed time since previous call.
     */
    private void updateEntity(Entity e, double elapsed) {
        applyPhysicRuleToEntity(e, elapsed);
        constrainsEntity(e);
    }

    /**
     * Apply world Influencer's to the `Entity` e.
     *
     * @param e The Entity to be influenced
     * @return Material out from Influencer's and Entity.
     */
    private Material applyWorldInfluencers(Entity e) {
        Material m = DefaultMaterial.DEFAULT.get();
        Vec2d g = new Vec2d(world.gravity.x, e.mass * world.gravity.y);
        for (Influencer i : getInfluencers().values()) {
            if (i.box.intersects(e.box)) {
                Logger.log(Logger.DETAILED, this.getClass(), "Entity %s intersects Influencer %s", e.name, i.name);
                if (Optional.ofNullable(i.getGravity()).isPresent()) {
                    g = new Vec2d(
                            i.getGravity().x,
                            e.mass * i.getGravity().y);
                }
                if (Optional.ofNullable(i.getForce()).isPresent()) {
                    e.forces.add(i.getForce());
                }
                m = i.getMaterial();
            }
        }
        e.forces.add(g);
        return m;
    }

    /**
     * Apply Physic computation on the Entity e for the elapsed time.
     *
     * @param e       the Entity to compute Physic for.
     * @param elapsed the elapsed tile since previous call.
     */
    private void applyPhysicRuleToEntity(Entity e, double elapsed) {
        e.oldPos.x = e.pos.x;
        e.oldPos.y = e.pos.y;

        // a small reduction of time
        elapsed *= 0.4;

        Material m = applyWorldInfluencers(e);

        e.acc = new Vec2d(0.0, 0.0);
        e.acc.add(e.forces);

        // TODO fix the friction issue :
        // TODO here it is but don't already find the right way to fix it !
        double collisionFriction = e.colliders.stream()
                .filter(c -> c.collide)
                .mapToDouble(c -> c.material.friction)
                .reduce(m.friction, Double::sum);
        double friction = e.collide
                ? collisionFriction * m.friction * world.getMaterial().friction
                : world.getMaterial().friction;

        e.vel.add(e.acc
                .minMax(config.accMinValue, config.accMaxValue)
                .multiply(0.5 * elapsed * friction * m.density));

        e.vel.minMax(config.speedMinValue, config.speedMaxValue);

        e.pos.add(e.vel);

        e.forces.clear();
        e.collide = false;
    }

    /**
     * Apply a lower threshold on the double value
     *
     * @param value          the value to be zero-ified
     * @param valueThreshold the Threshold value below we consider value as 0.
     * @return the threshold value.
     */
    private double threshold(double value, double valueThreshold) {
        return (value < valueThreshold) ? 0.0 : value;
    }

    /**
     * Apply the constraints to the Entity.
     *
     * @param e the Entity to be constrained.
     */
    private void constrainsEntity(Entity e) {
        if (e.isAlive() || e.isPersistent()) {
            constrainToWorld(e, world);
        }
    }

    /**
     * Apply the World limitations to the Entity.
     *
     * @param e the Entity to be world constrained.
     */
    private void constrainToWorld(Entity e, World world) {
        if (e.cbox.getBounds().getX() < 0.0) {
            e.pos.x = 0.0;
            e.vel.x *= -1 * e.elasticity;
            e.acc.x = 0.0;
        }
        if (e.cbox.getBounds().getY() < 0.0) {
            e.pos.y = 0.0;
            e.vel.y *= -1 * e.elasticity;
            e.acc.y = 0.0;
        }
        if (e.cbox.getBounds().getX() + e.cbox.getBounds().getWidth() > world.area.getWidth()) {
            e.pos.x = world.area.getWidth() - e.width;
            e.vel.x *= -1 * e.elasticity;
            e.acc.x = 0.0;
        }
        if (e.cbox.getBounds().getY() + e.cbox.getBounds().getHeight() > world.area.getHeight()) {
            e.pos.y = world.area.getHeight() - e.height;
            e.vel.x *= -1 * e.elasticity;
            e.acc.x = 0.0;
        }
    }

    public void dispose() {

    }

    public Map<String, Influencer> getInfluencers() {
        return app.getEntities().values()
                .stream()
                .filter(e -> e instanceof Influencer)
                .collect(Collectors.toMap(e -> e.name, e -> (Influencer) e));
    }
}
