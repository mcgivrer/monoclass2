package com.demoing.app.core.io;

import com.demoing.app.core.entity.TileMap;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class TileMapLoader {
    public static TileMap load(String mapFilepath) {
        TileMap tm;
        Properties tmProps = new Properties();
        InputStream is = TileMapLoader.class.getResourceAsStream(mapFilepath);
        try {

            tmProps.load(is);
            String code = tmProps.getProperty("level.code");
            String name = tmProps.getProperty("level.name");
            String description = tmProps.getProperty("level.description");
            String map = tmProps.getProperty("level.map");
            int mapWidth = Integer.parseInt(tmProps.getProperty("level.map.width"));
            int mapHeight = Integer.parseInt(tmProps.getProperty("level.map.height"));
            int tileWidth = Integer.parseInt(tmProps.getProperty("level.map.tile.width"));
            int tileHeight = Integer.parseInt(tmProps.getProperty("level.map.tile.height"));

            tm = new TileMap(name)
                    .setMapSize(mapWidth, mapHeight)
                    .setTileSize(tileWidth, tileHeight);
            readMapFromStringList(tm, map, mapWidth, mapHeight);

            List<Object> objects = tmProps.keySet().stream().filter(k -> k.toString().contains("level.objects")).collect(Collectors.toList());
            Map<Integer, String> mapEntities = new ConcurrentHashMap<>();
            objects.forEach(o -> {
                int key = Integer.parseInt(o.toString().substring("level.objects.".length()));
                mapEntities.put(key, tmProps.getProperty(o.toString()));
            });
            System.out.printf("mapEntities size:%d", mapEntities.size());

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return tm;
    }

    private static void readMapFromStringList(TileMap tm, String map, int mapWidth, int mapHeight) {
        int[] binMap = new int[mapWidth * mapHeight];

        String[] lmap = map.split(",");
        for (int i = 0; i < lmap.length; i++) {
            binMap[i] = Integer.parseInt(lmap[i]);
        }
        tm.map = binMap;
    }
}
