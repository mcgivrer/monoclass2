package com.demoing.app.tests.features;

import com.demoing.app.core.entity.Entity;
import com.demoing.app.core.entity.TileMap;
import com.demoing.app.core.scene.Scene;
import com.demoing.app.tests.core.AbstractApplicationTest;
import io.cucumber.java8.En;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

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
        And("the Scene creates a TileMap named {string}", (String tilemapName) -> {
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
        Then("the TileMap {string} has an Object named {string}", (String tilemapName, String objectName) -> {
            TileMap tm = (TileMap) (getApp().getEntity(tilemapName));
            boolean status = false;
            for (Map<String, Object> item : tm.getEntities().values()) {
                if (item.get("name").equals(objectName)) {
                    status = true;
                    break;
                }
            }
            assertTrue(status, "The entity name " + objectName + " in tilemap " + tilemapName + "is not declared");
        });
        And("the Object named {string} from TileMap {string} has attribute {string} with value {string}",
                (String objectName, String tilemapName, String attributeName, String attributeValue) -> {
                    TileMap tm = (TileMap) (getApp().getEntity(tilemapName));
                    Map<String, Object> objAttributes = findObject(tm, objectName);
                    if (Optional.ofNullable(objAttributes).isPresent()) {
                        assertEquals(attributeValue,
                                (String) objAttributes.get(attributeName).toString(),
                                "The attribute " + attributeName + " has not value " + attributeValue);
                    } else {
                        fail("The attribute " + attributeName + " does not exist in object " + objectName);
                    }
                });
        Then("a Entity named {string} is created by TileMap {string}", (String objectName, String tilemapName) -> {
            Entity entityFound = findEntityFromTilemap(tilemapName, objectName);
            assertTrue(Optional.ofNullable(entityFound).isPresent(),
                    "The Entity '" + objectName
                            + "' has not been created by TileMap " + tilemapName);
        });
        Then("an Entity named {string} is created by TileMap {string}", (String entityName, String tilemapName) -> {
            Entity player = findEntityFromTilemap(tilemapName, entityName);
            assertTrue(Optional.ofNullable(player).isPresent(),
                    "The Entity '" + entityName
                            + "' has not been created by TileMap " + tilemapName);
        });
    }

    private Entity findEntityFromTilemap(String tilemapName, String entityName) {
        TileMap tm = (TileMap) (getApp().getEntity(tilemapName));
        Entity entityFound = null;
        for (Entity e : tm.getChild()) {
            if (e.name.equals(entityName)) {
                entityFound = e;
            }
            break;

        }
        return entityFound;
    }

    private Map<String, Object> findObject(TileMap tm, String objectName) {
        for (Map<String, Object> item : tm.getEntities().values()) {
            if (item.get("name").equals(objectName)) {
                return item;
            }
        }
        return null;
    }

    private Optional<Entity> findEntity(TileMap tm, String game) {
        return tm.getChild().stream().filter(e -> e.name.equals(game)).limit(1).findFirst();
    }
}
