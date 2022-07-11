package com.demoing.app.core.io;

import com.demoing.app.core.Application;
import com.demoing.app.core.service.Service;

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
public class ActionHandler implements Service, KeyListener {
    private final boolean[] prevKeys = new boolean[65536];
    private final boolean[] keys = new boolean[65536];
    private boolean ctrlState = false, shiftState = false, altState = false;
    private Application app;
    private boolean anyKeyPressed;

    public Map<Integer, Function> actionMapping = new HashMap<>();

    /**
     * Initialize the service with ots parent {@link Application}.
     */
    public ActionHandler() {

    }


    @Override
    public void keyTyped(KeyEvent e) {
        this.anyKeyPressed = true;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        getAltCtrlShiftStates(e);

        this.keys[e.getKeyCode()] = true;
        if (actionMapping.containsKey(e.getKeyCode())) {
            Function f = actionMapping.get(e.getKeyCode());
            f.apply(e);
        }
    }

    private void getAltCtrlShiftStates(KeyEvent e) {
        this.ctrlState = e.isControlDown();
        this.altState = e.isAltDown();
        this.shiftState = e.isShiftDown();
    }

    @Override
    public void keyReleased(KeyEvent e) {

        getAltCtrlShiftStates(e);
        this.keys[e.getKeyCode()] = false;
    }

    public boolean isAnyKeyPressed() {
        boolean toBeReturned = this.anyKeyPressed;
        this.anyKeyPressed = false;
        return toBeReturned;
    }

    @Override
    public String getName() {
        return "actionHandler";
    }

    @Override
    public void start(Application app) {
        this.app = app;
        // register this listener to the Window
        app.getWindow().addListener(this);
        // register the Screenshot action to the Action mapping.
        this.actionMapping.put(KeyEvent.VK_F3, (e) -> {
            app.getRender().saveScreenshot();
            return this;
        });
    }

    @Override
    public String[] getDependencies() {
        return new String[]{"render"};
    }

    @Override
    public void dispose(Application app) {

    }
}
