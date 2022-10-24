package com.demoing.app.demo.scenes;

import com.demoing.app.core.Application;
import com.demoing.app.core.behavior.Behavior;
import com.demoing.app.core.entity.Entity;
import com.demoing.app.core.gfx.Window;
import com.demoing.app.core.math.Vec2d;
import com.demoing.app.core.scene.Scene;

import java.awt.event.KeyEvent;
import java.util.Optional;

public class PlayerOnInputBehavior implements Behavior {

    @Override
    public String filterOnEvent() {
        return ON_INPUT_SCENE;
    }

    @Override
    public void update(Application a, Entity e, double elapsed) {

    }

    @Override
    public void update(Application a, double elapsed) {

    }

    @Override
    public void onCollide(Application a, Entity e1, Entity e2) {

    }

    @Override
    public void input(Application app, Scene s) {
        Window win = app.getWindow();
        Entity p = app.getEntity("player");
        if (Optional.ofNullable(p).isPresent()) {
            double speed = (double) p.getAttribute("accStep", 0.05);
            double jumpFactor = (double) p.getAttribute("jumpFactor", 12.0);
            boolean action = (boolean) p.getAttribute("action", false);
            if (win.isCtrlPressed()) {
                speed *= 2;
            }
            if (win.isShiftPressed()) {
                speed *= 4;
            }
            p.activateAnimation("idle");
            if (win.isKeyPressed(KeyEvent.VK_LEFT)) {
                p.activateAnimation("walk");
                p.forces.add(new Vec2d(-speed, 0.0));
                action = true;
            }
            if (win.isKeyPressed(KeyEvent.VK_RIGHT)) {
                p.activateAnimation("walk");
                p.forces.add(new Vec2d(speed, 0.0));
                action = true;
            }
            if (win.isKeyPressed(KeyEvent.VK_UP)) {
                p.activateAnimation("jump");
                p.forces.add(new Vec2d(0.0, -jumpFactor * speed));
                action = true;
            }
            if (win.isKeyPressed(KeyEvent.VK_DOWN)) {
                p.forces.add(new Vec2d(0.0, speed));
                action = true;
            }

            if (!action) {
                p.vel.x *= (p.friction);
                p.vel.x *= (p.friction);
            }
        }
    }
}