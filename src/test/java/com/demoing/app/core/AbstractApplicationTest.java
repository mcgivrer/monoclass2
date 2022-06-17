package com.demoing.app.core;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class AbstractApplicationTest {
    protected Application app;

    protected void setup(String filename) {
        app = new Application(new String[] {}, filename);
        app.initializeServices();
        app.loadScenes();
    }

    protected void tearDown() {
        app.dispose();
        app = null;
    }

    @Test
    protected void testApplicationINstanciated() {
        assertNotNull(app, "Application has not been instanciated");
    }
}
