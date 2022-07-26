package com.demoing.app.tests.core;

import com.demoing.app.core.Application;
import com.demoing.app.core.config.Configuration;

public class AbstractApplicationTest {
    protected Application app;

    protected void setup(String filename) {
        app = new Application(new String[]{}, filename);
        app.initializeServices();
        String[] scenes = (app.getConfiguration().scenes != null ? app.getConfiguration().scenes.split(",") : new String[]{});
        app.getSceneManager().loadScenes(scenes);
    }

    protected void tearDown() {
        app.dispose();
        app = null;
    }

}
