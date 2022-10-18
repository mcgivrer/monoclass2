package com.demoing.app.core.entity;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

/**
 * A Tile is a basic graphic element to draw a tilemap.
 *
 * @author Frédéric Delorme
 * @since 1.0.0
 */
public class Tile {
    public int id;
    public BufferedImage image;
    public Map<String, Object> attributes = new HashMap<>();

    /**
     * Create a new Tile with all its attributes.
     *
     * @param id         a unic id for this tile
     * @param image      the corresponding BufferedImage to be drawn
     * @param attributes some attributes usefull to be processed
     *                   by {@link com.demoing.app.core.service.physic.PhysicEngine}
     *                   and {@link com.demoing.app.core.service.collision.CollisionDetector}
     */
    public Tile(int id, BufferedImage image, Map<String, Object> attributes) {
        this.id = id;
        this.image = image;
        this.attributes = attributes;
    }
}
