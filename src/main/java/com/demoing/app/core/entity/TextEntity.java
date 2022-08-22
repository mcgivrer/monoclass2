package com.demoing.app.core.entity;

import com.demoing.app.core.service.physic.PhysicType;

import java.awt.*;

/**
 * A {@link TextEntity} extending the {@link Entity} will display a {@link TextEntity#text} with a dedicated
 * {@link TextEntity#font}, with an {@link TextEntity#align} as required on the {@link TextEntity#pos}.
 */
public class TextEntity extends Entity {
    public String text;
    public Font font;
    public TextAlign align = TextAlign.LEFT;

    /**
     * Create a new {@link TextEntity} with a name.
     *
     * @param name the name for this new TextEntity (see {@link Entity#Entity(String)}
     */
    public TextEntity(String name) {
        super(name);
        this.physicType = PhysicType.STATIC;
    }

    /**
     * Set the text value t to be displayed for this {@link TextEntity}.
     *
     * @param t the text for the {@link TextEntity}.
     * @return the {@link TextEntity} with its new text.
     */
    public TextEntity setText(String t) {
        this.text = t;
        return this;
    }

    /**
     * Set the {@link Font} t for this {@link TextEntity}.
     *
     * @param f the {@link Font} to be assigned to this {@link TextEntity}.
     * @return the {@link TextEntity} object with its new assigned {@link Font}.
     */
    public TextEntity setFont(Font f) {
        this.font = f;
        return this;
    }

    /**
     * Set the {@link TextAlign} value for this {@link TextEntity}.
     *
     * @param a the {@link TextAlign} value defining howto draw the text at its {@link TextEntity#pos}.
     * @return the {@link TextEntity} object with its new text alignement defined.
     */
    public TextEntity setAlign(TextAlign a) {
        this.align = a;
        return this;
    }

}
