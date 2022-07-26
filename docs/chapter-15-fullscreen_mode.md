# Fullscreen mode

Integrate in the `Render` and `Application` window display a full screen mode,

## At Application level:

- move all default Action from Scene to Application
- a key pressed on <kbd>F11</kbd> switches between windowed and full screen display.

## At `Window` (JFrame) level :

- the fullscreen (`DISP_MODE_FULLSCREEN`) mode will extend the existing java awt window to the full screen without
  border and title.
- the windowed (`DISP_MODE_WINDOWED`) mode will keep and restore the last known window's size, borders and title are
  restored .

## At `Configuration` level :

The default window mode required is configured in the configuration file under
the `app.window.fullscreen=true|false` configuration property.
