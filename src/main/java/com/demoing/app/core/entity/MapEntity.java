package com.demoing.app.core.entity;

import com.demoing.app.core.service.render.Render;
import com.demoing.app.core.service.physic.PhysicType;
import com.demoing.app.core.service.physic.World;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>{@link MapEntity} is a displayed map of the all Scene existing active objects.</p>
 * <p>The {@link Render} will display all object according to a defined color code.
 * <pre>
 * MapEntity mapEntity = (MapEntity) new MapEntity("map")
 *   .setColorMapping(
 *     // define color mapping on name entity filtering
 *     Map.of(
 *       "ball_", Color.RED,
 *     "player", Color.BLUE,
 *     "pf_", Color.LIGHT_GRAY,
 *     "floor", Color.GRAY,
 *     "outPlatform", Color.YELLOW))
 *   // define list of entities to be displayed on the map
 *   .setRefEntities(app.entities.values().stream().toList())
 *   // set World reference
 *   .setWorld(app.world)
 *   // define Map display size
 *   .setSize(48, 32)
 *   // define where to display the Map
 *   .setPosition(10, app.config.screenHeight - 48);
 * </pre>
 * </p>
 *
 * @author Frédéric Delorme
 * @see Entity
 * @since 1.0.4
 */
public class MapEntity extends Entity {
    public List<Entity> entitiesRef = new ArrayList<>();
    public Map<String, Color> colorEntityMapping = new HashMap<>();
    public Color backgroundColor = new Color(0.1f, 0.1f, 0.1f, 0.4f);
    public World world;

    public MapEntity(String name) {
        super(name);
        setPhysicType(PhysicType.STATIC);
        setStickToCamera(true);
    }

    public MapEntity setWorld(World w) {
        this.world = w;
        return this;
    }

    public MapEntity setColorMapping(Map<String, Color> mp) {
        this.colorEntityMapping = mp;
        return this;
    }

    public MapEntity setRefEntities(List<Entity> le) {
        this.entitiesRef = le;
        return this;
    }
}
