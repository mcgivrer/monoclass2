package com.demoing.app.core.service.render.plugins;

import com.demoing.app.core.entity.ParticleSystem;
import com.demoing.app.core.service.render.Renderer;
import com.demoing.app.core.service.render.RenderPlugin;

import java.awt.*;


/**
 * Implementation of the ParticleSystem render plugin.
 *
 * @author Frédéric Delorme
 * @since 1.0.6
 **/
public class ParticleSystemRenderPlugin implements RenderPlugin<ParticleSystem> {
    @Override
    public Class<ParticleSystem> getRegisteringClass() {
        return ParticleSystem.class;
    }

    @Override
    public void draw(Renderer r, Graphics2D g, ParticleSystem entity) {

        RenderPlugin.super.draw(r, g, entity);
    }
}
