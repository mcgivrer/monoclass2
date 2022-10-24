package com.demoing.app.tests.scenes;

import com.demoing.app.core.Application;
import com.demoing.app.core.entity.TileMap;
import com.demoing.app.core.io.TileMapLoader;

/**
 * A Test TileMap with some objects preloaded.
 *
 * @author Frédéric Delorme
 * @since 1.0.6
 */
public class TileMapWithAnimations extends TestScene {
    public TileMapWithAnimations(String name) {
        super(name);
    }

    @Override
    public boolean create(Application app) throws Exception {
        TileMap tileMap = TileMapLoader.load(app, this, "/maps/map_test_04.properties");
        app.addEntity(tileMap);
        return true;
    }
}
