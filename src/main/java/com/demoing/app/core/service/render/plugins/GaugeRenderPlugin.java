package com.demoing.app.core.service.render.plugins;

import com.demoing.app.core.entity.GaugeEntity;
import com.demoing.app.core.service.render.Renderer;
import com.demoing.app.core.service.render.RenderPlugin;

import java.awt.*;

/**
 * Implementation of the GaugeEntity render plugin.
 *
 * @author Frédéric Delorme
 * @since 1.0.6
 **/
public class GaugeRenderPlugin implements RenderPlugin<GaugeEntity> {
    @Override
    public Class<GaugeEntity> getRegisteringClass() {
        return GaugeEntity.class;
    }

    @Override
    public void draw(Renderer r, Graphics2D g, GaugeEntity ge) {
        g.setColor(ge.shadow);
        g.fillRect((int) ge.pos.x - 1, (int) ge.pos.y - 1, (int) ge.width + 2, (int) ge.height + 2);
        g.setColor(ge.border);
        g.fillRect((int) ge.pos.x, (int) ge.pos.y, (int) ge.width, (int) ge.height);
        int value = (int) ((ge.value / ge.maxValue) * ge.width - 2);
        g.setColor(ge.color);
        g.fillRect((int) (ge.pos.x) + 1, (int) (ge.pos.y) + 1, value, (int) (ge.height) - 2);
    }
}
