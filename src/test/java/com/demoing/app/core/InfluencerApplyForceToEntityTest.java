package com.demoing.app.core;

import com.demoing.app.core.Application.Entity;
import com.demoing.app.core.Application.Influencer;
import com.demoing.app.core.Application.Vec2d;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class InfluencerApplyForceToEntityTest extends AbstractApplicationTest {

    @BeforeEach
    public void setup() {
        super.setup("/test-influencers.properties");
    }

    @AfterEach
    public void tearDown() {
        super.tearDown();
    }

    @Test
    public void aWorldCanHaveInfluencer() {
        assertNotNull(
                app.getPhysicEngine().getInfluencers(),
                "the PhysicEngine has no Influencers List");
    }

    @Test
    public void addingOneInfluencer() {
        Influencer i = new Influencer("influencer_1")
                .setForce(new Vec2d(10, 10))
                .setGravity(new Vec2d(0.0, -0.981));
        app.addEntity(i);
        assertEquals(
                1, app.getPhysicEngine().getInfluencers().size(),
                "The PhysicEngine was not updated with the created Influencer");
    }

    @Test
    public void addOneEntityUnderInfluencerAction() {
        // stop gravity effect on the application's World instance.
        app.getWorld().setGravity(0.0);

        // Create an Influencer in the initialized app World
        Influencer i = (Influencer) new Influencer("influencer_1")
                .setForce(new Vec2d(1.0, 0.0))
                .setPosition(0, 0)
                .setSize(200, 200);
        app.getPhysicEngine().addInfluencer(i);

        // create a simple Rectangle entity to be influenced by the "influencer_1"
        Entity e1 = new Entity("entity_1")
                .setSize(10, 10)
                .setPosition(10, 10)
                .setPhysicType(Application.PhysicType.DYNAMIC)
                .setType(Application.EntityType.RECTANGLE);
        app.addEntity(e1);

        // 1 second of update (60 call for physic engine call)
        for (int n = 0; n < 60; n++) {
            app.getPhysicEngine().update(16.0);
        }

        assertTrue(e1.pos.x > 10.0,
                "The Entity has not been updated by the Influencer force on the X axis");
        assertTrue(
                e1.pos.y > 10.0,
                "The Entity has not been updated by the Influencer force on the Y axis");
    }

}
