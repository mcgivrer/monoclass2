package com.demoing.app.core.service.render.plugins;

import com.demoing.app.core.entity.MapEntity;
import com.demoing.app.core.service.render.Renderer;
import com.demoing.app.core.service.render.RenderPlugin;

import java.awt.Graphics2D;
import java.awt.Color;

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
        g.setColor(me.color);
        g.drawRect((int) me.pos.x, (int) me.pos.y, (int) me.width, (int) me.height);
        g.setColor(me.backgroundColor);
        g.fillRect((int) me.pos.x, (int) me.pos.y, (int) me.width, (int) me.height);
        me.entitiesRef.stream()
                .filter(e -> (e.isAlive() || e.isPersistent()))
                .forEach(e -> {
                    me.colorEntityMapping.entrySet().forEach(cm -> {
                        if (e.name.contains(cm.getKey())) {
                            g.setColor(cm.getValue());
                            int px = (int) (me.pos.x + (me.width * (e.pos.x / me.world.area.getWidth())));
                            int py = (int) (me.pos.y + me.height * (e.pos.y / me.world.area.getHeight()));
                            int pw = (int) (me.width * (e.width / me.world.area.getWidth()));
                            int ph = (int) (me.height * (e.height / me.world.area.getHeight()));
                            g.drawRect(px, py, pw, ph);
                        } else {
                            g.setColor(Color.GRAY);
                            int px = (int) (me.pos.x + (me.width * (e.pos.x / me.world.area.getWidth())));
                            int py = (int) (me.pos.y + me.height * (e.pos.y / me.world.area.getHeight()));
                            int pw = (int) (me.width * (e.width / me.world.area.getWidth()));
                            int ph = (int) (me.height * (e.height / me.world.area.getHeight()));
                            g.drawRect(px, py, pw, ph);
                        }
                    });
                });
    }
}