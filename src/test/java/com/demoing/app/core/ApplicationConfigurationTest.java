package com.demoing.app.core;

import org.junit.Assert;
import org.junit.Test;

public class ApplicationConfigurationTest {

    Application app;

    @Test
    public void testRunApp() {
        app = new Application(new String[]{});
        Assert.assertNotNull(app);
    }

    @Test
    public void testBasicConfiguration() {
        app = new Application(new String[]{});
        Assert.assertEquals("The default language configuration has not been set", "en_EN", app.config.defaultLanguage);
        Assert.assertEquals("The debug level configuration has not been set", 1, app.config.debug);
        Assert.assertEquals("The Screen Width configuration has not been set", 60.0, app.config.fps, 0.1);
        Assert.assertEquals("The Screen Width configuration has not been set", 320.0, app.config.screenWidth, 0.1);
        Assert.assertEquals("The Screen Height configuration has not been set", 200.0, app.config.screenHeight, 0.1);
        Assert.assertEquals("The Display Scale configuration has not been set", 2.5, app.config.displayScale, 0.1);
        Assert.assertEquals("The list of scenes configuration has not been set", "test:com.demoing.app.tests.TestScene", app.config.scenes);
        Assert.assertEquals("The default scene configuration has not been set", "test", app.config.defaultScene);
    }

    @Test
    public void testPhysicEngineConfiguration() {
        app = new Application(new String[]{});
        Assert.assertEquals("The minimum speed configuration has not been set", 0.1, app.config.speedMinValue, 0.001);
        Assert.assertEquals("The maximum speed configuration has not been set", 3.2, app.config.speedMaxValue, 0.001);
        Assert.assertEquals("The minimum acceleration configuration has not been set", 0.01, app.config.accMinValue, 0.001);
        Assert.assertEquals("The maximum acceleration configuration has not been set", 3.5, app.config.accMaxValue, 0.001);
    }

    @Test
    public void testCollisionConfiguration() {
        app = new Application(new String[]{});
        Assert.assertEquals("The minimum speed configuration has not been set", 0.1, app.config.colSpeedMinValue, 0.001);
        Assert.assertEquals("The maximum speed configuration has not been set", 3.2, app.config.colSpeedMaxValue, 0.001);
    }

    @Test
    public void testWorldConfiguration() {
        app = new Application(new String[]{});
        Assert.assertEquals("The World area Width configuration has not been set", 960.0, app.config.worldWidth, 0.1);
        Assert.assertEquals("The World area height configuration has not been set", 600.0, app.config.worldHeight, 0.1);
        Assert.assertEquals("The World default gravity configuration has not been set", -0.008, app.config.worldGravity, 0.0001);
    }


}