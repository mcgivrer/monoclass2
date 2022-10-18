package com.demoing.app.core.service.render.plugins;

import com.demoing.app.core.config.Configuration;
import com.demoing.app.core.entity.Camera;
import com.demoing.app.core.entity.Light;
import com.demoing.app.core.service.render.Renderer;
import com.demoing.app.core.service.render.RenderPlugin;

import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

/**
 * @author : M313104
 * @mailto : buy@mail.com
 * @created : 17/10/2022
 **/
public class LightRenderPlugin implements RenderPlugin<Light> {

    private Camera activeCamera;
    private Configuration config;

    @Override
    public Class<Light> getRegisteringClass() {
        return Light.class;
    }

    @Override
    public void draw(Renderer r, Graphics2D g, Light l) {
        activeCamera = r.getActiveCamera();
        config = r.getConfiguration();
        switch (l.lightType) {
            case SPOT -> drawSpotLight(g, l);
            case SPHERICAL -> drawSphericalLight(g, l);
            case AMBIENT -> drawAmbiantLight(g, l);
            case AREA_RECTANGLE -> drawLightArea(g, l);
        }
    }

    private void drawLightArea(Graphics2D g, Light l) {

        final Area ambientArea = new Area(new Rectangle2D.Double(l.pos.x, l.pos.y, l.width, l.height));
        g.setColor(l.color);
        Composite c = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) l.energy));
        g.fill(ambientArea);
        g.setComposite(c);
    }

    private void drawAmbiantLight(Graphics2D g, Light l) {
        final Area ambientArea = new Area(
                new Rectangle2D.Double(
                        activeCamera.pos.x, activeCamera.pos.y,
                        config.screenWidth, config.screenHeight));
        g.setColor(l.color);
        Composite c = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) l.energy));
        g.fill(ambientArea);
        g.setComposite(c);
    }

    private void drawSphericalLight(Graphics2D g, Light l) {
        l.color = brighten(l.color, l.energy);
        Color medColor = brighten(l.color, l.energy * 0.5);
        Color endColor = new Color(0.0f, 0.0f, 0.0f, 0.2f);

        l.colors = new Color[]{l.color,
                medColor,
                endColor};
        l.dist = new float[]{0.0f, 0.05f, 0.5f};
        l.rgp = new RadialGradientPaint(
                new Point(
                        (int) (l.pos.x + (l.width * 0.5) + (10 * Math.random() * l.glitterEffect)),
                        (int) (l.pos.y + (l.width * 0.5) + (10 * Math.random() * l.glitterEffect))),
                (int) (l.width),
                l.dist,
                l.colors);
        g.setPaint(l.rgp);
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) l.energy));
        g.fill(new Ellipse2D.Double(l.pos.x, l.pos.y, l.width, l.width));
    }

    private void drawSpotLight(Graphics2D g, Light l) {

    }

    /**
     * Make a color brighten.
     *
     * @param color    Color to make brighten.
     * @param fraction Darkness fraction.
     * @return Lighter color.
     * @link https://stackoverflow.com/questions/18648142/creating-brighter-color-java
     */
    public static Color brighten(Color color, double fraction) {

        int red = (int) Math.round(Math.min(255, color.getRed() + 255 * fraction));
        int green = (int) Math.round(Math.min(255, color.getGreen() + 255 * fraction));
        int blue = (int) Math.round(Math.min(255, color.getBlue() + 255 * fraction));

        int alpha = color.getAlpha();

        return new Color(red, green, blue, alpha);

    }
}
