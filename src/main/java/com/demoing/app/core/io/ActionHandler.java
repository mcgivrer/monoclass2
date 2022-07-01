package com.demoing.app.core.io;

import com.demoing.app.core.Application;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * A brand-new service to manage action on KeyEvent.
 *
 * @author Frédéric Delorme
 * @since 1.0.3
 */
public class ActionHandler implements KeyListener {
    private final boolean[] prevKeys = new boolean[65536];
    private final boolean[] keys = new boolean[65536];
    private final Application app;
    private boolean anyKeyPressed;

    public Map<Integer, Function> actionMapping = new HashMap<>();

    /**
     * Initialize the service with ots parent {@link Application}.
     *
     * @param a THe parent application to link the ActionHandler to.
     */
    public ActionHandler(Application a) {
        this.app = a;
        this.actionMapping.put(KeyEvent.VK_F3, (e) -> {
            app.render.saveScreenshot();
            return this;
        });
    }


    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (actionMapping.containsKey(e.getKeyCode())) {
            Function f = actionMapping.get(e.getKeyCode());
            f.apply(e);
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }
}
