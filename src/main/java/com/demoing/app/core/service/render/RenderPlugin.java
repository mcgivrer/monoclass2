package com.demoing.app.core.service.render;

import com.demoing.app.core.entity.Entity;

import java.awt.*;

/**
 * Interface of the render plugin.
 *
 * @author Frédéric Delorme
 * @since 1.0.6
 **/
public interface RenderPlugin<T extends Entity> {

    Class<T> getRegisteringClass();

    default void draw(Renderer r, Graphics2D g, T ee) {
        g.setColor(ee.color);
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
