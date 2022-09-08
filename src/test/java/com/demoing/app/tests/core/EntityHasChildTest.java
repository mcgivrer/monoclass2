package com.demoing.app.tests.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;


import com.demoing.app.core.entity.Entity;
import org.junit.jupiter.api.Test;

public class EntityHasChildTest {
    Entity entity;

    @Test
    public void entityCanHaveaddOneChildTest() {
        entity = new Entity("parent");
        entity.addChild(new Entity("child1"));
        assertNotEquals(0, entity.getChild().size(), 0, "The parent entity has no child");
    }

    @Test
    public void entityCanHaveMulitpleChildTest() {
        entity = new Entity("parent");
        for (int i = 0; i < 20; i++) {
            entity.addChild(new Entity("child_" + i));
        }
        assertEquals(20, entity.getChild().size(), 0, "The parent entity has no multiple child");
    }
}
