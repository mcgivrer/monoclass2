package com.demoing.app.tests;

import com.demoing.app.core.Application;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TestScene implements Application.Scene {


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
        return "test";
    }

    @Override
    public Map<String, Application.Behavior> getBehaviors() {
        return null;
    }

    @Override
    public List<Application.Light> getLights() {
        return new ArrayList<>();
    }

    @Override
    public void dispose() {

    }
}
