package com.demoing.app.demo.scenes;

import com.demoing.app.core.Application;
import com.demoing.app.core.behavior.Behavior;
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
                        0.1,
                        0.001,
                        1.0));

        // define Game global variables
        app.setAttribute("life", 5);
        app.setAttribute("score", 0);
        app.setAttribute("time", (long) 3 * (10 * 1000));

        // prepare Scene

        TileMap tm = (TileMap) TileMapLoader.load(app, this, "/maps/map_0_1.properties")
                .setPriority(8)
                .setLayer(3);
        app.addEntity(tm);
        // retrieve the "player" Entity.
        Entity player = tm.getEntity("player")
                .setSize(32, 32)
                .setCollisionBox(+4, -8, -4, -2)
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
                        "coin_", Color.YELLOW,
                        "enemy_", Color.RED,
                        "iflu_", Color.CYAN,
                        tm.name, Color.DARK_GRAY,
                        "pf_", Color.GRAY
                ));

        world.area.setRect(0, 0, tm.width, tm.height);

        addBehavior(new com.demoing.app.demo.scenes.PlayerOnInputBehavior());

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
            if (time <= 0 && !gameOver) {
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
        super.input(app);
    }

    @Override
    public String getName() {
        return "level";
    }

    @Override
    public Map<String, Behavior> getBehaviors() {
        return null;
    }
}
