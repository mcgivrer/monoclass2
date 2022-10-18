package com.demoing.app.tests.scenes;

import com.demoing.app.core.Application;
import com.demoing.app.core.entity.TileMap;
import com.demoing.app.core.io.TileMapLoader;

public class TileMapScene extends TestScene {
    public TileMapScene(String name) {
        super(name);
    }

    @Override
    public boolean create(Application app) throws Exception {
        TileMap tileMap = TileMapLoader.load(app, this, "/maps/map_test.properties");
        app.addEntity(tileMap);
        return true;
    }
}
