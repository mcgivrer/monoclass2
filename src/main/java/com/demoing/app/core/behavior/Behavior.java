package com.demoing.app.core.behavior;

import com.demoing.app.core.Application;
import com.demoing.app.core.entity.Entity;

public interface Behavior {
    String ON_COLLISION = "onCollide";
    String ON_UPDATE_ENTITY = "updateEntity";
    String ON_UPDATE_SCENE = "updateScene";

    String filterOnEvent();

    void update(Application a, Entity e, double elapsed);

    void update(Application a, double elapsed);

    void onCollide(Application a, Entity e1, Entity e2);

}
