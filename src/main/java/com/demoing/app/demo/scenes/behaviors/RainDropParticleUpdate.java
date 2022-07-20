package com.demoing.app.demo.scenes.behaviors;

import com.demoing.app.core.Application;
import com.demoing.app.core.behavior.Behavior;
import com.demoing.app.core.entity.Entity;
import com.demoing.app.core.service.physic.World;
import com.demoing.app.core.utils.Logger;

public class RainDropParticleUpdate implements Behavior {
    private final World world;

    public RainDropParticleUpdate(World world) {
        this.world = world;
    }

    @Override
    public String filterOnEvent() {
        return Behavior.ON_COLLISION;
    }

    @Override
    public void update(Application a, Entity e, double elapsed) {
        if (e.pos.y > world.area.getHeight()) {
            e.duration = 0;
        }
    }

    /**
     * No need to implement the Scene update for this {@link Behavior}.
     *
     * @param a       the parent application
     * @param elapsed the elapsed time since previous call.
     */
    @Override
    public void update(Application a, double elapsed) {

    }

    /**
     * if the particle collides with another entity, Log it !
     *
     * @param a  the parent application
     * @param e1 the first entity (the Particle)
     * @param e2 the second entity colliding the particle.
     */
    @Override
    public void onCollide(Application a, Entity e1, Entity e2) {

    }
}
