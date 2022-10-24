package com.demoing.app.core.service.render.plugins;

import com.demoing.app.core.entity.*;
import com.demoing.app.core.service.render.RenderPlugin;
import com.demoing.app.core.service.render.Renderer;

import java.awt.*;

/**
 * Implementation of the MapEntity render plugin.
 *
 * @author Frédéric Delorme
 * @since 1.0.6
 **/
public class MapRenderPlugin implements RenderPlugin<MapEntity> {

    @Override
    public Class<MapEntity> getRegisteringClass() {
        return MapEntity.class;
    }

    @Override
    public void draw(Renderer r, Graphics2D g, MapEntity me) {
        g.setColor(me.backgroundColor);
        g.fillRect((int) me.pos.x, (int) me.pos.y, (int) me.width, (int) me.height);
        me.entitiesRef.stream()
                .filter(e -> (e.isAlive() || e.isPersistent()))
                .forEach(e -> {

                    String colorMapping = me.colorEntityMapping.keySet().stream()
                            .filter(k -> e.name.contains(k)).findFirst()
                            .orElse("default");


                    int pw = (int) (me.width * (e.width / me.world.area.getWidth()));
                    int ph = (int) (me.height * (e.height / me.world.area.getHeight()));
                    g.setColor(me.colorEntityMapping.get(colorMapping));
                    switch (e) {
                        case TileMap tm -> {
                            drawTileMap(g, me, tm, pw, ph);
                        }
                        case TextEntity te -> {
                            // Do nothing !
                        }
                        case ValueEntity te -> {
                            // Do nothing
                        }
                        case GaugeEntity ge -> {
                            // Do nothing
                        }
                        case Light le -> {
                            drawFillBox(g, me, le, pw, ph);
                        }
                        case default -> {
                            drawFillBox(g, me, e, pw, ph);
                        }
                    }
                });
        g.setColor(me.color);
        g.drawRect((int) me.pos.x, (int) me.pos.y, (int) me.width, (int) me.height);
    }

    private void drawFillBox(Graphics2D g, MapEntity me, Entity e, int pw, int ph) {
        int px = (int) (me.pos.x + (me.width * (e.pos.x / me.world.area.getWidth())));
        int py = (int) (me.pos.y + me.height * (e.pos.y / me.world.area.getHeight()));
        g.fillRect(px, py, pw, ph);
    }

    private void drawTileMap(Graphics2D g, MapEntity me, Entity e, int pw, int ph) {
        TileMap tm = (TileMap) e;
        for (int y = 0; y < tm.mapHeight; y++) {
            for (int x = 0; x < tm.mapWidth; x++) {
                int index = x + (y * tm.mapWidth);
                if (tm.map[index] != 0) {
                    int px = (int) (me.pos.x + (me.width * (e.pos.x + (x * tm.tileWidth) / me.world.area.getWidth())));
                    int py = (int) (me.pos.y + (me.height * (e.pos.y + (y * tm.tileHeight) / me.world.area.getHeight())));
                    g.drawRect(px, py, 1, 1);
                }
            }
        }
    }
}
