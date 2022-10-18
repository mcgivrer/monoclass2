package com.demoing.app.core.service.render.plugins;

import com.demoing.app.core.entity.ValueEntity;
import com.demoing.app.core.service.render.Renderer;
import com.demoing.app.core.service.render.RenderPlugin;

import java.awt.*;
import java.nio.charset.StandardCharsets;

/**
 * Implementation of the ValueEntity render plugin.
 *
 * @author Frédéric Delorme
 * @since 1.0.6
 **/
public class ValueRenderPlugin implements RenderPlugin<ValueEntity> {
    @Override
    public Class<ValueEntity> getRegisteringClass() {
        return ValueEntity.class;
    }

    @Override
    public void draw(Renderer r, Graphics2D g, ValueEntity se) {
        String textValue = se.valueTxt.strip();
        byte c[] = textValue.getBytes(StandardCharsets.US_ASCII);
        for (int pos = 0; pos < textValue.length(); pos++) {
            //convert character ascii value to number from 0 to 9.
            int v = c[pos] - 48;
            drawFig(g, se, v, se.pos.x + (pos * 8), se.pos.y);
        }
    }

    /**
     * Draw a simple figure
     *
     * @param g     the Graphics2D API
     * @param value number value to draw
     * @param x     horizontal position
     * @param y     vertical position
     */
    private void drawFig(Graphics2D g, ValueEntity se, int value, double x, double y) {
        assert (value > -1);
        assert (value < 10);
        g.drawImage(se.figures[value], (int) x, (int) y, null);
    }
}
