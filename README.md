# README

A **MonoClass 2** project test.

**UPDATE 2022-JULY-01** 
The project evolves to a too big number of feature, and debugging a monoclass is annoying; I reach the point of "class splitting", or refactoring.
I'e also introduce Maven to perform a better build.
As for now, the project _small_ is now _standard_ and need standard tools and classes.

the project is now structured through multiple packages :

```text
com.demoing.app
|_core
|  |_  behavior
|  |_  config
|  |_  entity
|  |_  gfx
|  |_  io
|  |_  math
|  |_  scene
|  |_  services
|  |   |_  collision
|  |   |_  monitor
|  |   |_  physic
|  |   |_  render
|  |_  utils
|_ demo
     |_ scenes
```

Root package is `com.demoing.app`, and sub-packages are 
- `core` is the game framework, 
- and `demo` ... the Demo !


~~This _small_ Java project with only one (master) class is a proof-of-concept of an over simplified java program to
deliver maximum feature in a minimum number lines of code, and WITHOUT dependencies out of the JDK itself.~~

~~A second challenge is in the no use for a build tool (maven or gradle) but nothing else than the JDK compiler tool and
the magic of a bash script (Back to Roots !).~~

![2022 Gitkraken Project Timeline](docs/images/project-timeline-2022.png "2022 Gitkraken Project Timeline")

And a last constrain is to reduce as much as possible the number of LoC, using enhancement from latest JDK.

Have fun discover this beast of code, to learn and take benefits of some latest JDK features (like
the [pattern matching for switch](https://openjdk.java.net/jeps/406 "ssee the official specification")), and moreover,
overuse of lambda and stream each time this was possible.

> :blue_book: _**NOTE**_[^1]
> _If your are curious, just visit
the "[Java Language Updates](https://docs.oracle.com/en/java/javase/18/language/java-language-changes.html "go to official source of information for Java evolution")"
page from Oracle, you will discover accross release from 9 to 18, the list of new introduced features._
>

## More Doc sir ?

See the [/docs](docs/00-index.md) for details

## Build

Relying an a custom build script, just execute:

```shell
$> mvn clean compile
```

This will build a jar in `target/` directory.

## Run it !

You can run it by executing the following command line :

```shell
$> mvn exec:java
```

(see [Build README](https://gist.github.com/mcgivrer/a31510019029eba73edf5721a93c3dec#file-readme-md) for details on
build script)

Or if you build it with `scripts/build.sh a`, you will be able to run it with :

```shell
$> java --enable-preview -jar target/monoclass2-{project.version}.jar
```

You will get the following window

![LightAndShadow pre-release preparing the v1.0.5 with Light and Influences](docs/images/monoclass2-1.0.5-snapshot-screenshot.png "LightAndShadow pre-release preparing the v1.0.5 with Light and Influences")

_figure 1 - LightAndShadow pre-release preparing the v1.0.5 with Light and Influences_

> :blue_book: _**NOTE**_
> _You can notice the new Time display, limiting the time play._

### Keyboard

Here are all the keys and their corresponding action 

| Key                | Action              |
|:-------------------|:--------------------|
| <kbd>Up</kbd>      | Jump / Move up      |
| <kbd>Down</kbd>    | Move down           |
| <kbd>Left</kbd>    | Move left           |
| <kbd>Right</kbd>   | Move right          |
| <kbd>F3</kbd>      | take screenshot     |
| <kbd>Z</kbd>       | reset current scene |
| <kbd>ESC</kbd>     | Exit the demo       |
| <kbd>K</kbd>       | Empty energy        |
| <kbd>D</kbd>       | Switch visual debug level from 0 to 5 |

### CLI arguments

You can configure dynamically some of the internal parameters:

| Argument                | Configuration key | Description                                                                                         | Default   |
|:------------------------|:------------------|:----------------------------------------------------------------------------------------------------|:----------|
| `w`, `width`            | screenWidth       | The width of the game window                                                                        | 320       |
| `h`, `height`           | screenHeight      | The height of the game window                                                                       | 240       |
| `s`, `scale`            | displayScale      | The the pixel scale                                                                                 | 2         |
| `d`, `debug`            | debug             | the debug level of display mode                                                                     | 1         |
| `ww`, `worldwidth`      | worldWidth        | The width of the world play area                                                                    | 800       |
| `wh`, `worldheight`     | worldHeight       | The height of the world play area                                                                   | 600       |
| `wg`, `worldgravity`    | worldGravity      | The gravity in the world play area                                                                  | 0.981     |
| `spmin`                 | speedMinValue     | Physic Engine threshold object minimum speed to 0                                                   |           |
| `spmax`                 | speedMaxValue     | Physic Engine maximum object speed                                                                  |           |
| `accmin`                | accMinValue       | Physic Engine threshold object minimum acceleration to 0                                            |           |
| `accmax`                | accMaxValue       | Physic Engine maximum object acceleration                                                           |           |
| `cspmin`                | colSpeedMinValue  | Collision Detector threshold object minimum speed to 0                                              |           |
| `cspmax`                | colSpeedMaxValue  | Collision Detector maximum object speed                                                             |           |
| `fps`                   | fps               | Frame Per Second                                                                                    | 60        |
| `f`, `fullScreen`       | fullScreen        | Switch game window to fullscreen mode                                                               | false[^2] |
| `scene`                 | defaultScene      | the default scene to be activated (must be listed in the `app.scenes` in the `app.properties` file) | N/A       |
| `l`, `language`, `lang` | defaultLanguage   | select the preferred language  (existing values are en_EN, fr_FR, es_ES or de_DE).                  | en_EN     |


### Usage example

1. Change the size of the opened window :

```shell
$ java ---enable-preview \
 -jar target/monoclass2-{project.version}.jar \
 w=600 h=400
```

2. set the preferred language to English at start :

```shell
$ java ---enable-preview \
 -jar target/monoclass2-{project.version}.jar \
 language=en_EN
```

3. set the preferred language to French and change the pixel scale :

```shell
$ java ---enable-preview \
 -jar target/monoclass2-{project.version}.jar\
 language=fr_FR s=2.0
```


## Contribute

Feel free to add/edit/modify for your own usage and learn. You can fork this small project to play with.

McG. May 1st, 2022.

[^1]: Icons reference https://gist.github.com/rxaviers/7360908
[^2]: Supported boolean values to set to `true` are "1", "on", "ON", "true", "TRUE", "True", and false value are  "0", "
off", "OFF", "false", "FALSE", "False".
