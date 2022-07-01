package com.demoing.app.core.service.collision;

import com.demoing.app.core.Application;
import com.demoing.app.core.behavior.Behavior;
import com.demoing.app.core.config.Configuration;
import com.demoing.app.core.entity.Entity;
import com.demoing.app.core.math.MathUtils;
import com.demoing.app.core.service.physic.PhysicType;
import com.demoing.app.core.math.Vec2d;
import com.demoing.app.core.service.physic.World;
import com.demoing.app.core.utils.Logger;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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

    public CollisionDetector(Application a, Configuration c, World w) {
        this.config = c;
        this.app = a;
        this.world = w;
    }

    public void add(Entity e) {
        colliders.put(e.name, e);
    }

    public void update(double elapsed) {
        detect();
    }

    private void detect() {
        List<Entity> targets = colliders.values().stream().filter(e -> e.isAlive() || e.isPersistent()).toList();
        for (Entity e1 : colliders.values()) {
            e1.collide = false;
            for (Entity e2 : targets) {
                e2.collide = false;
                if (e1.id != e2.id && e1.cbox.getBounds().intersects(e2.cbox.getBounds())) {
                    resolve(e1, e2);
                    e1.behaviors.values().stream()
                            .filter(b -> b.filterOnEvent().equals(Behavior.onCollision)).toList()
                            .forEach(b -> b.onCollide(app, e1, e2));
                }
            }
        }
    }

    /**
     * Collision response largely inspired by the article from
     * https://spicyyoghurt.com/tutorials/html5-javascript-game-development/collision-detection-physics
     *
     * @param e1 first Entity in the collision
     * @param e2 second Entity in the collision
     */
    private void resolve(Entity e1, Entity e2) {
        e1.collide = true;
        e2.collide = true;
        Vec2d vp = new Vec2d((e2.pos.x - e1.pos.x), (e2.pos.y - e1.pos.y));
        double distance = Math.sqrt((e2.pos.x - e1.pos.x) * (e2.pos.x - e1.pos.x) + (e2.pos.y - e1.pos.y) * (e2.pos.y - e1.pos.y));
        Vec2d colNorm = new Vec2d(vp.x / distance, vp.y / distance);
        if (e1.physicType == PhysicType.DYNAMIC && e2.physicType == PhysicType.DYNAMIC) {
            Vec2d vRelSpeed = new Vec2d(e1.vel.x - e2.vel.x, e1.vel.y - e2.vel.y);
            double colSpeed = vRelSpeed.x * colNorm.x + vRelSpeed.y * colNorm.y;
            var impulse = 2 * colSpeed / (e1.mass + e2.mass);
            e1.vel.x -= MathUtils.ceilMinMaxValue(impulse * e2.mass * colSpeed * colNorm.x,
                    config.speedMinValue, config.colSpeedMaxValue);
            e1.vel.y -= MathUtils.ceilMinMaxValue(impulse * e2.mass * colSpeed * colNorm.y,
                    config.speedMinValue, config.colSpeedMaxValue);
            e2.vel.x += MathUtils.ceilMinMaxValue(impulse * e1.mass * colSpeed * colNorm.x,
                    config.speedMinValue, config.colSpeedMaxValue);
            e2.vel.y += MathUtils.ceilMinMaxValue(impulse * e1.mass * colSpeed * colNorm.y,
                    config.speedMinValue, config.colSpeedMaxValue);
            if (e1.name.equals("player")) {
                Logger.log(Logger.FINED, this.getClass(), "e1.%s collides e2.%s Vp=%s / dist=%f / norm=%s\n", e1.name, e2.name, vp, distance, colNorm);
            }
        } else {
            if (e1.physicType == PhysicType.DYNAMIC && e2.physicType == PhysicType.STATIC) {
                if (e1.pos.y + e1.height > e2.pos.y && vp.y > 0) {
                    e1.pos.y = e2.pos.y - e1.height;
                    e1.vel.y = -e1.vel.y * e1.elasticity;
                } else {
                    e1.vel.y = -e1.vel.y * e1.elasticity;
                    e1.pos.y = e2.pos.y + e2.height;
                }
                if (e1.name.equals("player")) {
                    Logger.log(Logger.FINED, this.getClass(), "e1.%s collides static e2.%s\n", e1.name, e2.name);
                }
            }
        }
    }
}
