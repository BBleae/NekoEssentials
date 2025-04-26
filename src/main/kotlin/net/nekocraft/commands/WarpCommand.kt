package net.nekocraft.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import com.mojang.brigadier.suggestion.SuggestionProvider
import net.minecraft.command.CommandSource
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.nekocraft.config.NekoConfigParsed
import java.util.stream.Stream

object WarpCommand {
    val INVALID_WARP_POINT_EXCEPTION: DynamicCommandExceptionType =
        DynamicCommandExceptionType(java.util.function.Function { name: Any? -> net.minecraft.text.Text.of("路径点 $name 不存在") })
    private val INVALID_DIMENSION_EXCEPTION: DynamicCommandExceptionType =
        DynamicCommandExceptionType(java.util.function.Function { id: Any? -> net.minecraft.text.Text.of("invalid warp dimension: $id") })

    fun register(dispatcher: CommandDispatcher<ServerCommandSource?>) {
        dispatcher.register(
            CommandManager.literal("warp")
                .then(
                    CommandManager.argument<String?>("target", StringArgumentType.word())
                        .suggests(SuggestionProvider { context: CommandContext<ServerCommandSource?>?, builder: SuggestionsBuilder? ->
                            CommandSource.suggestMatching(
                                Stream.concat<T?>(
                                    NekoConfigParsed.warpPoints.keySet().stream(),
                                    Stream.of<String?>("spawn")
                                )
                                    .collect(Collectors.toSet()), builder
                            )
                        })
                        .executes(com.mojang.brigadier.Command { context: CommandContext<ServerCommandSource?>? ->
                            execute(
                                context.getSource(), context.getSource().getPlayer(),
                                StringArgumentType.getString(context, "target")
                            )
                        })
                )
        )
    }

    @Throws(CommandSyntaxException::class)
    private fun execute(source: ServerCommandSource, player: ServerPlayerEntity, name: String?): Int {
        if (name == "spawn") {
            val overworld: ServerWorld = source.getServer().getOverworld()
            val pos: BlockPos = overworld.getSpawnPos()

            logger.info(String.format("[warp] %s -> %s (%s)", player, name, pos))
            (player as IMixinServerPlayerEntity).setLastLocation(
                SavedLocation(
                    player.getWorld().getRegistryKey().getValue().toString(),
                    player.getX(), player.getY(), player.getZ(), player.getYaw(), player.getPitch()
                )
            )
            player.teleport(overworld, pos.getX(), pos.getY(), pos.getZ(), 0, 0)

            return 0
        }

        val loc: SavedLocation = NekoConfigParsed.warpPoints.get(name)
        if (loc == null) throw INVALID_WARP_POINT_EXCEPTION.create(name)

        val registryKey: net.minecraft.registry.RegistryKey<World?>? =
            net.minecraft.registry.RegistryKey.of<World?>(RegistryKeys.WORLD, net.minecraft.util.Identifier(loc.world))
        val serverWorld: ServerWorld? = source.getServer().getWorld(registryKey)
        if (serverWorld == null) throw INVALID_DIMENSION_EXCEPTION.create(loc.world)

        logger.info(java.lang.String.format("[warp] %s -> %s (%s)", player, name, loc.asFullString()))
        (player as IMixinServerPlayerEntity).setLastLocation(
            SavedLocation(
                player.getWorld().getRegistryKey().getValue().toString(),
                player.getX(), player.getY(), player.getZ(), player.getYaw(), player.getPitch()
            )
        )
        player.teleport(serverWorld, loc.x, loc.y, loc.z, loc.yaw, loc.pitch)
        source.sendFeedback(net.minecraft.text.Text.of("已传送到路径点 $name"), false)

        return 0
    }
}
