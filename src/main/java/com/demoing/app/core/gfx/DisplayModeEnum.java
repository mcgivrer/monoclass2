package com.demoing.app.core.gfx;

import com.demoing.app.core.Application;
import com.demoing.app.core.service.render.Renderer;

import javax.swing.*;

/**
 * The possible display mode for the {@link Application} window.
 */
public enum DisplayModeEnum {
    /**
     * The {@link Renderer} will display the {@link Application} window (see {@link JFrame}) in a Full screen mode.
     */
    DISPLAY_MODE_FULLSCREEN,
    /**
     * The {@link Renderer} will display the {@link Application} window (see {@link JFrame}) as a normal window with a title bar.
     */
    DISPLAY_MODE_WINDOWED,
}
