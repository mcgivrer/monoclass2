package com.demoing.app.core.service.collision;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.demoing.app.core.Application;
import com.demoing.app.core.behavior.Behavior;
import com.demoing.app.core.config.Configuration;
import com.demoing.app.core.entity.Entity;
import com.demoing.app.core.math.MathUtils;
import com.demoing.app.core.math.Vec2d;
import com.demoing.app.core.service.physic.PhysicType;
import com.demoing.app.core.service.physic.World;
import com.demoing.app.core.utils.Logger;

/**
 * Collision Detector Service.
 *
 * @author Frédéric Delorme
 * @since 1.0.3
 */
public class CollisionDetector {
    private final Configuration config;
    private final Application app;
    private final World world;

    // ToDo! maintain a binTree to 'sub-space' world.
    public Map<String, Entity> colliders = new ConcurrentHashMap<>();

    /**
     * initialization of the CollisionDetector service
     *
     * @param a the parent Application
     * @param c the configuration to rely on
     * @param w the world where all the entities evolve.
     */
    public CollisionDetector(Application a, Configuration c, World w) {
        this.config = c;
        this.app = a;
        this.world = w;
    }

    /**
     * Adding an {@link Entity} to the collision detection service.
     *
     * @param e the {@link Entity} to kae part in the collision detection system.
     */
    public void add(Entity e) {

        colliders.put(e.name, e);
        e.getChild().forEach(
                ce -> colliders.put(ce.name, ce));
    }

    /**
     * Remove an {@link Entity} and all its child object from the collision
     * detection service.
     *
     * @param e the {@link Entity} to be removed from the collision detection
     *          system.
     */
    public void remove(Entity e) {
        e.getChild().forEach(
                ce -> {
                    colliders.remove(ce.name);
                });
        colliders.remove(e.name);
    }

    /**
     * Step into the detection
     *
     * @param elapsed
     */
    public void update(double elapsed) {
        detect();
    }

    private void detect() {
        List<Entity> targets = colliders.values().stream().filter(e -> e.isAlive() || e.isPersistent()).toList();
        for (Entity e1 : colliders.values()) {
            e1.collide = e1.collide || false;
            for (Entity e2 : targets) {
                e2.collide = e2.collide || false;
                if (e1.id != e2.id) {
                    if (e1.cbox.getBounds().intersects(e2.cbox.getBounds())) {
                        resolve(e1, e2);
                        applyBehaviors(e1, e2);
                    }
                }
            }
        }
    }

    private void applyBehaviors(Entity e1, Entity e2) {
        e1.behaviors.values().stream()
                .forEach(l -> l.stream()
                        .filter(b -> b.filterOnEvent()
                                .contains(Behavior.ON_COLLISION))
                        .toList()
                        .forEach(b -> {
                            b.onCollide(app, e1, e2);
                        }));
    }

    /**
     * Collision response largely inspired by the article from
     * <a href=
     * "https://spicyyoghurt.com/tutorials/html5-javascript-game-development/collision-detection-physics">collision-detection-physics</a>
     *
     * @param e1 first Entity in the collision
     * @param e2 second Entity in the collision
     */
    private void resolve(Entity e1, Entity e2) {
        e1.collide = true;
        e2.collide = true;

        Vec2d vp = new Vec2d((e2.pos.x - e1.pos.x), (e2.pos.y - e1.pos.y));
        double distance = Math
                .sqrt((e2.pos.x - e1.pos.x) * (e2.pos.x - e1.pos.x) + (e2.pos.y - e1.pos.y) * (e2.pos.y - e1.pos.y));
        Vec2d colNorm = new Vec2d(vp.x / distance, vp.y / distance);

        if (e1.physicType == PhysicType.DYNAMIC && e2.physicType == PhysicType.DYNAMIC) {
            e1.colliders.add(e2);
            e2.colliders.add(e1);

            Vec2d vRelSpeed = new Vec2d(e1.vel.x - e2.vel.x, e1.vel.y - e2.vel.y);
            double colSpeed = vRelSpeed.x * colNorm.x + vRelSpeed.y * colNorm.y;
            var impulse = 2 * colSpeed / (e1.mass * e1.material.density + e2.mass * e2.material.density);
            e1.vel.x -= MathUtils.ceilMinMaxValue(impulse * e2.mass * e2.material.density * colSpeed * colNorm.x,
                    config.speedMinValue, config.colSpeedMaxValue);
            e1.vel.y -= MathUtils.ceilMinMaxValue(impulse * e2.mass * e2.material.density * colSpeed * colNorm.y,
                    config.speedMinValue, config.colSpeedMaxValue);
            e2.vel.x += MathUtils.ceilMinMaxValue(impulse * e1.mass * e2.material.density * colSpeed * colNorm.x,
                    config.speedMinValue, config.colSpeedMaxValue);
            e2.vel.y += MathUtils.ceilMinMaxValue(impulse * e1.mass * e2.material.density * colSpeed * colNorm.y,
                    config.speedMinValue, config.colSpeedMaxValue);

            Logger.log(Logger.DETAILED, this.getClass(), "e1.%s collides e2.%s Vp=%s / dist=%f / norm=%s\n", e1.name,
                    e2.name, vp, distance, colNorm);

        } else {

            if (e1.physicType == PhysicType.DYNAMIC && e2.physicType == PhysicType.STATIC) {
                e1.colliders.add(e2);
                if (e2.material.elasticity > 0) {
                    // 4 = nb min pixel to authorise going upper e2 object.
                    if (e1.pos.y + e1.height > e2.pos.y && vp.y > 0) {
                        e1.pos.y = e2.pos.y - e1.height;
                        e1.acc.y = -e1.acc.y * e1.elasticity * e2.material.density;
                    } else {
                        e1.acc.y = -e1.acc.y * e1.elasticity * e2.material.density;
                        e1.pos.y = e2.pos.y + e2.height;
                    }
                    Logger.log(Logger.DETAILED, this.getClass(), "e1.%s collides static e2.%s\n", e1.name, e2.name);
                }
            }
            if (e1.physicType == PhysicType.DYNAMIC && e2.physicType == PhysicType.NONE) {
                e1.colliders.add(e2);
            }
        }
        e1.update(1);
        e2.update(1);
    }
}
