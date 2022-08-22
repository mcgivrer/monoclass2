package com.demoing.app.tests.scenes;

import java.awt.event.KeyEvent;
import java.util.List;
import java.util.Map;

import com.demoing.app.core.Application;
import com.demoing.app.core.behavior.Behavior;
import com.demoing.app.core.entity.Entity;
import com.demoing.app.core.entity.Light;
import com.demoing.app.core.gfx.Window;
import com.demoing.app.core.scene.Scene;

public class KeyScene implements Scene {


    private final String name;

    public KeyScene(String sceneName) {
        this.name = sceneName;
    }

    @Override
    public void prepare() {

    }

    @Override
    public boolean create(Application app) throws Exception {
        return true;
    }

    @Override
    public void update(Application app, double elapsed) {

    }

    @Override
    public void input(Application app) {
        Window win = app.getWindow();
        Entity player = app.getEntity("player");
        if (win.isKeyPressed(KeyEvent.VK_UP)) {
            player.addForce(0.0, -4.0);
        }
        if (win.isKeyPressed(KeyEvent.VK_DOWN)) {
            player.addForce(0.0, 4.0);

        }
        if (win.isKeyPressed(KeyEvent.VK_LEFT)) {

            player.addForce(-4.0, 0.0);
        }
        if (win.isKeyPressed(KeyEvent.VK_RIGHT)) {
            player.addForce(4.0, 0.0);
        }
    }

    @Override
    public String getName() {
        return name;
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
