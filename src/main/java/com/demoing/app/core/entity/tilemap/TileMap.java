package com.demoing.app.core.entity.tilemap;

import com.demoing.app.core.entity.Entity;
import com.demoing.app.core.service.physic.PhysicType;

import java.awt.*;
import java.util.Map;

/**
 * Create a new TileMap corresponding to the new decor based on tiles.
 *
 * @author Frédéric Delorme
 * @since 1.0.6
 */
public class TileMap extends Entity {
    /**
     * internal color mapping for Tile rendering
     */
    public final Map<Integer, Color> tilesColor = Map.of(
            0, Color.BLACK,
            1, Color.GREEN,
            2, Color.DARK_GRAY,
            3, Color.GRAY,
            4, Color.CYAN,
            5, Color.YELLOW,
            6, Color.RED,
            7, Color.BLUE
    );
    /**
     * width of a tile in this map
     */
    public int tileWidth;
    /**
     * height of a tile in this map
     */

    public int tileHeight;
    /**
     * flag to track drawing of this TileMap entity
     */
    public boolean drawn;
    /**
     * Number of tiles drawn during rendering process, for statistics and tests purpose.
     */
    public int tileDrawnCounter;
    /**
     * width of this map
     */

    public int mapWidth;
    /**
     * height of this map
     */
    public int mapHeight;
    /**
     * binary data of this TileMap
     */
    public int[] map;
    private Map<Integer, Map<String, Object>> entities;

    /**
     * Create a new {@link TileMap} named <code>tilemapName</code>.
     *
     * @param tilemapName the name of this new TileMap.
     */
    public TileMap(String tilemapName) {
        super(tilemapName);
        this.physicType = PhysicType.NONE;
    }

    /**
     * Set the tile size for this {@link TileMap}.
     *
     * @param tileWidth  width of a tile
     * @param tileHeight height of a tile.
     * @return this updated TileMap.
     */
    public TileMap setTileSize(int tileWidth, int tileHeight) {
        this.tileWidth = tileWidth;
        this.tileHeight = tileHeight;
        return this;
    }

    /**
     * Set the map size for this {@link TileMap}.
     *
     * @param width  width of a tile map
     * @param height height of a tile map.
     * @return this updated TileMap.
     */
    public TileMap setMapSize(int width, int height) {
        this.mapWidth = width;
        this.mapHeight = height;
        this.map = new int[width * height];
        return this;
    }

    /**
     * Return the map array's length.
     *
     * @return the length of the map int array.
     */
    public int getMapLength() {
        return map.length;
    }

    public TileMap addEntities(Map<Integer, Map<String, Object>> entities) {
        this.entities = entities;
        return this;
    }

    public Map<Integer, Map<String, Object>> getEntities() {
        return this.entities;
    }

    public Map<String, Object> getEntity(int entityNb) {
        return this.entities.get(entityNb);
    }

    public Entity getEntity(String name) {
        return this.getChild().stream().filter(e -> e.name.equals(name)).findFirst().orElse(null);
    }
}
