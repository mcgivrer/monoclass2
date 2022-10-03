package com.demoing.app.core.io;

import com.demoing.app.core.Application;
import com.demoing.app.core.entity.Entity;
import com.demoing.app.core.entity.EntityType;
import com.demoing.app.core.entity.Tile;
import com.demoing.app.core.entity.TileMap;
import com.demoing.app.core.service.physic.PhysicType;
import com.demoing.app.core.service.physic.material.Material;

import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class TileMapLoader {
    private static int entityIndex = 0;

    public static TileMap load(Application app, String mapFilepath) {
        TileMap tm;
        Properties tmProps = new Properties();
        InputStream is = TileMapLoader.class.getResourceAsStream(mapFilepath);
        try {

            tmProps.load(is);

            Map<Object, Object> resources = retrieveResources(tmProps);

            readBackGroundFromFile(app, tmProps, resources);

            tm = readMapFromStringList(tmProps);

            readAllTiles(tm, tmProps, resources);

            Collection<Entity> entities = createEntitiesAsChild(tm, tmProps, resources);

            System.out.printf("mapEntities size:%d", entities.size());

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return tm;
    }

    private static void readAllTiles(TileMap tm, Properties tmProps, Map<Object, Object> resources) {

        Map<Integer, Map<String, Object>> mapEntities = new ConcurrentHashMap<>();

        List<Object> tilesDefinitions = tmProps.keySet().stream()
                .filter(k -> k.toString().contains("level.tiles"))
                .collect(Collectors.toList());

        Map<Integer, Tile> tiles = new HashMap<>();
        for (Object o : tilesDefinitions) {
            int key = Integer.parseInt(o.toString().substring("level.tiles.".length()));

            Map<String, Object> attributes = collectAttributes((String) tmProps.get(o.toString()), ";", ":");

            Map<String, Object> tileImageAttributes = (Map<String, Object>) attributes.get("image");
            attributes.put("image", tileImageAttributes);
            BufferedImage tilesImgSrc = convertImageAttributeToBufferedImage(resources, tileImageAttributes);
            Tile tile = new Tile(key, tilesImgSrc, attributes);
            System.out.printf("INFO : TileMapLoader | Tile id %d with %s has been added .%n", tile.id, tile.attributes);

            tiles.put(key, tile);
        }
        if (tiles.size() == 0) {
            System.out.printf("ERR : TileMapLoader | no tiles has been defined.%n");

        }
        tm.setAttribute("level.tiles", tiles);
    }

    private static Map<String, Object> collectAttributes(String attrString, String attrSeparator,
                                                         String valueSeparator) {
        Map<String, Object> attributes = new HashMap<>();
        String[] attrs = attrString.split(attrSeparator);
        Arrays.stream(attrs).toList().forEach(s -> {
            String[] attrValues = s.split(valueSeparator);
            attributes.put(attrValues[0], convertAttributeValue(attrValues[1].strip()));
        });
        return attributes;
    }


    private static Object convertAttributeValue(String attrValue) {
        Object value = null;
        if (attrValue.startsWith("[") && attrValue.endsWith("]")) {
            // attribute.
            value = collectAttributes(attrValue.substring(1, attrValue.length() - 1), ",", "=");
            return value;
        } else {
            try {
                // Try to convert to integer (0-~)
                value = Integer.parseInt(attrValue);
                return value;
            } catch (NumberFormatException intE) {
                try {
                    // try to convert to Double (0.0)
                    value = Double.parseDouble(attrValue);
                    return value;
                } catch (NumberFormatException doubleE) {
                    if ("true,True,False,false,TRUE,FALSE".contains(attrValue)) {
                        // try to convert to boolean (true/false)
                        value = Boolean.parseBoolean(attrValue);
                        return value;
                    } else {
                        value = attrValue;
                        return value;
                    }
                }
            }
        }
    }

    private static void readBackGroundFromFile(Application app, Properties tmProps, Map<Object, Object> resources) {
        if (tmProps.containsKey("level.map.background")) {
            int backGroundResourceId = Integer.parseInt(tmProps.getProperty("level.map.background").strip());
            Entity background = new Entity("background")
                    .setType(EntityType.IMAGE)
                    .setPhysicType(PhysicType.NONE)
                    .setLayer(2)
                    .setPriority(10)
                    .setPosition(0.0, 0.0)
                    .setStickToCamera(true)
                    .setImage(Resources.loadImage((String) resources.get(backGroundResourceId)));
            app.addEntity(background);
        }
    }

    private static Collection<Entity> createEntitiesAsChild(TileMap tm, Properties tmProps, Map<Object, Object> resources) {
        Collection<Entity> entities = new ArrayList<>();
        Map<Integer, Map<String, Object>> mapEntities = readMapEntities(tm, tmProps);

        int mapWidth = Integer.parseInt(tmProps.getProperty("level.map.width"));
        int mapHeight = Integer.parseInt(tmProps.getProperty("level.map.height"));
        int tileWidth = Integer.parseInt(tmProps.getProperty("level.map.tile.width"));
        int tileHeight = Integer.parseInt(tmProps.getProperty("level.map.tile.height"));

        for (int iy = 0; iy < mapHeight; iy++) {
            for (int ix = 0; ix < mapWidth; ix++) {
                int tileId = tm.map[ix + (iy * mapWidth)];
                if (Optional.ofNullable(mapEntities.get(tileId)).isPresent()) {
                    Entity t = populateEntityWithAttr(mapEntities.get(tileId),
                            ix * tileWidth, iy * tileHeight,
                            resources);
                    entities.add(t);
                    tm.getChild().add(t);
                }
            }
        }
        return entities;
    }

    private static Entity populateEntityWithAttr(Map<String, Object> attributes, double x, double y, Map<Object, Object> resources) {
        String entityName = ((String) attributes.get("name")).replace("#", "" + (entityIndex++));
        String entityClassName = ((String) attributes.get("class"));
        Entity obj = null;
        try {
            Class<?> entityClass = Class.forName(entityClassName);
            obj = (Entity) entityClass.getDeclaredConstructor(String.class).newInstance(entityName);
            obj.setPosition(x, y);

            if (attributes.containsKey("image")) {
                Map<String, Object> imageAttributes = (Map<String, Object>) attributes.get("image");
                BufferedImage img = convertImageAttributeToBufferedImage(resources, imageAttributes);
                obj.setImage(img);
                obj.setSize(img.getWidth(), img.getHeight());
            }

            // set the GameObject Material based on the material attribute
            if (attributes.containsKey("material")) {
                Map<String, Object> matAttributes = (Map<String, Object>) attributes.get("material");
                Material material = new Material((String) matAttributes.get("name"),
                        (double) matAttributes.get("elasticity"),
                        (double) matAttributes.get("density"),
                        (double) matAttributes.get("friction"));
                obj.setMaterial(material);
            }

            convertMapEntriesToEntityAttributes(obj, attributes);

        } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException |
                 InvocationTargetException e) {
            throw new RuntimeException(e);
        }
        return obj;
    }

    private static Map<Object, Object> retrieveResources(Properties tmProps) {
        Map<Object, Object> resources = tmProps.entrySet().stream()
                .filter(e -> e.getKey().toString().contains("level.resources"))
                .collect(Collectors.toMap(
                        x -> Integer.parseInt(((String) x.getKey()).substring("level.resources.".length())),
                        x -> x.getValue()));
        resources.entrySet().forEach(
                e -> System.out.printf("INFO : TileMapLoader | load resource %s as %s%n", e.getKey(), e.getValue()));
        if (resources.size() == 0) {
            System.err.printf("ERR : TileMapLoader | No resource has been defined.%n");

        }
        return resources;
    }

    private static BufferedImage convertImageAttributeToBufferedImage(
            Map<Object, Object> resources,
            Map<String, Object> tileImageAttributes) {
        int resId = (int) tileImageAttributes.get("resource");
        String value = (String) resources.get(resId);
        BufferedImage tilesImgSrc = Resources.loadImage(value)
                .getSubimage(
                        (int) tileImageAttributes.get("x"),
                        (int) tileImageAttributes.get("y"),
                        (int) tileImageAttributes.get("w"),
                        (int) tileImageAttributes.get("h"));
        return tilesImgSrc;
    }

    private static void convertMapEntriesToEntityAttributes(Entity obj, Map<String, Object> objAttributes) {
        objAttributes.entrySet().forEach(e -> {
            String key = (String) e.getKey().toLowerCase();
            switch (key) {
                case "layer":
                    int layer = (int) objAttributes.get("layer");
                    obj.setLayer(layer);
                    break;
                case "priority":
                    int priority = (int) objAttributes.get("priority");
                    obj.setPriority(priority);
                    break;
                case "size":
                    String[] sizeStr = ((String) objAttributes.get("size")).split("x");
                    double objWidth = Double.parseDouble(sizeStr[0]);
                    double objHeight = Double.parseDouble(sizeStr[1]);
                    obj.setSize(objWidth, objHeight);
                    break;
                case "type":
                    String objType = (String) objAttributes.get("type");
                    obj.setType(EntityType.valueOf((objType)));
                    break;
                case "physic_type":
                    String objPhysicType = (String) objAttributes.get("physic_type");
                    obj.setPhysicType(PhysicType.valueOf((objPhysicType)));
                    break;
                case "box":
                    String boxType = (String) objAttributes.get("box");
                    obj.cbox = new Ellipse2D.Double(obj.pos.x, obj.pos.y, obj.width, obj.height);
                default:
                    Object value = objAttributes.get(e.getKey());
                    obj.setAttribute(e.getKey(), value);
                    break;
            }
        });
    }

    private static Map<Integer, Map<String, Object>> readMapEntities(TileMap tm, Properties tmProps) {
        List<Object> objects = tmProps.keySet().stream().filter(k -> k.toString().contains("level.objects")).collect(Collectors.toList());
        Map<Integer, Map<String, Object>> mapEntities = new ConcurrentHashMap<>();
        for (Object o : objects) {
            int key = Integer.parseInt(o.toString().substring("level.objects.".length()));
            mapEntities.put(key, collectObjectAttributes((String) tmProps.get(o.toString())));
        }
        tm.addEntities(mapEntities);
        return mapEntities;
    }

    private static Map<String, Object> collectObjectAttributes(String attrString) {
        Map<String, Object> attributes = new HashMap<>();
        String[] attrs = attrString.split(";");
        Arrays.stream(attrs).toList().forEach(s -> {
            String[] attrValues = s.split(":");
            attributes.put(attrValues[0], attrValues[1]);
        });
        return attributes;
    }

    private static TileMap readMapFromStringList(Properties tmProps) {

        String name = tmProps.getProperty("level.object.name");
        int mapWidth = Integer.parseInt(tmProps.getProperty("level.map.width"));
        int mapHeight = Integer.parseInt(tmProps.getProperty("level.map.height"));
        int tileWidth = Integer.parseInt(tmProps.getProperty("level.map.tile.width"));
        int tileHeight = Integer.parseInt(tmProps.getProperty("level.map.tile.height"));
        String map = tmProps.getProperty("level.map");


        TileMap tm = new TileMap(name)
                .setMapSize(mapWidth, mapHeight)
                .setTileSize(tileWidth, tileHeight);

        int[] binMap = new int[mapWidth * mapHeight];

        String[] lmap = map.split(",");
        for (int i = 0; i < lmap.length; i++) {
            binMap[i] = Integer.parseInt(lmap[i]);
        }
        tm.map = binMap;

        return tm;
    }
}
