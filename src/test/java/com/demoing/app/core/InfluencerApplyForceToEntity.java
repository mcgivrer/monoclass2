package com.demoing.app.core;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class InfluencerApplyForceToEntity extends AbstractApplicationTest {

    @BeforeEach
    public void setup() {
        app = new Application(new String[]{});
        app.initializeServices();
    }

    @AfterEach
    public void tearDown() {
        app.dispose();
        app = null;
    }

    @Test
    public void testWorldCanHaveInfluencer() {
        Assertions.assertNotNull(app.getPhysicEngine().getInfluencers(), "the PhysicEngine has no Influencers List");
    }

}
