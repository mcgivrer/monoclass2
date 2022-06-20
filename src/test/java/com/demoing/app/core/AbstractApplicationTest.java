package com.demoing.app.core;

public class AbstractApplicationTest {
    protected Application app;

    protected void setup(String filename) {
        app = new Application(new String[]{},filename);
        app.initializeServices();
        app.loadScenes();
    }

    protected void tearDown() {
        app.dispose();
        app = null;
    }

}
