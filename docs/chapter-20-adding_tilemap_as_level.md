# Adding TileMap as level

## what is a Level

In any platform game, you will move from level to level in a world organized play area.

The level is a definition af a part of game world where player (hero) must fight enemies to achieve a quest.

To simplify level conception and definition, the graphics part of a level is built upon some basic graphic element, the
tiles.

A Tile is a (often) squared graphic element to be used, and reused, everywhere in the level to build more complex
graphics elements. this tile, with a fixed size of 8 pixel multiple value on horizontal and vertical axis is a bitmap
with RGBA color coded.

So a Tile is basically a `BufferedImage` that will be repeated through a level to build this level.

This tiles will be identified with a single int as id, and we are able to define some large level with tons of repeated
tiles.

So a `TileMap` is defined as a rectangular map (mapWidth x mapHeight) and composed of tiles, and tiles have
their own size (tileWidth x tileHeight).

```plantuml
@startuml
!theme plain
hide Entity method
hide Entity attributes
hide TileMap method
class TileMap extends Entity {
 - tileWidth:int
 - tileHeight:int
 - mapWidth:int
 - mapHeight:int
 - map:int[]
}
@enduml
```

