package com.demoing.app.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractScene implements Application.Scene {
    protected final String name;
    protected Map<String, Application.Behavior> behaviors = new ConcurrentHashMap<>();
    /**
     * The list of Light manage by this scene
     */
    protected List<Application.Light> lights = new ArrayList<>();

    public AbstractScene(String name) {
        this.name = name;
    }

    @Override
    public abstract void prepare();

    @Override
    public abstract boolean create(Application app) throws Exception;

    @Override
    public abstract void update(Application app, double elapsed);

    @Override
    public abstract void input(Application app);

    @Override
    public String getName() {
        return null;
    }

    @Override
    public Map<String, Application.Behavior> getBehaviors() {
        return null;
    }

    @Override
    public List<Application.Light> getLights() {
        return null;
    }

    @Override
    public void dispose() {

    }
}
