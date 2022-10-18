package com.demoing.app.core.service.render.plugins;

import com.demoing.app.core.entity.Entity;
import com.demoing.app.core.service.render.Renderer;
import com.demoing.app.core.service.render.RenderPlugin;

import java.awt.*;

/**
 * Implementation of the Entity render plugin.
 *
 * @author Frédéric Delorme
 * @since 1.0.6
 **/
public class EntityRenderPlugin implements RenderPlugin<Entity> {
    @Override
    public Class<Entity> getRegisteringClass() {
        return Entity.class;
    }

    @Override
    public void draw(Renderer r, Graphics2D g, Entity ee) {
        switch (ee.type) {
            case RECTANGLE -> g.fillRect((int) ee.pos.x, (int) ee.pos.y, (int) ee.width, (int) ee.height);
            case ELLIPSE -> g.fillArc((int) ee.pos.x, (int) ee.pos.y, (int) ee.width, (int) ee.height, 0, 360);
            case IMAGE -> {
                if (ee.getDirection() > 0) {
                    g.drawImage(
                            ee.getImage(),
                            (int) ee.pos.x, (int) ee.pos.y,
                            null);
                } else {
                    g.drawImage(
                            ee.getImage(),
                            (int) (ee.pos.x + ee.width), (int) ee.pos.y,
                            (int) (-ee.width), (int) ee.height,
                            null);
                }
            }
            case NONE -> {
            }
        }
    }
}
