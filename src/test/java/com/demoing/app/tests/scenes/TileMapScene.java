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
        TileMap tileMap = (TileMap) new TileMap("tm_02")
                .setTileSize(16, 16)
                .setMapSize(40, 20)
                .setPriority(1);
        loadMap(tileMap,"maps/map_0_1.properties");
        app.addEntity(tileMap);
        return true;
    }

    private void loadMap(TileMap tileMap, String mapFilepath) {
        tileMap = TileMapLoader.load(mapFilepath);
    }
}
