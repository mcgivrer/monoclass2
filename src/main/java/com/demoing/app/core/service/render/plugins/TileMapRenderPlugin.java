package com.demoing.app.core.service.render.plugins;

import com.demoing.app.core.entity.Entity;
import com.demoing.app.core.entity.Tile;
import com.demoing.app.core.entity.TileMap;
import com.demoing.app.core.service.render.RenderPlugin;
import com.demoing.app.core.service.render.Renderer;

import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.Optional;

/**
 * Implementation of the TileMap render plugin.
 *
 * @author Frédéric Delorme
 * @since 1.0.6
 **/
public class TileMapRenderPlugin implements RenderPlugin<TileMap> {
    @Override
    public Class<TileMap> getRegisteringClass() {
        return TileMap.class;
    }

    @Override
    public void draw(Renderer r, Graphics2D g, TileMap tme) {
        int tileCounter = 0;
        Map<String, Tile> tiles = (Map<String, Tile>) tme.attributes.get("level.tiles");
        g.setColor(Color.CYAN);
        for (int ix = 0; ix < tme.mapWidth; ix++) {
            for (int iy = 0; iy < tme.mapHeight; iy++) {
                tileCounter++;
                int tileIdx = tme.map[ix + (iy * tme.mapWidth)];
                if (tileIdx != 0) {
                    if (Optional.ofNullable(tiles).isPresent() && tiles.containsKey(tileIdx)) {
                        BufferedImage tileImg = tiles.get(tileIdx).image;
                        g.drawImage(tileImg, ix * tme.tileWidth, iy * tme.tileHeight, null);
                    } else {
                        g.setColor(tme.tilesColor.get(tileIdx));
                        g.fillRect(ix * tme.tileWidth, iy * tme.tileHeight, tme.tileWidth, tme.tileHeight);
                    }
                } else {
                    g.setColor(Color.BLUE);
                    g.drawRect(ix * tme.tileWidth, iy * tme.tileHeight, tme.tileWidth, tme.tileHeight);
                }
            }
        }
        tme.drawn = true;
        tme.tileDrawnCounter = tileCounter;
    }
}
