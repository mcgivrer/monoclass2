package com.demoing.app.core.service.render.plugins;

import com.demoing.app.core.entity.TextAlign;
import com.demoing.app.core.entity.TextEntity;
import com.demoing.app.core.service.render.Renderer;
import com.demoing.app.core.service.render.RenderPlugin;

import java.awt.*;

/**
 * Implementation of the TextEntity render plugin.
 *
 * @author Frédéric Delorme
 * @since 1.0.6
 **/
public class TextRenderPlugin implements RenderPlugin<TextEntity> {
    @Override
    public Class<TextEntity> getRegisteringClass() {
        return TextEntity.class;
    }

    @Override
    public void draw(Renderer r, Graphics2D g, TextEntity te) {
        g.setColor(te.color);
        g.setFont(te.font);
        int size = g.getFontMetrics().stringWidth(te.text);
        double offsetX = te.align.equals(TextAlign.RIGHT) ? -size
                : te.align.equals(TextAlign.CENTER) ? -size * 0.5 : 0;
        g.drawString(te.text, (int) (te.pos.x + offsetX), (int) te.pos.y);
        te.width = size;
        te.height = g.getFontMetrics().getHeight();
        te.box.setRect(te.pos.x + offsetX, te.pos.y - te.height + g.getFontMetrics().getDescent(), te.width,
                te.height);
    }
}
