# README

[![Java CI with Maven](https://github.com/mcgivrer/monoclass2/actions/workflows/main.yml/badge.svg)](https://github.com/mcgivrer/monoclass2/actions/workflows/main.yml)
[![Known Vulnerabilities](https://snyk.io//test/github/mcgivrer/monoclass2/badge.svg?targetFile=pom.xml)](https://snyk.io//test/github/mcgivrer/monoclass2?targetFile=pom.xml) 
[![FOSSA Status](https://app.fossa.com/api/projects/git%2Bgithub.com%2Fmcgivrer%2Fmonoclass2.svg?type=shield)](https://app.fossa.com/projects/git%2Bgithub.com%2Fmcgivrer%2Fmonoclass2?ref=badge_shield) 
[![Codacy Badge](https://app.codacy.com/project/badge/Grade/005379698de87a7a17cc9c8fa9b8b109ea8d893f)](https://www.codacy.com/gh/mcgivrer/monoclass2/dashboard?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=mcgivrer/monoclass2&amp;utm_campaign=Badge_Grade)

A **MonoClass 2** v1.0.6 project test.

## Introduction

This small Java project with only one (master) class is a proof-of-concept of an over simplified java program to deliver maximum feature in a minimum number lines of code, and WITHOUT dependencies out of the JDK itself (but only for test purpose, using JUnit and Cucumber). 

> **UPDATE 2022-JULY-01**<br/>
>The project evolves to a too big number of feature, and debugging a `monoclass` is annoying; I reach the point of "class
>splitting", or refactoring.
>I've also introduced Maven to perform a better build (see [pom.xml](./pom.xml "open the maven build file"))
>As for now, the project _small_ is now _standard_ and need standard tools and classes.
>
>the project is now structured through multiple packages :
>
>```text
>[com.demoing.app]
>  |_core
>  |  |_  behavior
>  |  |_  config
>  |  |_  entity
>  |  |_  gfx
>  |  |_  io
>  |  |_  math
>  |  |_  scene
>  |  |_  services
>  |  |   |_  collision
>  |  |   |_  monitor
>  |  |   |_  physic
>  |  |   |_  render
>  |  |   |_  scene
>  |  |_  utils
>  |_ demo
>       |_ scenes
>```

Root package is `com.demoing.app`, and sub-packages are

- `core` is the game framework,
- and `demo` ... the Demo project !

## Roadmap

[![2022 Gitkraken Project Timeline](docs/images/project-timeline-2022.png "2022 Gitkraken Project Timeline")](https://timelines.gitkraken.com/timeline/7611459f03de41c09848c1cd543f87bb)

T reduce as much as possible the number of LoC, using enhancement from latest JDK.

Have fun discover this beast of code, to learn and take benefits of some latest JDK features (like
the [pattern matching for switch](https://openjdk.java.net/jeps/406 "ssee the official specification")), and moreover,
overuse of lambda and stream each time this was possible.

> :blue_book: _**NOTE**_ [^1]<br/>
> _If you are curious, just visit
the "[Java Language Updates](https://docs.oracle.com/en/java/javase/18/language/java-language-changes.html
> "go to official source of information for Java evolution")"
page from Oracle, you will discover across release from 9 to 18, the list of new introduced features._

## More Doc sir ?

See the [/docs](docs/00-index.md) for details

## Build

Relying an a custom build script, just execute:

```shell
$> mvn clean compile
```

This will build a jar in `target/` directory.

## Run it

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

| Key              | Action                                |
| :--------------- | :------------------------------------ |
| <kbd>Up</kbd>    | Jump / Move up                        |
| <kbd>Down</kbd>  | Move down                             |
| <kbd>Left</kbd>  | Move left                             |
| <kbd>Right</kbd> | Move right                            |
| <kbd>F3</kbd>    | take screenshot                       |
| <kbd>Z</kbd>     | reset current scene                   |
| <kbd>ESC</kbd>   | Exit the demo                         |
| <kbd>K</kbd>     | Empty energy                          |
| <kbd>D</kbd>     | Switch visual debug level from 0 to 5 |

### CLI arguments

#### Common arguments

You can configure dynamically some internal parameters:

| Argument                | Configuration key           | Description                                                                                         | Default   |
| :---------------------- |:----------------------------| :-------------------------------------------------------------------------------------------------- |:----------|
| `w`, `width`            | app.screen.width            | The width of the game window                                                                        | 320       |
| `h`, `height`           | app.screen.height           | The height of the game window                                                                       | 240       |
| `s`, `scale`            | app.screen.scale            | The the pixel scale                                                                                 | 2         |
| `fps`                   | app.screen.fps              | Frame Per Second                                                                                    | 60        |
| `f`, `fullScreen`       | app.window.mode.fullscreen  | Switch game window to fullscreen mode                                                               | false[^2] |
| `ww`, `worldwidth`      | app.world.area.width        | The width of the world play area                                                                    | 800       |
| `wh`, `worldheight`     | app.world.area.height       | The height of the world play area                                                                   | 600       |
| `wg`, `worldgravity`    | app.world.gravity           | The gravity in the world play area                                                                  | 0.981     |
| `spmin`                 | app.physic.speed.min        | Physic Engine threshold object minimum speed to 0                                                   | 0.1       |
| `spmax`                 | app.physic.speed.max        | Physic Engine maximum object speed                                                                  | 3.2       |
| `accmin`                | app.physic.acceleration.min | Physic Engine threshold object minimum acceleration to 0                                            | 0.01      |
| `accmax`                | app.physic.acceleration.max | Physic Engine maximum object acceleration                                                           | 3.5       |
| `cspmin`                | app.collision.speed.min     | Collision Detector threshold object minimum speed to 0                                              | 0.1       |
| `cspmax`                | app.collision.speed.max     | Collision Detector maximum object speed                                                             | 3.2       |
| `scene`                 | app.scene.default           | the default scene to be activated (must be listed in the `app.scenes` in the `app.properties` file) | N/A       |
| `l`, `language`, `lang` | app.language.default        | select the preferred language  (existing values are en_EN, fr_FR, es_ES or de_DE).                  | en_EN     |

#### Debug specific arguments

| Argument             | Configuration key      | Description                                                                                                         | Default |
|:---------------------|:-----------------------|:--------------------------------------------------------------------------------------------------------------------|:--------|
| `d`, `debug`         | app.debug.level        | Debug level (0=no debug to 5=max debut)                                                                             | 0       |
| `of`, `objectFilter` | app.debug.objectFilter | String coma separated of objects named to activate details debug display information for.                           | ""      |
| `ll`, `logLevel`     | app.logger.level       | Logger level (0=none, 5=all)                                                                                        | 0       |


### Usage example

1. Change the size of the opened window :

```shell
$ java --enable-preview \
 -jar target/monoclass2-{project.version}.jar \
 w=600 h=400
```

2. set the preferred language to English at start :

```shell
$ java --enable-preview \
 -jar target/monoclass2-{project.version}.jar \
 language=en_EN
```

3. set the preferred language to French and change the pixel scale :

```shell
$ java --enable-preview \
 -jar target/monoclass2-{project.version}.jar \
 -Dlanguage=fr_FR s=2.0
```

## Contribute

Feel free to add/edit/modify for your own usage and learn. You can fork this small project to play with.

McG. May 1st, 2022.

[^1]: Icons reference <https://gist.github.com/rxaviers/7360908>
[^2]: Supported boolean values to set to `true` are "1", "on", "ON", "true", "TRUE", "True", and false value are  "0", "
off", "OFF", "false", "FALSE", "False".
