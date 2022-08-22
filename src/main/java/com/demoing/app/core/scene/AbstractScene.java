package com.demoing.app.core.scene;

import com.demoing.app.core.Application;
import com.demoing.app.core.behavior.Behavior;
import com.demoing.app.core.config.Configuration;
import com.demoing.app.core.entity.Light;
import com.demoing.app.core.math.Vec2d;
import com.demoing.app.core.service.physic.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractScene implements Scene {
    protected final String name;
    protected Map<String, Behavior> behaviors = new ConcurrentHashMap<>();
    /**
     * The list of Light manage by this scene
     */
    protected List<Light> lights = new ArrayList<>();

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
    public Map<String, Behavior> getBehaviors() {
        return null;
    }

    @Override
    public List<Light> getLights() {
        return null;
    }

    @Override
    public void dispose() {

    }
}
