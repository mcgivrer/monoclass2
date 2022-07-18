package com.demoing.app.demo.scenes.behaviors;

import com.demoing.app.core.Application;
import com.demoing.app.core.behavior.Behavior;
import com.demoing.app.core.entity.Entity;

public final class EnemyOnCollisionBehavior implements Behavior {
    @Override
    public String filterOnEvent() {
        return Behavior.ON_COLLISION;
    }

    @Override
    public void onCollide(Application a, Entity e1, Entity e2) {
        // If hurt a dead attribute platform => Die !
        if ((boolean) e2.getAttribute("dead", false) && e1.isAlive()) {
            int score = (int) a.getAttribute("score", 0);
            int points = (int) e1.getAttribute("points", 0);
            a.setAttribute("score", score + points);
            e1.setDuration(0);
        }
    }

    @Override
    public void update(Application a, Entity e, double d) {

    }

    @Override
    public void update(Application a, double d) {
    }
}
