package com.demoing.app.core.entity.helpers;

import com.demoing.app.core.entity.TextEntity;

/**
 * THe TextAlign attribute value is use for TextEntity only, to define how the rendered text must be aligned.
 * Possible values are LEFT, CENTER and RIGHT.
 */
public enum TextAlign {
    /**
     * The text provided for the {@link TextEntity} will be justified on LEFT side of the text rectangle position.
     */
    LEFT,
    /**
     * The text provided for the {@link TextEntity} will be centered on its current position.
     */
    CENTER,
    /**
     * The text provided for the {@link TextEntity} will be justified on RIGHT side of the text rectangle position.
     */
    RIGHT
}
