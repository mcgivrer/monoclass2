Feature: Add a new entity TileMap to define full level of a platform game.

  The TileMap is an Entity containing sub entities which define a full platform game level
  with player and ennemies.

  Scenario: A "test_01" Scene has a TileMap "tm_01"
    Given a Scene "test_01"
    Then I add a TileMap named "tm_01"
    And the TileMap "tm_01" has Tile size of 16 x 16
    And the TileMap "tm_01" size is 40 x 20
    Then the Scene contains a TileMap "tm_01" with a map of 800 tiles.

  Scenario: A "tm_02" TileMap is drawn by Renderer
    Given a Scene "test_02"
    And a TileMap named "tm_02" is created
    Then the Renderer has drawn the TileMap "tm_02".
