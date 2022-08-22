package com.demoing.app.tests.core;

import com.demoing.app.core.Application;
import com.demoing.app.core.behavior.Behavior;
import com.demoing.app.core.entity.Entity;
import com.demoing.app.core.entity.ParticleSystem;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ParticleSystemCanHaveBehaviorsTest {
    ParticleSystem ps;

    @Test
    public void psCanHaveParticleUpdateBehavior() {
        ps = new ParticleSystem("psOneUpdate");
        ps.addParticleUpdate(new Behavior() {
            @Override
            public String filterOnEvent() {
                return ON_UPDATE_ENTITY;
            }

            @Override
            public void update(Application a, Entity e, double elapsed) {

            }

            @Override
            public void update(Application a, double elapsed) {
                // Nothing to do there
            }

            @Override
            public void onCollide(Application a, Entity e1, Entity e2) {
                // nothing to do there
            }
        });

        Assertions.assertNotEquals(0, ps.getBehaviors(Behavior.ON_UPDATE_ENTITY).size(), "THe ParticleSystem has no Update behavior");
    }

    @Test
    public void psCanHaveParticleGeneratorBehavior() {
        ps = new ParticleSystem("psTwoGenerator");
        ps.addParticleUpdate(new Behavior() {
            @Override
            public String filterOnEvent() {
                return ON_UPDATE_ENTITY;
            }

            @Override
            public void update(Application a, Entity e, double elapsed) {

            }

            @Override
            public void update(Application a, double elapsed) {
                // Nothing to do there
            }

            @Override
            public void onCollide(Application a, Entity e1, Entity e2) {
                // nothing to do there
            }
        });

        Assertions.assertNotEquals(0, ps.getBehaviors(Behavior.ON_UPDATE_ENTITY).size(), "THe ParticleSystem has no Update behavior");
    }

}
