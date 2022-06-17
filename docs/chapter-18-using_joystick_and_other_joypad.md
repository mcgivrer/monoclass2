# Using Joystick and other Joypad

A good game framework must provide a way to support any control device. Keyboard and mouse are the basic ones. But in a
new 2022 framework, it must support more and Joystick and joypad are both mandatory.

Also, to be able to support such device, native java JDK does not offer a propoer way API or library.

This where we are going to go little out of the scope of this project, we will add an external dependency, and won't
stick to the JDK API.

## JInput

The [JInput](https://jinput.github.io/jinput/ "go and visit the official website for JInput library") library, developed
mainly for the LibGDX support for control device, but also for any java game, and provide all the right API and loop to
interact between game loop and a joystick.

You can visit also the [github repository](https://github.com/jinput/jinput "Github repository for JInput") for this
library to discover it further more in its usage details.

> **Good-To-Know**
> This library is multi-OS

In our code example, we will go with the Playstation 3 bluetooth controller, but know that any plugged-in controller
will do the job, only the button/stick mapping will have to be updated accordingly.

> :blue_book: **NOTE**
> Maybe an adaptator design pattern would be used here to do the trick.

### Add it to our Applciation

The first thing we will have to do is to add these dependecies, in fact, 2 JAR, one for the API and the other for the
multi-OS adaptors.

```bash
export LIB_DEP="$LIBS/dependencies/jinput-2.0.9.jar $LIBS/dependencies/jinput-2.0.9-natives-all.jar"
```

This new `LIB_DEP` internal build script variable will let us adding any dependency to our project, for compilation and
execution.

### Bring new implementation

In the existing ActionHandler, we will add an Update operation to retrieve controllers status and manage corresponding
actions.

```java
public class ActionHandler implements KeyListener {

    private Event event;
    private Controller[] controllers;

    public ActionHandler(Application a) {
        this.app = a;
        this.event = new Event();
        this.controllers = ControllerEnvironment.getDefaultEnvironment().getControllers();
        this.actionMapping.put(KeyEvent.VK_F3, (e) -> {
            app.render.saveScreenshot();
            return this;
        });
    }

    public void update(double elapsed) {
        for (int i = 0; i < controllers.length; i++) {
            /* Remember to poll each one */
            this.controllers[i].poll();
            System.out.printf("%d: controller %s",this.controllers[i].getType());
            /* Get the controller's event queue */
            EventQueue queue = this.controllers[i].getEventQueue();

            /* For each object in the queue */
            while (queue.getNextEvent(event)) {
                /* Get event component */
                Component comp = this.event.getComponent();

                /* Process event (your awesome code) */
                manageComponentAction(comp);
            }
        }
    }

    public void manageComponentAction(Component c) {
        // ...
    }
}
```