package com.demoing.app.core.io;

import com.demoing.app.core.Application;
import com.demoing.app.core.behavior.Behavior;
import com.demoing.app.core.entity.Entity;
import com.demoing.app.core.entity.EntityType;
import com.demoing.app.core.entity.Tile;
import com.demoing.app.core.entity.TileMap;
import com.demoing.app.core.scene.Scene;
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

/**
 * The TileMapLoader implementation load a TileMap from a properties file, and generate
 * <ul>
 *     <li>Preloading of image resources</li>
 *     <li>Tile to create the level map</li>
 *     <li>read an image to be displayed as background,</li>
 *     <li>Entity according to the definition ad position in the TileMap, with their own material, behaviors and attributes,</li>
 * </ul>
 * TODO
 * <ul>
 *     <li>Next to come is animations loading.</li>
 * </ul>
 *
 * @author Frédéric Delorme
 * @since 1.0.6
 */
public class TileMapLoader {
    private static int entityIndex = 0;

    /**
     * Load the game play level definition map from a properties file mapFilepath and create all the required resources:
     * <ul>
     *     <li>{@link Tile}</li>
     *     <li>{@link Entity}</li>
     * </ul>
     *  and obviously generate as output a {@link TileMap} entity.
     *
     * @param app         the parent {@link Application} hosting the Scene
     * @param scn         the parent {@link Scene} where the {@link TileMap} will be attached to
     * @param mapFilepath the file path to the map properties file.
     * @return an instance of a {@link TileMap} with all defined Entities, Tiles and resources to display and play the level.
     */
    public static TileMap load(Application app, Scene scn, String mapFilepath) {
        TileMap tm;
        Properties tmProps = new Properties();
        InputStream is = TileMapLoader.class.getResourceAsStream(mapFilepath);
        try {

            tmProps.load(is);

            Map<Object, Object> resources = retrieveResources(tmProps);

            readBackGroundFromFile(app, tmProps, resources);

            tm = readMapFromStringList(tmProps);

            readAllTiles(tm, tmProps, resources);

            Collection<Entity> entities = createEntitiesAsChild(scn, tm, tmProps, resources);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return tm;
    }

    /**
     * Parse tiles from properties and generate corresponding Tile instances.
     *
     * @param tm        the TileMap instance to be enhanced with loaded tiles.
     * @param tmProps   the properties object populated by the properties file.
     * @param resources the list of already loaded resources to be used to populate Tile's images with.
     */
    private static void readAllTiles(TileMap tm, Properties tmProps, Map<Object, Object> resources) {

        Map<Integer, Map<String, Object>> mapEntities = new ConcurrentHashMap<>();

        List<Object> tilesDefinitions = tmProps.keySet().stream()
                .filter(k -> k.toString().contains("level.tiles")).toList();

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


    /**
     * Read the Background image and convert it as Specific Entity of type IMAGE with no physic.
     * This Entity is added automatically to the app entity list.
     *
     * @param app       the parent Application to be populated with this Background image Entity object.
     * @param tmProps   the Properties instance from the properties file.
     * @param resources the Map of previously declared resources.
     */
    private static void readBackGroundFromFile(Application app, Properties tmProps, Map<Object, Object> resources) {
        if (tmProps.containsKey("level.map.background")) {
            int backGroundResourceId = Integer.parseInt(tmProps.getProperty("level.map.background").strip());
            Entity background = new Entity("background")
                    .setType(EntityType.IMAGE)
                    .setPhysicType(PhysicType.NONE)
                    .setLayer(4)
                    .setPriority(1)
                    .setPosition(0.0, 0.0)
                    .setStickToCamera(true)
                    .setImage(Resources.loadImage((String) resources.get(backGroundResourceId)));
            //app.addEntity(background);
            System.out.printf(
                    "INFO : TileMapLoader | added a Background image entity named %s with resource %s%n",
                    background.name,
                    (String) resources.get(backGroundResourceId));
        }
    }

    /**
     * Convert all the entities declared in the properties file to real {@link Entity} instances and add them to
     * the {@link TileMap} instance as child object.
     *
     * @param scn       the parent Scene
     * @param tm        the TileMap instance to be enhanced with those {@link Entity}'s.
     * @param tmProps   the Properties instance from the properties file.
     * @param resources the Map of previously declared resources.
     * @return A collection of {@link Entity}
     */
    private static Collection<Entity> createEntitiesAsChild(Scene scn, TileMap tm, Properties tmProps, Map<Object, Object> resources) {
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
                    Entity t = populateEntityWithAttr(scn, mapEntities.get(tileId),
                            ix * tileWidth, iy * tileHeight,
                            resources);
                    // reset the tile id to
                    tm.map[ix + (iy * mapWidth)] = 0;
                    entities.add(t);
                    System.out.printf("INFO : TileMapLoader | added and Entity %s named %s%n", t.getClass(), t.name);
                    tm.getChild().add(t);
                }
            }
        }
        return entities;
    }

    /**
     * Read all Map entities from properties file and convert to Map entries for ease of parsing.
     *
     * @param tm      the parent IleMap object to be populated with
     * @param tmProps the Properties instance from the map description properties file
     * @return a Map of Map of entities definition.
     */
    private static Map<Integer, Map<String, Object>> readMapEntities(TileMap tm, Properties tmProps) {
        List<Object> objects = tmProps.keySet().stream().filter(k -> k.toString().contains("level.objects")).toList();
        Map<Integer, Map<String, Object>> mapEntities = new ConcurrentHashMap<>();
        for (Object o : objects) {
            int key = Integer.parseInt(o.toString().substring("level.objects.".length()));
            mapEntities.put(key, collectObjectAttributes((String) tmProps.get(o.toString())));
        }
        tm.addEntities(mapEntities);
        return mapEntities;
    }

    /**
     * <p>Convert all attributes from a properties file entity description to an Entity instance.</p>
     * <p>The class of the {@link Entity} child is defined in the description itself.</p>
     *
     * <p>Please find bellow a complete description of an entity description</p>
     *
     * <pre>
     * <code>level.objects.9=name:player;\                    => entity Name
     *   class:com.demoing.app.core.entity.Entity;\  => Implementation Class
     *   image:[resource=1,x=144,y=64,w=16,h=16];\   => image to be extracted from resource 1
     *   attributes:[\                               => list of attributes
     *     type=IMAGE,\                              => Entity is of type Image
     *     physic_type=DYNAMIC,\                     => need a Physic engine type DYNAMIC
     *     layer=1,\                                 => define display layer
     *     priority=1,\                              => define display priority in the layer
     *     mass=100,\                                => define a Physic Engine mass property
     *     collide=true,\                            => define a property to declare in collision Detection
     *     lives=5,\                                 => a -free attribute- with a number of 5 lives
     *     energy=100,\                              => a -free attribute- to set energy to 100
     *     mana=100,\                                => a -free attribute- to set mana to 100
     *     ];\
     *   material:[\                                 => define material attributes
     *     name=enemy,\
     *     elasticity=0.30,\
     *     density=1.0,\
     *     friction=0.998];
     *   behaviors:[\                                => define a list of Behaviors to be attached to.
     *     com.demoing.app.demo.scenes.behaviors.PlayerOnCollisionBehavior,\
     *   ]</code>
     * </pre>
     *
     * <blockquote>NOTE: A <code>-free attribute-</code> is typed converted and added to the {@link Entity#attributes}
     * map.</blockquote>
     *
     * @param scn        the parent {@link Scene} instance hosting this {@link TileMap}.
     * @param attributes the entity attributes list.
     * @param x          horizontal position of this Entity.
     * @param y          vertical position of this entity.
     * @param resources  list of already defined resources.
     * @return a new instance of an {@link Entity} or one of its inherited child. the class of that entity is defines
     * in the class attribute.
     */
    private static Entity populateEntityWithAttr(Scene scn, Map<String, Object> attributes, double x, double y, Map<Object, Object> resources) {
        String entityName = ((String) attributes.get("name")).replace("$ID", "" + (entityIndex++));
        String entityClassName = ((String) attributes.get("class"));
        Entity obj = null;
        try {
            Class<?> entityClass = Class.forName(entityClassName);
            obj = (Entity) entityClass.getDeclaredConstructor(String.class).newInstance(entityName);
            obj.setPosition(x, y);

            if (attributes.containsKey("image")) {
                generateImageEntityAttribute(attributes, resources, obj);
            }

            // set the GameObject Material based on the material attribute
            if (attributes.containsKey("material")) {
                generateMaterialEntityAttribute(attributes, obj);
            }
            // Set the Behaviors fot this entity.
            if (attributes.containsKey("behaviors")) {
                generateEntityBehaviorsImplementations(scn, attributes, obj);
            }

            if (attributes.containsKey("animations")) {
                generateAnimationsForEntity(scn, attributes, obj);
            }
            // all letting attributes are moved to the Entity attributes itself
            retrieveAllOtherEntityAttributes(attributes, obj);

        } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException |
                 InvocationTargetException e) {
            throw new RuntimeException(e);
        }
        return obj;
    }

    /**
     * Parse the "animations" and "defaultAnimation" attributes to populate Entity with animation frames.
     *
     * <pre>
     *   animations:{\
     *     idle={x=0,y=0,tw=32,th=32,time=[450, 60, 60, 250, 60, 60, 60, 450, 60, 60, 60, 250, 60],resource=3},\
     *     walk={x=0,y=32,tw=32,th=32,time=[60, 60, 60, 150, 60, 60, 60, 150],resource=3},\
     *     jump={x=0,y=160,tw=32,th=32,time=[60, 60, 250, 250, 60, 60],resource=3},\
     *     dead={x=0,y=224,tw=32,th=32,time=[160, 160, 160, 160, 160, 160, 500],resource=3}};\
     *   defaultAnimation:idle
     * </pre>
     *
     * @param scn        the parent {@link Scene} instance hosting this {@link TileMap}.
     * @param attributes the entity "animations" attribute.
     * @param obj        the Entity to be updated with those attribute values.
     */
    private static void generateAnimationsForEntity(Scene scn, Map<String, Object> attributes, Entity obj) {
        // TODO read animation from attribute list and create Animation instances in obj.

    }


    /**
     * Populate all the attributes from the entity definition attributes map and convert if needed to a Typed value.
     * <pre>
     *     attributes:[\
     *       type=IMAGE,\
     *       physic_type=DYNAMIC,\
     *       live=10,\
     *       fire=5,\
     *       hurt=5,\
     *       layer=1,\
     *       priority=2,\
     *       mass=10,\
     *       collide=true];\
     * </pre>
     *
     * @param attributes a map of String/Object to be parsed and interpreted (typed or converted) as obj class attributes
     *                   of free attribute in the {@link Entity#attributes} map.
     * @param obj        the Entity to be updated with those attribute values.
     */
    private static void retrieveAllOtherEntityAttributes(Map<String, Object> attributes, Entity obj) {
        String entityAttributesStr = (String) attributes.get("attributes");
        Map<String, Object> entityAttributes = collectAttributes(
                entityAttributesStr.substring(1, entityAttributesStr.length() - 1),
                ",",
                "=");
        convertMapEntriesToEntityAttributes(obj, entityAttributes);
    }

    /**
     * Convert the "image" attribute entry to a real {@link BufferedImage} from the existing resources.
     *
     * @param attributes the "image" map attributes sub entries
     * @param resources  list of already defined resources.
     * @param obj        the Entity to be updated with those attribute values.
     */
    private static void generateImageEntityAttribute(Map<String, Object> attributes, Map<Object, Object> resources, Entity obj) {
        String imageAttribursStr = (String) attributes.get("image");
        Map<String, Object> imageAttributes = collectAttributes(
                imageAttribursStr.substring(1, imageAttribursStr.length() - 1),
                ",",
                "=");
        BufferedImage img = convertImageAttributeToBufferedImage(resources, imageAttributes);
        obj.setImage(img);
    }

    /**
     * Convert the "material" attribute entry to a real {@link Material} Instance to be added to the obj.
     *
     * <pre>
     *     material:[name=enemy,elasticity=0.30,density=1.0,friction=0.998]
     * </pre>
     *
     * @param attributes the "material" map entries to be converted to an {@link Material} instance.
     * @param obj        the Entity to be updated with those attribute values.
     */
    private static void generateMaterialEntityAttribute(Map<String, Object> attributes, Entity obj) {
        String matAttribursStr = (String) attributes.get("material");
        Map<String, Object> matAttributes = collectAttributes(
                matAttribursStr.substring(1, matAttribursStr.length() - 1),
                ",",
                "=");
        Material material = new Material((String) matAttributes.get("name"),
                (double) matAttributes.get("elasticity"),
                (double) matAttributes.get("density"),
                (double) matAttributes.get("friction"));
        obj.setMaterial(material);

    }

    /**
     * Retrieve all the defined resources into the {@link Properties} instance.
     *
     * <pre>
     * # Resources
     * level.resources.1=/images/tiles01.png
     * level.resources.2=/images/backgrounds/forest.jpg
     * </pre>
     * <p>
     * In the previous sample the '1' and '2' in <code>level.resources.1</code> or <code>level.resources.2</code>
     * will be used as unique resource identifier in the output Map.
     *
     * @param tmProps the {@link Properties} instance form the map properties definition file.
     * @return a Map of Resources with an Integer as id and a String as path to resource.
     */
    private static Map<Object, Object> retrieveResources(Properties tmProps) {
        Map<Object, Object> resources = tmProps.entrySet().stream()
                .filter(e -> e.getKey().toString().contains("level.resources"))
                .collect(Collectors.toMap(
                        x -> Integer.parseInt(((String) x.getKey()).substring("level.resources.".length())),
                        Map.Entry::getValue));
        resources.forEach((key, value) -> System.out.printf("INFO : TileMapLoader | load resource %s as %s%n", key, value));
        if (resources.size() == 0) {
            System.err.printf("ERR : TileMapLoader | No resource has been defined.%n");

        }
        return resources;
    }

    /**
     * Generate a BufferedImage from attributes values :
     * <pre>
     *     image:[resource=1,x=128,y=80,w=16,h=16];
     * </pre>
     *
     * @param resources           list of already defined resources.
     * @param tileImageAttributes map of image attributes
     * @return a BufferedImage
     */
    private static BufferedImage convertImageAttributeToBufferedImage(
            Map<Object, Object> resources,
            Map<String, Object> tileImageAttributes) {
        int resId = (int) tileImageAttributes.get("resource");
        String value = (String) resources.get(resId);
        return Resources.loadImage(value)
                .getSubimage(
                        (int) tileImageAttributes.get("x"),
                        (int) tileImageAttributes.get("y"),
                        (int) tileImageAttributes.get("w"),
                        (int) tileImageAttributes.get("h"));
    }

    /**
     * Convert all attributes value into typed values and assign them to the {@link Entity}.
     *
     * @param obj           the entity to be updated
     * @param objAttributes the list of attributes to parse, convert and set into th Entity.
     */
    private static void convertMapEntriesToEntityAttributes(Entity obj, Map<String, Object> objAttributes) {
        objAttributes.forEach((key1, value1) -> {
            String key = key1.toLowerCase();
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
                    Object value = objAttributes.get(key1);
                    obj.setAttribute(key1, value);
                    break;
            }
        });
    }

    /**
     * Parse entity behavior's definition list from attributes and add them to the obj.
     * <p>
     * In the properties' entity definition, here is a list of Behaviors to be attached to.
     * <pre>
     *     behaviors:[\
     *       com.demoing.app.demo.scenes.behaviors.PlayerOnCollisionBehavior,\
     *     ]
     * </pre>
     *
     * @param scn        the parent {@link Scene} where the object must be added.
     * @param attributes the list of behaviors
     * @param obj        the {@link Entity} to be enhanced with the Behavior's.
     */
    private static void generateEntityBehaviorsImplementations(Scene scn, Map<String, Object> attributes, Entity obj) {
        String behaviorsString = (String) attributes.get("behaviors");
        List<String> behaviorsClass = collectAttributesList(
                behaviorsString.substring(1, behaviorsString.length() - 1),
                ",");
        for (String b : behaviorsClass) {
            try {

                Behavior instanceBehavior = (Behavior) Class.forName(b)
                        .getConstructor(Scene.class).newInstance(scn);
                obj.addBehavior(instanceBehavior);

                System.out.printf("INFO : TileMapLoader | Behavior %s added to GameObject %s%n",
                        b, obj.name);

            } catch (InstantiationException | IllegalAccessException | NoSuchMethodException |
                     ClassNotFoundException | InvocationTargetException e) {
                System.out.printf("ERROR : TileMapLoader | Unable to add behavior %s to current Entity %s%n",
                        b, obj.name);
            }
        }
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

    private static List<String> collectAttributesList(String attrString, String attrSeparator) {
        String[] values = attrString.split(attrSeparator);
        return Arrays.asList(values);
    }

    private static TileMap readMapFromStringList(Properties tmProps) {

        String name = tmProps.getProperty("level.object.name");
        int mapWidth = Integer.parseInt(tmProps.getProperty("level.map.width"));
        int mapHeight = Integer.parseInt(tmProps.getProperty("level.map.height"));
        int tileWidth = Integer.parseInt(tmProps.getProperty("level.map.tile.width"));
        int tileHeight = Integer.parseInt(tmProps.getProperty("level.map.tile.height"));
        String map = tmProps.getProperty("level.map");


        TileMap tm = (TileMap) new TileMap(name)
                .setMapSize(mapWidth, mapHeight)
                .setTileSize(tileWidth, tileHeight)
                .setSize(tileWidth * mapWidth,
                        tileHeight * mapHeight);

        int[] binMap = new int[mapWidth * mapHeight];

        String[] lmap = map.split(",");
        for (int i = 0; i < lmap.length; i++) {
            binMap[i] = Integer.parseInt(lmap[i]);
        }
        tm.map = binMap;

        return tm;
    }

    /**
     * Helper method to read attributes list from a String and convert these to real type data with their name and convert
     * those values to a {@link Map}.
     *
     * @param attrString     the String containing the mist of attribute formatted
     *                       "[attr1=val1,attr2=123,attr3=120.98,attr4=true,attr5=v(12.34,45.67)]"
     * @param attrSeparator  the characters used to split attributes list (here ",").
     * @param valueSeparator the characters used to split attributeName from attributeValue (here "=").
     * @return a Map of String / Object.
     * @see TileMapLoader#convertAttributeValue(String)
     */
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


    /**
     * Convert a String value to a Typed one. It can convert to type Integer, Double, Boolean and String.
     * <blockquote>Boolean values can be valued with <code>true,false,True,False,TRUE,FALSE</code>.</blockquote>
     *
     * @param attrValue the String value to be typed to a known one (Integer, Double, Boolean or String)
     * @return the converted typed value.
     */
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
                    if ("truefalseTrueFalseTRUEFALSE".contains(attrValue)) {
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
}
