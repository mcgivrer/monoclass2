package com.demoing.app.tests.features;


import com.demoing.app.core.entity.Entity;
import com.demoing.app.core.entity.Influencer;
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
            getApp().sceneMgr.addScene(sceneName, new TestScene(sceneName));
            getApp().sceneMgr.activateScene(sceneName);
        });
        And("I create a World {string}", (String worldName) -> {
            world = new World(worldName);
            getApp().getPhysicEngine().setWorld(world);
        });
        And("I add a default Material to world {string}", (String worldName) -> {
            world.setMaterial(DefaultMaterial.DEFAULT.get());
        });
        And("A add an area of {double} x {double} to world {string}",
                (Double width, Double height, String worldName) -> {
                    world.setArea(width, height);
                });

        Then("The game has a world to play with.", () -> {
            assertAll("The Game has not been correctly set with the world",
                    () -> assertNotNull(getApp().getPhysicEngine().getWorld().getArea(), "Area has not been set"),
                    () -> assertNotNull(getApp().getPhysicEngine().getWorld().getMaterial(), "Material has not been set"),
                    () -> assertNotNull(getApp().getPhysicEngine().getWorld().getGravity(), "gravity has not been set"));
        });

        Given("the existing scene {string}", (String sceneName) -> {
            scene = getApp().getSceneManager().getActiveScene();
        });

        And("I create an Entity {string} with size of \\({double},{double})", (String entityName, Double w, Double h) -> {
            Entity player = new Entity(entityName).setSize(w, h);
            getApp().addEntity(player);
        });
        And("I set position to \\({double},{double}) to the Entity {string}",
                (Double x, Double y, String entityName) -> {
                    initialPosition = new Vec2d(x, y);
                    addCache("initialPosition", initialPosition);
                    getApp().getEntity(entityName).setPosition(x, y);
                });
        And("I set speed to \\({double},{double}) to the Entity {string}",
                (Double dx, Double dy, String entityName) -> {
                    Entity player = getApp().getEntity(entityName);
                    player.setSpeed(dx, dy);
                });
        And("I create an Influencer named {string} at \\({double},{double}) with size of \\({double},{double})",
                (String influencerName, Double x, Double y, Double w, Double h) -> {
                    Influencer i = (Influencer) new Influencer(influencerName)
                            .setPosition(x, y)
                            .setSize(w, h);
                    getApp().addEntity(i);
                });
        And("I set a force of \\({double},{double}) on Influencer {string}",
                (Double fx, Double fy, String influencerName) -> {
                    Influencer i = (Influencer) getApp().getEntity(influencerName);
                    i.setForce(new Vec2d(fx, fy));
                });

        Then("the Entity {string} can move vertically and horizontally in the World {string}.",
                (String entityName, String worldName) -> {
                    Entity player = getApp().getEntity(entityName);
                    for (int i = 0; i < 60; i++) {
                        getApp().getPhysicEngine().update(16);
                    }
                    assertAll("Entity player has not moved",
                            () -> assertTrue(
                                    player.pos.x != initialPosition.x,
                                    String.format("The Player entity has not move on x (=%f)", player.pos.x)),
                            () -> assertTrue(
                                    player.pos.y != initialPosition.y,
                                    String.format("The Player entity has not move on y (=%f)", player.pos.y))
                    );
                });
    }
}
