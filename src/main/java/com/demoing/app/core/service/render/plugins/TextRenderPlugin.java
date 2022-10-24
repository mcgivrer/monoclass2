package com.demoing.app.core.service.render.plugins;

import com.demoing.app.core.entity.TextAlign;
import com.demoing.app.core.entity.TextEntity;
import com.demoing.app.core.service.render.Renderer;
import com.demoing.app.core.service.render.RenderPlugin;

import java.awt.*;
import java.util.Optional;

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

        drawText(r, g, te);
        /*g.setFont(te.font);
        int size = g.getFontMetrics().stringWidth(te.text);
        double offsetX = te.align.equals(TextAlign.RIGHT) ? -size
                : te.align.equals(TextAlign.CENTER) ? -size * 0.5 : 0;

        if (te.shadowColor != null) {
            g.setColor(te.shadowColor);
            for (int dx = 0; dx < te.shadowOffsetX; dx++) {
                for (int dy = 0; dy < te.shadowOffsetX; dy++) {
                    g.drawString(te.text, (int) (te.pos.x + offsetX) + dx+1, (int) te.pos.y + dy+1);
                }
            }
        }
        g.setColor(te.color);
        g.drawString(te.text, (int) (te.pos.x + offsetX), (int) te.pos.y);
        te.width = size;
        te.height = g.getFontMetrics().getHeight();
        te.box.setRect(te.pos.x + offsetX, te.pos.y - te.height + g.getFontMetrics().getDescent(), te.width,
                te.height);

         */
    }

    public void drawText(Renderer r, Graphics2D g, TextEntity txt) {
        if (Optional.ofNullable(txt.font).isPresent()) {
            int x = (int) txt.pos.x;
            int y = (int) txt.pos.y;

            g.setFont(txt.font);
            int dx = 0;

            // detect longest line
            String[] lines = txt.text.split("\n");
            int textWidth = 0;
            for (String line : lines) {
                textWidth = Math.max(textWidth, g.getFontMetrics().stringWidth(line));
            }
            int textHeight = g.getFontMetrics().getHeight();
            int paragraphHeight = textHeight * lines.length;

            // define graphical alignment
            switch (txt.align) {
                case LEFT -> dx = 0;
                case RIGHT -> dx = -textWidth;
                case CENTER -> dx = (int) (-(textWidth * 0.5));
            }
            //draw background is required
            if (Optional.ofNullable(txt.backGroundColor).isPresent()) {
                // fill background
                r.drawRectangleObject(g,
                        txt.backGroundColor,
                        txt.color,
                        (int) (x + dx - txt.shadowOffsetX),
                        (int) (y - ((paragraphHeight * 0.5) + txt.shadowOffsetX)),
                        (int) (textWidth + (2 * txt.shadowOffsetX)),
                        (int) ((paragraphHeight) + (2 * txt.shadowOffsetY)));
            }
            // draw text to display
            int lineId = 0;
            for (String textLine : lines) {
                // draw shadow if required
                if (Optional.ofNullable(txt.shadowColor).isPresent() && (txt.shadowOffsetX != 0 || txt.shadowOffsetY != 0)) {
                    r.drawTextShadow(g, textLine, x + dx, y + (lineId * textHeight), txt.shadowOffsetX, txt.shadowColor);
                }
                g.setColor(txt.color);
                g.drawString(textLine, x + dx, y + (lineId * textHeight));
                lineId++;
            }
        }
    }
}
