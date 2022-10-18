package com.demoing.app.tests.core;

import com.demoing.app.core.entity.Entity;
import com.demoing.app.core.entity.Influencer;
import com.demoing.app.core.math.Vec2d;
import com.demoing.app.core.entity.helpers.EntityType;
import com.demoing.app.core.service.physic.PhysicType;
import com.demoing.app.core.service.physic.World;
import com.demoing.app.core.service.physic.material.DefaultMaterial;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.awt.*;

import static org.junit.jupiter.api.Assertions.*;

public class InfluencerApplyForceToEntityTest extends AbstractApplicationTest {

    @BeforeEach
    public void setup() {
        super.setup("test-influencers.properties");
    }

    @AfterEach
    public void tearDown() {
        super.tearDown();
    }

    @Test
    @DisplayName("World can support Influencer's")
    public void aWorldCanHaveInfluencer() {
        assertNotNull(
                getApp().getPhysicEngine().getInfluencers(),
                "the PhysicEngine has no Influence's List");
    }

    @Test
    @DisplayName("Adding an Influencer to the World")
    public void addingOneInfluencer() {
        Influencer i = new Influencer("influencer_1")
                .setForce(new Vec2d(10, 10))
                .setGravity(new Vec2d(0.0, -0.981));
        getApp().addEntity(i);
        assertEquals(
                1, getApp().getPhysicEngine().getInfluencers().size(),
                "The PhysicEngine was not updated with the created Influencer");
    }

    @Test
    @DisplayName("World's influencer effects are applied to an Entity ")
    public void addOneEntityUnderInfluencerAction() {
        World w = getApp().getPhysicEngine().getWorld();
        // stop gravity effect on the application's World instance.
        w.setGravity(new Vec2d(0.0, 0.0)).setMaterial(DefaultMaterial.DEFAULT.get());

        // Create an Influencer in the initialized app World
        Influencer i = (Influencer) new Influencer("influencer_1")
                .setForce(new Vec2d(1.0, 1.0))
                .setPosition(0.0, 0.0)
                .setSize(w.area.getWidth(), 200.0)
                .setPhysicType(PhysicType.NONE)
                .setColor(new Color(0.0f, 0.0f, 0.5f, .07f));
        getApp().addEntity(i);

        // create a simple Rectangle entity to be influenced by the "influencer_1"
        Entity e1 = new Entity("entity_1")
                .setSize(10, 10)
                .setPosition(10, 10)
                .setPhysicType(PhysicType.DYNAMIC)
                .setType(EntityType.RECTANGLE);
        getApp().addEntity(e1);

        // 1 second of update (60 call for physic engine call)
        for (int n = 0; n < 60; n++) {
            getApp().getPhysicEngine().update(16.0);
        }

        assertAll("The Entity has not been updated by the Influencer force",
                () -> assertTrue(e1.pos.x > 10.0, "The Entity has not been updated by the Influencer force on the X axis"),
                () -> assertTrue(e1.pos.y > 10.0, "The Entity has not been updated by the Influencer force on the Y axis"));
    }

}
