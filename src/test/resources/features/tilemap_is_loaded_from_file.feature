Feature: Add a new entity TileMap to define full level of a platform game.

  The TileMap is an Entity containing sub entities which define a full platform game level
  with player and enemies.

  Scenario: A "test_01" Scene has a TileMap "tm_01"
    Given a Scene "test_01"
    Then I add a TileMap named "tm_01"
    And the TileMap "tm_01" has Tile size of 16 x 16
    And the TileMap "tm_01" size is 40 x 20
    Then the Scene contains a TileMap "tm_01" with a map of 800 tiles.

  Scenario: A "tm_02" TileMap is drawn by Renderer
    Given a Scene "test_02"
    And the Scene creates a TileMap named "tm_02"
    Then the Renderer has drawn the TileMap "tm_02".

  Scenario: A "tm_03" TileMap defines objects
    Given a Scene "test_03"
    And the Scene creates a TileMap named "tm_03"
    Then the TileMap "tm_03" has an Object named "enemy_$"
    And  the TileMap "tm_03" has an Object named "player"

  Scenario: A "tm_03" TileMap defines an enemy_$ entity with attributes
    Given a Scene "test_03"
    And the Scene creates a TileMap named "tm_03"
    Then the TileMap "tm_03" has an Object named "enemy_$"
    And the Object named "enemy_$" from TileMap "tm_03" has attribute "class" with value "com.demoing.app.core.entity.Entity"
    And the Object named "enemy_$" from TileMap "tm_03" has attribute "attributes" with value "[live=10,fire=5]"

  Scenario: A "tm_03" TileMap create Entity
    Given a Scene "test_03"
    And the Scene creates a TileMap named "tm_03"
    Then a GameObject named "player" is created in TileMap "tm_03"

  Scenario Outline: A "tm_03" TileMap create multiple GameObject instance in map
    Given a Scene "test_03"
    And the Scene creates a TileMap named "tm_03"
    Then a Entity instance named {instanceName} is created in TileMap "tm_03"
    Examples:
      | instanceName |
      | enemy_1      |
      | enemy_2      |