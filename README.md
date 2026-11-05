# Emerald Starcat Minecraft Plugin

Emerald Starcat is a Minecraft plugin for Paper servers that summons an alien cat in an aerospace ship, gives the summoning player five emeralds, and lets the player tame a 75-heart alien cat that protects its owner.

## Install

1. Use Java 21 or newer.
2. Use a Paper Minecraft 1.21 server.
3. Build the plugin with Maven:
   ```text
   mvn package
   ```
4. Copy the generated `target/plugin.jar` file into the server's `plugins` folder.
5. Start or restart the server.
6. Confirm that the plugin is enabled in the server console.

The plugin does not require a configuration file or database.

## Use

1. Join the server as an operator, or grant the `aliencat.summon` permission.
2. Run:
   ```text
   /aliencat
   ```
3. The plugin creates an aerospace ship and an alien cat near the player.
4. The summoning player receives five emeralds.
5. Hold an emerald and interact with the untamed alien cat. Each interaction consumes one emerald and tames the cat.
6. Once tamed, the alien cat follows its owner and attacks living entities that damage the owner.
7. If the tamed alien cat moves more than 24 blocks from its owner, it is teleported back nearby.
8. The aerospace ship is invulnerable and is removed when its alien cat dies.

The alien cat has 150 maximum health, equal to 75 hearts.

---

More Minecraft plugin projects by EnderDevelopment: https://github.com/EnderDevelopment

Project page: https://dash.enderdevelopment.com/community/fa14cb49-601b-4e51-bde6-551677f1ef85?utm_source=github&utm_medium=readme&utm_campaign=auto_publish&utm_content=fa14cb49-601b-4e51-bde6-551677f1ef85
