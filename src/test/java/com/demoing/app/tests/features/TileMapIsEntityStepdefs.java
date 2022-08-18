package com.demoing.app.tests.features;

import com.demoing.app.core.entity.TileMap;
import com.demoing.app.core.scene.Scene;
import com.demoing.app.tests.core.AbstractApplicationTest;
import io.cucumber.java8.En;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TileMapIsEntityStepdefs extends AbstractApplicationTest implements En {

    private Scene scn;

    public TileMapIsEntityStepdefs() {
        Given("a Scene {string}", (String sceneName) -> {
            setup("tilemap_" + sceneName.toLowerCase(Locale.ROOT) + ".properties");
        });
        Then("I add a TileMap named {string}", (String tilemapName) -> {
            Scene scn = getApp().getSceneManager().getActiveScene();
            getApp().addEntity(new TileMap(tilemapName));
        });
        And("the TileMap {string} has Tile size of {int} x {int}",
                (String tilemapName, Integer tileWidth, Integer tileHeight) -> {
                    TileMap tm = (TileMap) (getApp().getEntity(tilemapName));
                    tm.setTileSize(tileWidth, tileHeight);

                });
        And("the TileMap {string} size is {int} x {int}", (String tilemapName, Integer width, Integer height) -> {
            TileMap tm = (TileMap) (getApp().getEntity(tilemapName));
            tm.setMapSize(width, height);
        });
        Then("the Scene contains a TileMap {string} with a map of {int} tiles.", (String tilemapName, Integer mapTileLength) -> {
            TileMap tm = (TileMap) (getApp().getEntity(tilemapName));
            assertEquals(mapTileLength, tm.getMapLength(), "The TileMap internal array does not match width and height");
        });
        And("a TileMap named {string} is created", (String tilemapName) -> {
            TileMap tm = (TileMap) (getApp().getEntity(tilemapName));
            assertEquals(tilemapName, tm.name, "A wrong TileMap is created");
        });
        Then("the Renderer has drawn the TileMap {string}.", (String tilemapName) -> {
            TileMap tm = (TileMap) (getApp().getEntity(tilemapName));
            getApp().getRender().draw(60);
            for (int d = 0; d < 240; d++) {
                getApp().getPhysicEngine().update(16);
                Thread.sleep(16);
            }
            assertTrue(tm.drawn, "the TileMap has not been drawn");
            assertEquals(tm.getMapLength(), tm.tileDrawnCounter, "the number of rendered tiles does not match map size");
        });

    }
}
