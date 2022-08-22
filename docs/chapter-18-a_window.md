# A Window

At this point, having the Frame and window management managed at Application level is not the way we need to do
architecture:
One class one goal !

## Create a new Window class

So we are going to delegate the JFrame window management to a dedicated class:  `Window` !

So the new `Window` class will extend the `JPanel`, supporting the AWT/Swing component, and will also implement the
`KeyListener` to support Keyboard interaction.

```java
public class Window extends JPanel implements KeyListener {
    //..
}
```

## Window creation

The createWindow method from Application will now move to Window:

```java
public class Window extends JPanel implements KeyListener {
    private DisplayModeEnum displayMode;
    private JFrame frame;

    //...
    private void createWindow() {
        setWindowMode(app.config.fullScreen);
    }


    public void setWindowMode(boolean fullScreenMode) {
        //...
    }
}
```

So add the `JFrame` attribute and the `DisplayModeEnum` are now moved to this `Window` class.

## Keyboard management

All the implemented method in Application to manage Key event are moved to Window :

```java
public class Window extends JPanel implements KeyListener {
    private final boolean[] prevKeys = new boolean[65536];
    private final boolean[] keys = new boolean[65536];
    private boolean anyKeyPressed;
    private boolean keyCtrlPressed;
    private boolean keyShiftPressed;
    //...
}
```

We will not go into details for `KeyListener` implementation, but we need to notice that the `keys` and `prevKeys`
attributes
and the `keyCtrlPressed` and `keyShiftPressed` are now part of the Window class.

We also need a way to add an external listener, ion our case, the ActionHandler will have to be added to the window:

```java
public class Window extends JPanel implements KeyListener {
    //...

    public void addListener(KeyListener kl) {
        frame.addKeyListener(kl);
    }
}
```

We also need some getter to test key state :

```java
public class Window extends JPanel implements KeyListener {
    //...
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
}
```
