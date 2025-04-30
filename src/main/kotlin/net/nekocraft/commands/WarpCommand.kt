package net.nekocraft.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.CommandSyntaxException
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import net.minecraft.command.CommandSource
import net.minecraft.registry.RegistryKeys
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.world.World
import net.nekocraft.NekoEssentials.Companion.logger
import net.nekocraft.config.NekoConfigParsed
import net.nekocraft.mixinInterfaces.IMixinServerPlayerEntity
import net.nekocraft.utils.SavedLocation
import java.util.stream.Collectors
import java.util.stream.Stream

object WarpCommand {
    val INVALID_WARP_POINT_EXCEPTION: DynamicCommandExceptionType =
        DynamicCommandExceptionType { name: Any? -> Text.of("路径点 $name 不存在") }
    private val INVALID_DIMENSION_EXCEPTION: DynamicCommandExceptionType =
        DynamicCommandExceptionType { id: Any? -> Text.of("invalid warp dimension: $id") }

    fun register(dispatcher: CommandDispatcher<ServerCommandSource?>) {
        dispatcher.register(
            CommandManager.literal("warp")
                .then(
                    CommandManager.argument("target", StringArgumentType.word())
                        .suggests { context: CommandContext<ServerCommandSource?>?, builder: SuggestionsBuilder? ->
                            CommandSource.suggestMatching(
                                Stream.concat(
                                    NekoConfigParsed.warpPoints?.keys?.stream() ?: Stream.empty(), Stream.of("spawn")
                                ).collect(Collectors.toSet()), builder
                            )
                        }.executes { context: CommandContext<ServerCommandSource?>? ->
                            context?.source?.let { source ->
                                source.player?.let { player ->
                                    execute(source, player, StringArgumentType.getString(context, "target"))
                                    1 // 显式返回1表示成功执行
                                }
                            } ?: -1
                        })
        )
    }

    @Throws(CommandSyntaxException::class)
    private fun execute(source: ServerCommandSource, player: ServerPlayerEntity, name: String?) {
        if (name == "spawn") {
            val overworld: ServerWorld = source.server.overworld
            val pos = overworld.getSpawnPos().toCenterPos()

            logger.info(String.format("[warp] %s -> %s (%s)", player, name, pos))
            (player as IMixinServerPlayerEntity).lastLocation = SavedLocation(
                player.world.registryKey.value.toString(), player.x, player.y, player.z, player.yaw, player.pitch
            )

            player.teleport(overworld, pos.x, pos.y, pos.z, HashSet(), 0f, 0f, true)
            source.sendFeedback({ Text.of("已传送到出生点") }, false)
            return
        }

        val loc: SavedLocation? = NekoConfigParsed.warpPoints?.get(name)
        if (loc == null) throw INVALID_WARP_POINT_EXCEPTION.create(name)

        val registryKey: net.minecraft.registry.RegistryKey<World?>? =
            net.minecraft.registry.RegistryKey.of(RegistryKeys.WORLD, Identifier.of(loc.world))
        val serverWorld: ServerWorld? = source.server.getWorld(registryKey)
        if (serverWorld == null) throw INVALID_DIMENSION_EXCEPTION.create(loc.world)

        logger.info(java.lang.String.format("[warp] %s -> %s (%s)", player, name, loc.asFullString()))
        (player as IMixinServerPlayerEntity).lastLocation = SavedLocation(
            player.world.registryKey.value.toString(), player.x, player.y, player.z, player.yaw, player.pitch
        )

        player.teleport(serverWorld, loc.x, loc.y, loc.z, HashSet(), loc.yaw, loc.pitch, false)
        source.sendFeedback({ Text.of("已传送到路径点 $name") }, false)

        return
    }
}