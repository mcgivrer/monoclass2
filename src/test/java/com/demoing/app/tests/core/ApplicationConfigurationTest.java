package com.demoing.app.tests.core;

import com.demoing.app.core.config.Configuration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ApplicationConfigurationTest extends AbstractApplicationTest {

    @BeforeEach
    public void setup() {
        super.setup("test-config.properties");
    }

    @Test
    public void testRunApp() {
        Assertions.assertNotNull(getApp());
    }

    private Configuration getConfig() {
        return getApp().config;
    }

    @Test
    @DisplayName("Retrieve basic configuration about language, scree size and default Scene")
    public void testBasicConfiguration() {
        Assertions.assertEquals("en_EN", getConfig().defaultLanguage, "The default language configuration has not been set");
        Assertions.assertEquals(1, getConfig().debug, "The debug level configuration has not been set");
        Assertions.assertEquals(60.0, getConfig().fps, 0.1, "The Screen Width configuration has not been set");
        Assertions.assertEquals(320.0, getConfig().screenWidth, 0.1, "The Screen Width configuration has not been set");
        Assertions.assertEquals(200.0, getConfig().screenHeight, 0.1, "The Screen Height configuration has not been set");
        Assertions.assertEquals(2.5, getConfig().displayScale, 0.1, "The Display Scale configuration has not been set");
        Assertions.assertEquals("test:com.demoing.app.tests.scenes.TestScene", getConfig().scenes, "The list of scenes configuration has not been set");
        Assertions.assertEquals("test", getConfig().defaultScene, "The default scene configuration has not been set");
    }

    @Test
    @DisplayName("Retrieve the PhysicEngine specific configuration  parameters")
    public void testPhysicEngineConfiguration() {
        Assertions.assertEquals(0.1, getConfig().speedMinValue, 0.001, "The minimum speed configuration has not been set");
        Assertions.assertEquals(3.2, getConfig().speedMaxValue, 0.001, "The maximum speed configuration has not been set");
        Assertions.assertEquals(0.01, getConfig().accMinValue, 0.001, "The minimum acceleration configuration has not been set");
        Assertions.assertEquals(3.5, getConfig().accMaxValue, 0.001, "The maximum acceleration configuration has not been set");
    }

    @Test
    @DisplayName("Retrieve the Collision service configuration")
    public void testCollisionConfiguration() {
        Assertions.assertEquals(0.1, getConfig().colSpeedMinValue, 0.001, "The minimum speed configuration has not been set");
        Assertions.assertEquals(3.2, getConfig().colSpeedMaxValue, 0.001, "The maximum speed configuration has not been set");
    }

    @Test
    @DisplayName("Defining the default World parameters")
    public void testWorldConfiguration() {
        Assertions.assertEquals(960.0, getConfig().worldWidth, 0.1, "The World area Width configuration has not been set");
        Assertions.assertEquals(600.0, getConfig().worldHeight, 0.1, "The World area height configuration has not been set");
        Assertions.assertEquals(-0.008, getConfig().worldGravity, 0.0001, "The World default gravity configuration has not been set");
    }

    @Test
    @DisplayName("Defining configuration for debug: activation, level and filter")
    public void testDebugAndLogConfiguration() {
        Assertions.assertEquals(1, getConfig().debug, 0, "The debug level has not been set");
        Assertions.assertEquals("test_ent_", getConfig().debugObjectFilter, "The debug object filter level has not been set");
        Assertions.assertEquals(2, getConfig().logLevel, 0, "The logger level has not been set");
    }
}