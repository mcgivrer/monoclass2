package com.demoing.app.tests.features;


import com.demoing.app.core.entity.Entity;
import com.demoing.app.core.math.Vec2d;
import com.demoing.app.core.scene.Scene;
import com.demoing.app.core.service.physic.World;
import com.demoing.app.core.service.physic.material.DefaultMaterial;
import com.demoing.app.tests.scenes.TestScene;
import com.demoing.app.tests.core.AbstractApplicationTest;
import io.cucumber.java8.En;

import static org.junit.jupiter.api.Assertions.*;

public class PlayInAWorldStepdefs extends AbstractApplicationTest implements En {
    private World world;

    private Scene scene;

    public Vec2d initialPosition;

    public PlayInAWorldStepdefs() {

        Given("A new Game from configuration {string}", (String configFileName) -> {
            setup(configFileName);
        });
        And("I create a new Scene named {string} as default", (String sceneName) -> {
            app.sceneMgr.addScene(sceneName, new TestScene(sceneName));
            app.sceneMgr.activateScene(sceneName);
        });
        And("I create a World {string}", (String worldName) -> {
            world = new World(worldName);
            app.getPhysicEngine().setWorld(world);
        });
        And("I add a default Material to world {string}", (String worldName) -> {
            world.setMaterial(DefaultMaterial.DEFAULT.get());
        });
        And("A add an area of {double} x {double} to world {string}", (Double width, Double height, String worldName) -> {
            world.setArea(width, height);
        });
        Then("The game has a world to play with.", () -> {
            assertAll("The Game has not been correctly set with the world",
                    () -> assertNotNull(app.getPhysicEngine().getWorld().getArea(), "Area has not been set"),
                    () -> assertNotNull(app.getPhysicEngine().getWorld().getMaterial(), "Material has not been set"),
                    () -> assertNotNull(app.getPhysicEngine().getWorld().getGravity(), "gravity has not been set"));
        });
        Given("the existing scene {string}", (String sceneName) -> {
            scene = app.getSceneManager().getActiveScene();
        });
        And("I add and Entity {string}", (String entityName) -> {
            Entity player = new Entity(entityName);
            app.addEntity(player);
        });
        And("I set position to \\({double},{double}) to the Entity {string}", (Double x, Double y, String entityName) -> {
            initialPosition = new Vec2d(x, y);
            app.getEntity(entityName).setPosition(x, y);
        });
        Then("the Entity {string} can move vertically and horizontally in the World {string}.", (String entityName, String worldName) -> {
            Entity player = app.getEntity(entityName);
            for (int i = 0; i < 60; i++) {
                app.getPhysicEngine().update(16);
            }
            assertAll("Entity player has not moved",
                    () -> assertTrue(player.pos.x > initialPosition.x, "THe PLayer entity has not move on x"),
                    () -> assertTrue(player.pos.y > initialPosition.y, "THe PLayer entity has not move on y")
            );
        });
        And("I set speed to \\({double},{double}) to the Entity {string}", (Double dx, Double dy, String entityName) -> {
            Entity player = app.getEntity(entityName);
            player.setSpeed(dx, dy);
        });
    }
}
