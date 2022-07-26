package com.demoing.app.tests.scenes;

import com.demoing.app.core.Application;
import com.demoing.app.core.behavior.Behavior;
import com.demoing.app.core.entity.Light;
import com.demoing.app.core.scene.Scene;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TestScene implements Scene {
    private String name = "noname";

    public TestScene(String name) {
        this.name = name;
    }

    @Override
    public void prepare() {

    }

    @Override
    public boolean create(Application app) throws Exception {
        return false;
    }

    @Override
    public void update(Application app, double elapsed) {

    }

    @Override
    public void input(Application app) {

    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public Map<String, Behavior> getBehaviors() {
        return null;
    }

    @Override
    public List<Light> getLights() {
        return new ArrayList<>();
    }

    @Override
    public void dispose() {

    }
}
