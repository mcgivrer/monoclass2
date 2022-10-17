package com.demoing.app.core.service.render.plugins;

import com.demoing.app.core.entity.Influencer;
import com.demoing.app.core.service.render.Renderer;
import com.demoing.app.core.service.render.RenderPlugin;

import java.awt.*;

/**
 * Implementation of the Influencer render plugin.
 *
 * @author Frédéric Delorme
 * @since 1.0.6
 **/
public class InfluencerRenderPlugin implements RenderPlugin<Influencer> {
    @Override
    public Class<Influencer> getRegisteringClass() {
        return Influencer.class;
    }

    @Override
    public void draw(Renderer r, Graphics2D g, Influencer entity) {
        RenderPlugin.super.draw(r, g, entity);
    }
}
