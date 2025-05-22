# NekoEssentials

Welcome to NekoEssentials! 🐾

NekoEssentials is a Fabric mod for Minecraft designed to enhance your server experience by adding a collection of useful commands and delightful features. Whether you're a server administrator or a player, NekoEssentials aims to make your Minecraft life easier and more fun!

## Features

NekoEssentials comes packed with commands to streamline your gameplay and server management:

### Teleportation
*   `/back`: Return to your previous location.
*   `/home`: Teleport to your set home.
*   `/sethome`: Set your home location.
*   `/tpa <player>`: Request to teleport to another player.
*   `/tpaccept`: Accept a teleport request.
*   `/tpadeny`: Deny a teleport request.
*   `/tpahere <player>`: Request a player to teleport to you.
*   `/warp <name>`: Teleport to a predefined warp point (admin-defined).

### Inventory Management
*   `/hand`: Show other players the item you're currently holding.
*   `/hat`: Wear the item you're holding as a hat.
*   `/head <player>`: Get the head of a specified player.
*   `/enderchest` or `/ec`: Open your Ender Chest anywhere.
*   `/openinv <player>`: Open a player's inventory (requires permission).
*   `/takeoff`: Remove your current hat.

### Server Administration & Utility
*   `/acceptrules`: Accept the server rules.
*   `/denyrules`: Decline the server rules (action might be configurable, e.g., kick).
*   `/toggle <feature>`: Toggle various mod features (details may vary).
*   LuckPerms integration for fine-grained command permissions.
*   Carpet Mod compatibility/integration (details on specific features to be added if known).

*(More features might be available, check in-game or with server admins!)*

## Dependencies & Integrations

*   **Fabric:** NekoEssentials is a Fabric mod and requires the Fabric Loader and Fabric API to run.
*   **LuckPerms:** Integrates with LuckPerms for permission management. This allows server owners to control who can use which commands.
*   **Carpet Mod:** Includes compatibility or specific features related to the Carpet Mod.
*   **Minecraft:** Requires Minecraft version `1.21.5` (as per `fabric.mod.json`).
*   **Java:** Requires Java `21` or newer.

## Installation

1.  Ensure you have Fabric Loader installed. If not, you can get it from [the Fabric website](https://fabricmc.net/use/).
2.  Download the latest version of NekoEssentials (`.jar` file) from the [releases page](https://github.com/YOUR_USERNAME/NekoEssentials/releases) (Replace `YOUR_USERNAME/NekoEssentials` with the actual repository path if different, or point to Modrinth/CurseForge if applicable).
3.  Download the Fabric API if you haven't already. It's required by many Fabric mods, including NekoEssentials.
4.  Place the NekoEssentials `.jar` file (and Fabric API `.jar` if needed) into your Minecraft `mods` folder.
5.  Launch Minecraft using the Fabric profile.

Enjoy the new features!

## Configuration

NekoEssentials offers various configuration options to tailor the mod to your server's needs. You can find and modify the configuration file in the `config/nekoessentials.json` (or a similarly named file, typically generated on the first run) in your Minecraft server directory.

While the source code mentions `NekoConfig.kt`, the actual configuration will be generated in a common format like JSON or TOML in your server's `config` folder.

## Support & Issues

If you encounter any bugs, have feature suggestions, or need help, please open an issue on our [GitHub Issues page](https://github.com/YOUR_USERNAME/NekoEssentials/issues) (Replace `YOUR_USERNAME/NekoEssentials` with the actual repository path).

We encourage you to update the author information to properly credit the creators!
