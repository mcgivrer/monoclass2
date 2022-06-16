package com.demoing.app.core;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ApplicationConfigurationTest extends AbstractApplicationTest {

    @BeforeEach
    public void setup() {
        super.setup("/test-config.properties");
    }

    @Test
    public void testRunApp() {
        Assertions.assertNotNull(app);
    }

    @Test
    public void testBasicConfiguration() {
        Assertions.assertEquals("en_EN", app.config.defaultLanguage, "The default language configuration has not been set");
        Assertions.assertEquals(1, app.config.debug, "The debug level configuration has not been set");
        Assertions.assertEquals(60.0, app.config.fps, 0.1, "The Screen Width configuration has not been set");
        Assertions.assertEquals(320.0, app.config.screenWidth, 0.1, "The Screen Width configuration has not been set");
        Assertions.assertEquals(200.0, app.config.screenHeight, 0.1, "The Screen Height configuration has not been set");
        Assertions.assertEquals(2.5, app.config.displayScale, 0.1, "The Display Scale configuration has not been set");
        Assertions.assertEquals("test:com.demoing.app.tests.TestScene", app.config.scenes, "The list of scenes configuration has not been set");
        Assertions.assertEquals("test", app.config.defaultScene, "The default scene configuration has not been set");
    }

    @Test
    public void testPhysicEngineConfiguration() {
        Assertions.assertEquals(0.1, app.config.speedMinValue, 0.001, "The minimum speed configuration has not been set");
        Assertions.assertEquals(3.2, app.config.speedMaxValue, 0.001, "The maximum speed configuration has not been set");
        Assertions.assertEquals(0.01, app.config.accMinValue, 0.001, "The minimum acceleration configuration has not been set");
        Assertions.assertEquals(3.5, app.config.accMaxValue, 0.001, "The maximum acceleration configuration has not been set");
    }

    @Test
    public void testCollisionConfiguration() {
        Assertions.assertEquals(0.1, app.config.colSpeedMinValue, 0.001, "The minimum speed configuration has not been set");
        Assertions.assertEquals(3.2, app.config.colSpeedMaxValue, 0.001, "The maximum speed configuration has not been set");
    }

    @Test
    public void testWorldConfiguration() {
        Assertions.assertEquals(960.0, app.config.worldWidth, 0.1, "The World area Width configuration has not been set");
        Assertions.assertEquals(600.0, app.config.worldHeight, 0.1, "The World area height configuration has not been set");
        Assertions.assertEquals(-0.008, app.config.worldGravity, 0.0001, "The World default gravity configuration has not been set");
    }
}