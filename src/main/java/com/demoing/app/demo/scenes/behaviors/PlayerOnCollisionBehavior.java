package com.demoing.app.demo.scenes.behaviors;

import com.demoing.app.core.Application;
import com.demoing.app.core.behavior.Behavior;
import com.demoing.app.core.entity.Entity;
import com.demoing.app.core.scene.Scene;

/**
 * Define the behavior to be applied when a PLayer colliding with an enemy or a ball.
 * reducing the player 'energy' attribute's value according to the 'hurt' enemy attribute value.
 *
 * @author Frédéric Delorme
 * @since 1.0.6
 */
public final class PlayerOnCollisionBehavior implements Behavior {
    private final Scene demoScene;

    public PlayerOnCollisionBehavior(Scene demoScene) {
        this.demoScene = demoScene;
    }

    @Override
    public String filterOnEvent() {
        return ON_COLLISION;
    }

    @Override
    public void onCollide(Application a, Entity e1, Entity e2) {
        if ("ball_,enemy_".contains(e2.name)) {
            reducePlayerEnergy(a, e1, e2);
        }
    }

    @Override
    public void update(Application a, Entity e, double d) {

    }

    public void update(Application a, double d) {

    }

    public void reducePlayerEnergy(Application app, Entity player, Entity e) {
        int hurt = (int) e.getAttribute("hurt", 0);
        int energy = (int) player.getAttribute("energy", 0);
        energy -= hurt;
        if (energy < 0) {
            int life = (int) app.getAttribute("life", 0);
            life -= 1;
            if (life < 0) {
                app.setAttribute("endOfGame", true);
                player.setDuration(0);
            } else {
                app.setAttribute("life", life);
                player.setAttribute("energy", 100);
            }
        } else {
            player.setAttribute("energy", energy -= hurt);
        }
    }
}
