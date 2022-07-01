package com.demoing.app.core.io;

import com.demoing.app.core.Application;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A Resource manager to load and buffered all necessary resources.
 */
public class Resources {
    /**
     * THe internal buffer for resources
     */
    static Map<String, Object> resources = new ConcurrentHashMap<>();

    /**
     * Load an image and store it into buffer.
     *
     * @param path path of the image
     * @return the loaded image.
     */
    public static BufferedImage loadImage(String path) {
        BufferedImage img = null;
        if (resources.containsKey(path)) {
            img = (BufferedImage) resources.get(path);
        } else {
            try {
                InputStream is = Resources.class.getResourceAsStream(path);
                img = ImageIO.read(Objects.requireNonNull(is));
                resources.put(path, img);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return img;
    }

    /**
     * Load a Font and store it into buffer.
     *
     * @param path path of the Font (True Type Font)
     * @return the loaded Font
     */
    public static Font loadFont(String path) {
        Font f = null;
        if (resources.containsKey(path)) {
            f = (Font) resources.get(path);
        } else {
            try {
                f = Font.createFont(
                        Font.PLAIN,
                        Objects.requireNonNull(Resources.class.getResourceAsStream(path)));
            } catch (FontFormatException | IOException e) {
                throw new RuntimeException(e);
            }
        }
        return f;
    }

    /**
     * Free all loaded resources.
     */
    public static void dispose() {
        resources.clear();
    }
}
