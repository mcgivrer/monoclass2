package com.demoing.app.core.gfx;

import com.demoing.app.core.Application;
import com.demoing.app.core.config.Configuration;
import com.demoing.app.core.utils.I18n;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Optional;

/**
 * THe Window class is the link between Application, Render and the OS.
 * It will implement the necessary listener for keyboard connectivity or more.
 *
 * @author Frédéric Delorme
 * @since 1.0.5
 */
public class Window extends JPanel implements KeyListener {

    private final boolean[] prevKeys = new boolean[65536];
    private final boolean[] keys = new boolean[65536];
    private boolean anyKeyPressed;
    private boolean keyCtrlPressed;
    private boolean keyShiftPressed;

    /**
     * Display Mode for the application window.
     */
    private DisplayModeEnum displayMode;
    private JFrame frame;

    private Application app;

    public Window(Application app) {
        this.app = app;
        createWindow();
    }


    private void createWindow() {
        Configuration config = app.getConfiguration();
        setWindowMode(config.fullScreen);
    }

    /**
     * Create the JFrame window in fullscreen or windowed mode (according to fullScreenMode boolean value).
     *
     * @param fullScreenMode the display mode to be set:
     *                       <ul>
     *                       <li>true = DISPLAY_MODE_FULLSCREEN,</li>
     *                       <li>false = DISPLAY_MODE_WINDOWED</li>
     *                       </ul>
     */
    public void setWindowMode(boolean fullScreenMode) {
        Configuration config = app.getConfiguration();
        GraphicsEnvironment graphics =
                GraphicsEnvironment.getLocalGraphicsEnvironment();

        GraphicsDevice device = graphics.getDefaultScreenDevice();

        if (Optional.ofNullable(frame).isPresent() && frame.isVisible()) {
            frame.setVisible(false);
            frame.dispose();
        }

        frame = new JFrame(I18n.get("app.title"));
        frame.setIconImage(Toolkit.getDefaultToolkit()
                .getImage(getClass()
                        .getResource("/images/sg-logo-image.png")));
        frame.setContentPane(this);

        displayMode = fullScreenMode ? DisplayModeEnum.DISPLAY_MODE_FULLSCREEN : DisplayModeEnum.DISPLAY_MODE_WINDOWED;

        if (displayMode.equals(DisplayModeEnum.DISPLAY_MODE_FULLSCREEN)) {
            frame.setUndecorated(true);
            device.setFullScreenWindow(frame);
        } else {

            Dimension dim = new Dimension((int) (config.screenWidth * config.displayScale),
                    (int) (config.screenHeight * config.displayScale));
            frame.setSize(dim);
            frame.setPreferredSize(dim);
            frame.setMaximumSize(dim);
            frame.setLocationRelativeTo(null);
            frame.setUndecorated(false);
        }
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setFocusTraversalKeysEnabled(true);

        frame.addKeyListener(this);

        frame.pack();
        frame.setVisible(true);
        if (Optional.ofNullable(frame.getBufferStrategy()).isEmpty()) {
            frame.createBufferStrategy(config.numberOfBuffer);
        }
    }

    /**
     * Add a new Key Listener to the Window
     *
     * @param kl a KeyListener to be added.
     */
    public void addListener(KeyListener kl) {
        frame.addKeyListener(kl);
    }

    public boolean isCtrlPressed() {
        return keyCtrlPressed;
    }

    public boolean isShiftPressed() {
        return keyShiftPressed;
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        prevKeys[e.getKeyCode()] = keys[e.getKeyCode()];
        keys[e.getKeyCode()] = true;
        anyKeyPressed = true;
        this.keyCtrlPressed = e.isControlDown();
        this.keyShiftPressed = e.isShiftDown();
    }

    @Override
    public void keyReleased(KeyEvent e) {
        prevKeys[e.getKeyCode()] = keys[e.getKeyCode()];
        keys[e.getKeyCode()] = false;
        anyKeyPressed = false;
        this.keyCtrlPressed = e.isControlDown();
        this.keyShiftPressed = e.isShiftDown();
    }

    public boolean isKeyPressed(int keyCode) {
        assert (keyCode >= 0 && keyCode < keys.length);
        return this.keys[keyCode];
    }

    public boolean isKeyReleased(int keyCode) {
        assert (keyCode >= 0 && keyCode < keys.length);
        boolean status = !this.keys[keyCode] && prevKeys[keyCode];
        prevKeys[keyCode] = false;
        return status;
    }

    public void dispose() {
        if (Optional.ofNullable(frame).isPresent()) {
            frame.dispose();
        }
    }


    public JFrame getFrame() {
        return this.frame;
    }
}
