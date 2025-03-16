# SnowBallFight Plugin

A fun and engaging snowball fight minigame for Minecraft servers running Paper/Spigot 1.20.x.

## Description

SnowBallFight is a customizable minigame where players engage in a competitive snowball battle. Players score points by hitting others with snowballs, and the first player to reach the target score wins the game!

## Features

- **Complete Snowball Battle System**: Score points by hitting other players with snowballs
- **Auto-regenerating Snowballs**: Players automatically receive new snowballs over time
- **Visible Threat System**: Players who are one hit away from winning are highlighted with warning indicators
- **Automatic Boundary System**: Keeps players within the play area with visual warnings
- **Victory Celebration Effects**: Fireworks and special effects for the winner
- **Custom Game Area Setup**: Flexible configuration for arena setup
- **Automatic Middle Point Calculation**: The plugin automatically determines the optimal middle point
- **Scoreboard Integration**: Real-time score tracking for all players
- **Safe Gameplay Environment**: Players are kept in adventure mode with full health and hunger
- **Respawn Mechanics**: Players who get hit respawn at a random spawn point
- **Visual Effects**: Colorful particle effects for hits, respawns, and game events

## Requirements

- Paper/Spigot server 1.20.x (tested on 1.20.1)
- Java 8 or higher

## Installation

1. Download the latest SnowBallFight.jar file from [releases](https://github.com/yourname/SnowBallFight/releases)
2. Place the JAR file in your server's `plugins` folder
3. Restart your server or run `/reload` command
4. The plugin will automatically create necessary files and folders

## Game Setup

Setting up a game area requires just a few simple steps:

1. Create spawn points for players using `/createSpawn` (create multiple in different spots)
2. Set a loser spot (where eliminated players go) using `/setLoserSpot`
3. Set a winner spot (where the winner will be teleported) using `/setWinnerSpot`
4. Use `/gameStatus` to verify all required locations have been set
5. Start the game with `/startSnowBallFight <hits-to-win>`

## Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/createSpawn` | Creates a spawn point at your current location | snowballfight.admin |
| `/setLoserSpot` | Sets where losers will be teleported | snowballfight.admin |
| `/setWinnerSpot` | Sets where the winner will be teleported | snowballfight.admin |
| `/startSnowBallFight <hits-to-win>` | Starts a game with specified hits to win | snowballfight.admin |
| `/endGame` | Ends the current game | snowballfight.admin |
| `/score` | Shows the current game scores | snowballfight.player |
| `/gameStatus` | Shows what has been set up and what still needs to be set | snowballfight.admin |
| `/resetGame` | Removes all game settings (spawn points, middle, loser/winner spots) | snowballfight.admin |

## Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| snowballfight.admin | Allows access to all SnowBallFight admin commands | op |
| snowballfight.player | Allows access to all SnowBallFight player commands | true |

## Gameplay Mechanics

### Game Start
1. When a game starts, all players are teleported to spawn points
2. A 5-second countdown begins
3. After the countdown, players receive snowballs and the game starts
4. Boundary is automatically calculated based on spawn point distances

### During Gameplay
- Players score points by hitting other players with snowballs
- When hit, players are temporarily teleported high in the air and then to a random spawn point
- Players automatically regenerate snowballs over time (up to 16)
- Players must stay within the boundary or risk point penalties
- Visual indicators show which players are close to winning
- Current scores can be viewed using the `/score` command

### Winning
- The first player to reach the target number of hits wins
- The winner is teleported to the winner spot
- Other players are teleported to the loser spot
- Victory fireworks and effects celebrate the winner

## Advanced Features

### Automatic Middle Point Calculation
- The plugin automatically calculates the center point of the arena based on spawn points
- The middle point is used for boundary calculation and respawn mechanics
- No manual setup required - just create your spawn points!

### Threat System
Players who are one hit away from winning are:
- Highlighted with a warning icon (⚠) on the scoreboard
- Given a glowing effect visible to all players
- Marked with red particles and warning fireworks
- If a "threat" player is hit, they lose one point

### Boundary System
- Automatically calculated based on spawn point distances
- Visual warnings when approaching the boundary
- Point penalties for going too far outside the boundary

### Visual Effects
- Colorful particle trails follow snowballs in flight
- Custom effects for hits, respawns, and other events
- Victory fireworks for the winner
- Countdown effects when starting the game

## Data Storage

Game configuration data is stored in the following files in the plugin's data folder:
- `spawnpoints.json`: Player spawn point locations
- `loserspot.json`: Loser teleport location
- `winnerspot.json`: Winner teleport location

## Troubleshooting

### Common Issues

**Issue**: Game won't start  
**Solution**: Use `/gameStatus` to check if all required locations are set and if there are enough players/spawn points

**Issue**: Players falling through the ground after respawn  
**Solution**: Ensure spawn points are on solid ground with at least 2 blocks of clearance above

**Issue**: Scoreboard not showing properly  
**Solution**: Try ending the game with `/endGame` and starting a new game

**Issue**: Players can't hit each other  
**Solution**: Ensure players are in adventure mode (the plugin should do this automatically)

## Planned Features

- Custom snowball types with special effects
- Team-based gameplay
- Custom arena templates
- Configurable game parameters
- Statistics tracking

## Credits

Developed by Jack Hall

## License

This project is licensed under the [Your License] - see the LICENSE file for details.

## Support

If you encounter any issues or have questions, please open an issue on the [GitHub repository](https://github.com/Jack-B-Hall/SnowBallFlight).