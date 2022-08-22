package com.demoing.app.core.io;

import com.demoing.app.core.entity.TileMap;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

public class TileMapLoader {
    public static TileMap load(String mapFilepath) {
        Properties tmProps = new Properties();
        InputStream is = TileMapLoader.class.getResourceAsStream(mapFilepath);
        try {

            tmProps.load(is);
            String code = tmProps.getProperty("level.code");
            String name = tmProps.getProperty("level.name");
            String description = tmProps.getProperty("level.description");
            String map = tmProps.getProperty("level.map");
            List<Object> objects = tmProps.keySet().stream().filter(k -> k.toString().contains("level.objects")).collect(Collectors.toList());
            objects.forEach(o -> {
                String key = o.toString();
                System.out.printf("objects: %s%n", key);
            });

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return new TileMap("");
    }
}
