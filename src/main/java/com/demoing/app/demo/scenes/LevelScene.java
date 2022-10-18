package com.demoing.app.demo.scenes;

import com.demoing.app.core.Application;
import com.demoing.app.core.gfx.Window;
import com.demoing.app.core.io.TileMapLoader;
import com.demoing.app.core.math.Vec2d;
import com.demoing.app.core.scene.AbstractScene;
import com.demoing.app.core.service.physic.material.Material;
import com.demoing.app.core.entity.*;
import com.demoing.app.demo.scenes.behaviors.PlayerOnCollisionBehavior;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.Rectangle2D;
import java.util.Map;
import java.util.Optional;

import static com.demoing.app.core.entity.EntityType.IMAGE;

/**
 * Provide the default resources and objects for a Scene.
 *
 * @author Frédéric Delorme
 * @since 1.0.6
 **/
public class LevelScene extends AbstractScene {

    private boolean gameOver;

    public LevelScene(String name) {
        super(name);
    }


    @Override
    public void prepare() {
        super.prepare();
    }

    @Override
    public boolean create(Application app) throws Exception {

        // define default world friction (air resistance ?)
        world = app.getPhysicEngine().getWorld();
        world.setMaterial(
                new Material(
                        "world",
                        1.0,
                        0.001,
                        1.0));

        // define Game global variables
        app.setAttribute("life", 5);
        app.setAttribute("score", 0);
        app.setAttribute("time", (long) 3 * (60 * 1000));

        // prepare Scene

        TileMap tm = (TileMap) TileMapLoader.load(app, this, "/maps/map_0_1.properties")
                .setPriority(4)
                .setLayer(10);
        app.addEntity(tm);
        // retrieve the "player" Entity.
        Entity player = tm.getEntity("player").setType(IMAGE)
                .setCollisionBox(+4, -8, -4, -2)
                .addAnimation("idle",
                        0, 0,
                        32, 32,
                        new int[]{450, 60, 60, 250, 60, 60, 60, 450, 60, 60, 60, 250, 60},
                        "/images/sprites01.png", -1)
                .addAnimation("walk",
                        0, 32,
                        32, 32,
                        new int[]{60, 60, 60, 150, 60, 60, 60, 150},
                        "/images/sprites01.png", -1)
                .addAnimation("jump",
                        0, 5 * 32,
                        32, 32,
                        new int[]{60, 60, 250, 250, 60, 60},
                        "/images/sprites01.png", -1)
                .addAnimation("dead",
                        0, 7 * 32,
                        32, 32,
                        new int[]{160, 160, 160, 160, 160, 160, 500},
                        "/images/sprites01.png", 0)
                .activateAnimation("idle");

        // define the Camera.
        Camera cam = new Camera("cam01")
                .setViewport(new Rectangle2D.Double(0, 0, app.config.screenWidth, app.config.screenHeight))
                .setTarget(player)
                .setTweenFactor(0.005);
        app.render.addCamera(cam);

        createHUD(
                app,
                player,
                Map.of(
                        "player", Color.BLUE,
                        "enemy", Color.RED,
                        "coin", Color.YELLOW
                ));

        world.area.setRect(0, 0, tm.width, tm.height);
        return true;
    }

    @Override
    public void update(Application app, double elapsed) {
        if (app.getEntities().containsKey("score") && app.getEntities().containsKey("player")) {
            Entity player = app.getEntity("player");

            // Update timer
            long time = (long) app.getAttribute("time", 0);
            time -= elapsed;
            time = time >= 0 ? time : 0;
            app.setAttribute("time", time);

            // display timer
            ValueEntity timeTxt = (ValueEntity) app.getEntity("time");
            timeTxt.setValue((int) (time / 1000));

            // if time=0 => game over !
            if (time == 0 && !gameOver) {
                gameOver(app, player);
            }

            // update score
            int score = (int) app.getAttribute("score", 0);
            ValueEntity scoreEntity = (ValueEntity) app.getEntity("score");
            scoreEntity.setValue(score);

            int life = (int) app.getAttribute("life", 0);
            ValueEntity lifeEntity = (ValueEntity) app.getEntity("life");
            lifeEntity.setValue(life);

            int energy = (int) player.getAttribute("energy", 0);
            GaugeEntity energyEntity = (GaugeEntity) app.getEntity("energy");
            energyEntity.setValue(energy);

            int mana = (int) player.getAttribute("mana", 0);
            GaugeEntity manaEntity = (GaugeEntity) app.getEntity("mana");
            manaEntity.setValue(mana);

            if (energy <= 0 && life <= 0 && !gameOver) {
                gameOver(app, player);
            }
        }
    }


    private void gameOver(Application app, Entity player) {
        player.activateAnimation("dead");
        TextEntity youAreDead = (TextEntity) app.getEntities().get("YouAreDead");
        youAreDead.setInitialDuration(-1);
        gameOver = true;
    }

    @Override
    public void input(Application app) {
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

    @Override
    public String getName() {
        return "level";
    }
}
