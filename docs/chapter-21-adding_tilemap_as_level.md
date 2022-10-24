# Adding TileMap as level

## What is a Level ?

In any platform game, you will move from level to level in a world organized play area.

The level is a definition af a part of game world where player (hero) must fight enemies to achieve a quest.

To simplify level conception and definition, the graphics part of a level is built upon some basic graphic element, the
tiles.

### What is a Tile ?

A Tile is a (often) squared graphic element to be used, and reused, everywhere in the level to build more complex
graphics elements. this tile, with a fixed size of 8 pixel multiple value on horizontal and vertical axis is a bitmap
with RGBA color coded.

So a Tile is basically a `BufferedImage` that will be repeated through a level to build this level.

> **Note:**<br/>
> We will start with color rectangle, the tiles list will contain map  `id:int` and `color:Color`.

This tiles will be identified with a single int as id, and we are able to define some large level with tons of repeated
tiles.

### What is a map ?

Here is a map of a level :

```text
+--------------------------------+
|10000000011110000000000000000001|
|10800000011110000020000002000001|
|10111111111111111111111111111111|
|10000000000000000000000011111111|
|10000000000000000000000011111111|
|10111111111111111111111111111111|
|10111111111111111111111111111111|
|10000000000000000000000011111111|
|10000000000000000000000011111111|
|10111111111111111111111111111111|
|10000000011110000000000000000001|
+--------------------------------+
```

Each number on this map is a tile id, corresponding to an element in the tiles map, for a dedicated tile element.

## TileMap entity

So a `TileMap` is as any other entity in the `Application`, an extended `Entity`, and is defined as a
rectangular `map` (which size is `mapWidth` x `mapHeight`) and composed of `tiles`, and tiles have
their own size (`tileWidth` x `tileHeight`).

```plantuml
@startuml
!theme plain
hide Entity method
hide Entity attributes
hide TileMap method
class TileMap extends Entity {
 - tiles:Map<Integer,Color>
 - tileWidth:int
 - tileHeight:int
 - mapWidth:int
 - mapHeight:int
 - map:int[]
}
@enduml
```

