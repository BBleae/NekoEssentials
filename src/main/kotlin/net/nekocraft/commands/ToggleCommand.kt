package net.nekocraft.commands

import net.minecraft.util.registry.Registry

object ToggleCommand {
    private val playerToGameMode: MutableMap<String?, net.minecraft.world.GameMode?> =
        HashMap<String?, net.minecraft.world.GameMode?>()

    private val INVALID_DIMENSION_EXCEPTION: DynamicCommandExceptionType =
        DynamicCommandExceptionType(java.util.function.Function { id: Any? -> net.minecraft.text.Text.of("invalid home dimension: $id") })

    fun register(dispatcher: CommandDispatcher<ServerCommandSource?>) {
        dispatcher.register(
            CommandManager.literal("toggle")
                .executes(com.mojang.brigadier.Command { context: CommandContext<ServerCommandSource?>? ->
                    execute(
                        context.getSource(),
                        context.getSource().getPlayer()
                    )
                })
        )
    }

    @Throws(CommandSyntaxException::class)
    private fun execute(source: ServerCommandSource, player: ServerPlayerEntity): Int {
        if (!player.isSpectator()) {
            val loc = SavedLocation(
                player.getWorld().getRegistryKey().getValue().toString(),
                player.getX(), player.getY(), player.getZ(),
                player.getYaw(), player.getPitch()
            )
            logger.info(java.lang.String.format("[toggle][set] %s -> %s", player, loc.asFullString()))
            (player as IMixinServerPlayerEntity).setToggleLocation(loc)
            playerToGameMode.put(player.getUuidAsString(), player.interactionManager.getGameMode())
            player.changeGameMode(net.minecraft.world.GameMode.SPECTATOR)
        } else {
            val loc: SavedLocation? = (player as IMixinServerPlayerEntity).getToggleLocation()
            if (loc != null) {
                val registryKey: RegistryKey<World?>? =
                    RegistryKey.of(Registry.WORLD_KEY, net.minecraft.util.Identifier(loc.world))
                val serverWorld: ServerWorld? = source.getServer().getWorld(registryKey)
                if (serverWorld == null) throw INVALID_DIMENSION_EXCEPTION.create(loc.world)

                logger.info(java.lang.String.format("[toggle][teleport] %s -> %s", player, loc.asFullString()))
                player.teleport(serverWorld, loc.x, loc.y, loc.z, loc.yaw, loc.pitch)
            }
            if (playerToGameMode.containsKey(player.getUuidAsString()) && playerToGameMode[player.getUuidAsString()] != null
            ) {
                player.changeGameMode(playerToGameMode[player.getUuidAsString()])
                playerToGameMode.remove(player.getUuidAsString())
            } else {
                player.changeGameMode(net.minecraft.world.GameMode.DEFAULT)
            }
        }
        return 0
    }
}
