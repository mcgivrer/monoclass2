package com.demoing.app.core;

import org.junit.Assert;
import org.junit.Test;

import com.demoing.app.core.Application;

public class ApplicationTest {

    Application app;

    @Test
    public void testRunApp() {
        app = new Application(new String[] {});
        Assert.assertNotNull(app);
    }
}