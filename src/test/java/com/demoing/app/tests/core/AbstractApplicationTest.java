package com.demoing.app.tests.core;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.demoing.app.core.Application;

public class AbstractApplicationTest {
    protected static Map<String, Object> cache = new ConcurrentHashMap<>();

    protected Application setup(String name, String filename) {

        Application app = new Application(new String[]{}, filename);
        app.initializeServices();
        String[] scenes = (app.getConfiguration().scenes != null ? app.getConfiguration().scenes.split(",") : new String[]{});
        app.getSceneManager().loadScenes(scenes);
        cache.put(name, app);
        return app;
    }


    protected Application setup(String filename) {
        Application app = setup("app", filename);
        return app;
    }

    protected void tearDown() {
        tearDown("app");
    }

    protected void tearDown(String name) {
        getApp(name).dispose();
        cache.remove(name);
    }

    public Application getApp() {
        return getApp("app");
    }

    public Application getApp(String name) {
        return (Application) cache.get(name);
    }

    public <T> void addCache(String objectName, T object) {
        cache.put(objectName, object);
    }

    public Object getObject(String objectName) {
        return cache.get(objectName);
    }
}
