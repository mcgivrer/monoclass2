package com.demoing.app.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class AbstractApplicationTest {
    protected Application app;

    @BeforeEach
    protected void setup(String filename) {
        app = new Application(new String[] {}, filename);
        app.initializeServices();
        app.loadScenes();
    }

    @AfterEach
    protected void tearDown() {
        app.dispose();
        app = null;
    }

    @Test
    protected void testApplicationINstanciated() {
        assertNotNull(app, "Application has not been instanciated");
    }
}
