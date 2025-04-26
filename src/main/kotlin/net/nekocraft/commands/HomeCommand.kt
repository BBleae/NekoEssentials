package net.nekocraft.commands

import net.minecraft.util.registry.Registry

object HomeCommand {
    private val NO_HOME_EXCEPTION: SimpleCommandExceptionType =
        SimpleCommandExceptionType(net.minecraft.text.Text.of("你还没有设置过家"))
    private val INVALID_DIMENSION_EXCEPTION: DynamicCommandExceptionType =
        DynamicCommandExceptionType(java.util.function.Function { id: Any? -> net.minecraft.text.Text.of("invalid home dimension: $id") })

    fun register(dispatcher: CommandDispatcher<ServerCommandSource?>) {
        dispatcher.register(
            CommandManager.literal("home")
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
        val loc: SavedLocation = (player as IMixinServerPlayerEntity).getHomeLocation()
        if (loc == null) throw NO_HOME_EXCEPTION.create()

        val registryKey: RegistryKey<World?>? =
            RegistryKey.of(Registry.WORLD_KEY, net.minecraft.util.Identifier(loc.world))
        val serverWorld: ServerWorld? = source.getServer().getWorld(registryKey)
        if (serverWorld == null) throw INVALID_DIMENSION_EXCEPTION.create(loc.world)

        logger.info(java.lang.String.format("[home][teleport] %s -> %s", player, loc.asFullString()))
        (player as IMixinServerPlayerEntity).setLastLocation(
            SavedLocation(
                player.getWorld().getRegistryKey().getValue().toString(),
                player.getX(), player.getY(), player.getZ(), player.getYaw(), player.getPitch()
            )
        )
        player.teleport(serverWorld, loc.x, loc.y, loc.z, loc.yaw, loc.pitch)
        source.sendFeedback(net.minecraft.text.Text.of("已传送到家"), false)

        return 0
    }
}
