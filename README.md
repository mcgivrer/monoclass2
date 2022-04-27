# README

A **MonoClass 2** project test.

This _small_ Java project with only one (master) class is a proof-of-concept of an over simplified java program to deliver maximum feature in a minimum number lines of code, and WITHOUT dependecies out of the JDK itself.

A second challenge is in the no use for a build tool (maven or gradle) but nothing else than the JDK compiler tool and the magic of a bash script (Back to Roots !).

Have fun discover this beast of code, to learn and take benefits of some latest JDK features (like the [pattern matching for switch](https://openjdk.java.net/jeps/406 "ssee the official specification")), and moreover, overuse of lambda and stream each time this was possible.

> :blue_book: _**NOTE**_[^1]
> _If your are curious, just visit the "[Java Language Updates](https://docs.oracle.com/en/java/javase/18/language/java-language-changes.html "go to official source of information for Java evolution")" page from Oracle, you will discover accross release from 9 to 18, the list of new introduced features._
>
## A bit of doc please

The [`Application`](https://github.com/mcgivrer/monoclass2/blob/feature/add-camera-entity/src/main/java/com/demoing/app/Application.java#L17) class (we can not talk about a project for a 500 LoCs class) is composed of some attribtues and methods (sic) and a limited number of subclasses.

![Application UML class diagram](http://www.plantuml.com/plantuml/proxy?cache=no&src=https://raw.githubusercontent.com/mcgivrer/monoclass2/feature/add-camera-entity/docs/class-diagram.txt?token=GHSAT0AAAAAABSBHBPSAECVR3Q5SNLJJVICYTJ3JGA "Class Diagram for Application and its subclasses")

_[edit](https://github.com/mcgivrer/monoclass2/blob/feature/add-camera-entity/docs/class-diagram.txt)_

So, [`I18n`](https://github.com/mcgivrer/monoclass2/blob/feature/add-camera-entity/src/main/java/com/demoing/app/Application.java#L69), `Entity`, `TextEntity` and `Camera` are subclasses of `Application` master class, if you does not understand :P.

### Application

The main class (and the jar entrypoint) is composed of some feature centric attributes like:

- `entites` which is a map of `Entity` managed by the program,
- `gPipeline` is the list of entities to be rendered to screen.
- `activeCamera` is the possible `Camera` to see through to focus a specific target `Entity`.

A bunch of methods are for internal initialization and processing only:

- `initialize()`, `loadConfig()`, `parseArg()`, `parseDouble()`, `parseInt()` to start the app, load configuration and parse agrs
- `run()`, `loop()`, `update()`,  to execute the main loop,
- `updateEntity`, `applyPhysicRuleToEntity`, `ContrainEntity()`, `constrainToWorld()` to update and compute entities moves, physics and constrains,
- `draw()`, `renderToScreen()`,`moveCamera()` to process rendering pipeline.

And some methods that can be adapted:

- `loadConfig()` to define some configuratble parameters from the `app.propertties` file,
- `parseArg()` to override configuration with CLI arguments values,
- `createScene()` to define the game scene with `Entity` and `Camera`,
- `input()` to process key input for game interaction.

And some utilities / helpers:

- `addEntity()` to add an `Entity` to the application,
- `addCamera()` to define the active Camera, if one must be added (optional).

## Build

Relying an a custom build script, just execute:

```shell
$> build.sh a
```

> :blue_book: _**NOTE**_
> _for curious people, you can experiment the following command line :
> `$> build h` 
> getting help in this cli build script._

This will build a jar in `target/` directory.

## Contribute

Feel free to add/edit/modify for your own usage and learn. You can fork this small project to play with.

McG. May 1st, 2022.

[^1]: Icons reference https://gist.github.com/rxaviers/7360908
