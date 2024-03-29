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
    private World world;
    private Configuration config;
    public long updateTime;
    private final Map<String, Influencer> influencers = new ConcurrentHashMap<>();

    /**
     * Initialize the Physic Engine for the parent Application a, with the Configuration c
     * and in a World w.
     *
     * @param a the parent Application
     * @param c the Configuration where to find Physic Engine initialization parameters
     */
    public PhysicEngine(Application a, Configuration c) {
        this.app = a;
        this.config = c;
        this.world = new World()
                .setArea(config.worldWidth, config.worldHeight)
                .setGravity(new Vec2d(0.0, config.worldGravity))
                .setMaterial(DefaultMaterial.DEFAULT.get());
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
            // Reset all collision for the Entity e
            e.collide = false;
            e.colliders.clear();
            if (e.physicType.equals(PhysicType.DYNAMIC)) {
                updateEntity(e, elapsed);
                // Update all child
                e.getChild().forEach(childE -> {
                    updateEntity(childE, elapsed);
                    childE.update(elapsed);
                });
            }
            e.update(elapsed);

            // Update Entity Behavior (A specific event may have multiple Behavior)
            // this can be applied alto specifically to ParticleSystem particle's update and generation.
            e.behaviors.values().stream()
                    .forEach(
                            l -> l.stream()
                                    .filter(b -> b.filterOnEvent()
                                            .contains(Behavior.ON_UPDATE_ENTITY))
                                    .toList()
                                    .forEach(b -> b.update(app, e, elapsed)));

        });

        // Update Scene Behaviors
        if (Optional.ofNullable(app.getSceneManager().getActiveScene().getBehaviors()).isPresent()) {
            app.getSceneManager()
                    .getActiveScene()
                    .getBehaviors()
                    .values().stream()
                    .filter(b -> b.filterOnEvent()
                            .contains(Behavior.ON_UPDATE_SCENE))
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
        Material m = e.material;
        Vec2d g = new Vec2d(world.gravity.x, e.mass * world.gravity.y);
        for (Influencer i : getInfluencers().values()) {
            if (i.box.contains(e.box)) {
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

        // compute resulting friction
        double collisionFriction = e.colliders.stream()
                .filter(c -> c.collide)
                .mapToDouble(c -> c.material.friction)
                .reduce(m.friction, (a, b) -> a * b);
        e.friction = Double.min(collisionFriction, world.getMaterial().friction);

        // compute resulting elasticity
        double collisionElasticity = e.colliders.stream()
                .filter(c -> c.collide)
                .mapToDouble(c -> c.material.elasticity)
                .reduce(m.elasticity, (a, b) -> a * b);
        e.elasticity = Double.max(collisionElasticity, world.getMaterial().elasticity);

        e.vel.add(e.acc
                .minMax(config.accMinValue, config.accMaxValue)
                .multiply(0.5 * elapsed * e.friction * m.density));

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
            e.collide=true;
        }
        if (e.cbox.getBounds().getY() < 0.0) {
            e.pos.y = 0.0;
            e.vel.y *= -1 * e.elasticity;
            e.acc.y = 0.0;
            e.collide=true;
        }
        if (e.cbox.getBounds().getX() + e.cbox.getBounds().getWidth() > world.area.getWidth()) {
            e.pos.x = world.area.getWidth() - e.width;
            e.vel.x *= -1 * e.elasticity;
            e.acc.x = 0.0;
            e.collide=true;
        }
        if (e.cbox.getBounds().getY() + e.cbox.getBounds().getHeight() > world.area.getHeight()) {
            e.pos.y = world.area.getHeight() - e.height;
            e.vel.x *= -1 * e.elasticity;
            e.acc.x = 0.0;
            e.collide=true;
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

    public World getWorld() {
        return this.world;
    }

    public void setWorld(World world) {
        this.world = world;
    }
}
