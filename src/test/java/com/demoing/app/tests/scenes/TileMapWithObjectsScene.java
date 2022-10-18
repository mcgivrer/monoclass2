package com.demoing.app.tests.scenes;

import com.demoing.app.core.Application;
import com.demoing.app.core.entity.tilemap.TileMap;
import com.demoing.app.core.io.TileMapLoader;

/**
 * A Test TileMap with some objects preloaded.
 *
 * @author Frédéric Delorme
 * @since 1.0.6
 */
public class TileMapWithObjectsScene extends TestScene {
    public TileMapWithObjectsScene(String name) {
        super(name);
    }

    @Override
    public boolean create(Application app) throws Exception {
        TileMap tileMap = TileMapLoader.load(app, this, "/maps/map_test_03.properties");
        app.addEntity(tileMap);
        return true;
    }
}
