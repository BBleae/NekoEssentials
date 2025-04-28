package net.nekocraft.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.CommandSyntaxException
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.Identifier
import net.minecraft.world.World
import net.nekocraft.NekoEssentials.Companion.logger
import net.nekocraft.mixinInterfaces.IMixinServerPlayerEntity
import net.nekocraft.utils.SavedLocation


object ToggleCommand {
    private val playerToGameMode: MutableMap<String?, net.minecraft.world.GameMode?> = HashMap()

    private val INVALID_DIMENSION_EXCEPTION: DynamicCommandExceptionType =
        DynamicCommandExceptionType { id: Any? -> net.minecraft.text.Text.of("invalid home dimension: $id") }

    fun register(dispatcher: CommandDispatcher<ServerCommandSource?>) {
        dispatcher.register(
            CommandManager.literal("toggle").executes { context: CommandContext<ServerCommandSource?>? ->
                context?.source?.let { source ->
                    source.player?.let { player ->
                        execute(
                            source, player
                        )
                    }
                } ?: -1
            })
    }

    @Throws(CommandSyntaxException::class)
    private fun execute(source: ServerCommandSource, player: ServerPlayerEntity): Int {
        if (!player.isSpectator) {
            val loc = SavedLocation(
                player.world.registryKey.value.toString(), player.x, player.y, player.z, player.yaw, player.pitch
            )
            logger.info(java.lang.String.format("[toggle][set] %s -> %s", player, loc.asFullString()))
            (player as IMixinServerPlayerEntity).toggleLocation = loc
            playerToGameMode.put(player.uuidAsString, player.interactionManager.gameMode)
            player.changeGameMode(net.minecraft.world.GameMode.SPECTATOR)
        } else {
            val loc: SavedLocation? = (player as IMixinServerPlayerEntity).toggleLocation
            if (loc != null) {
                val registryKey: RegistryKey<World?>? = RegistryKey.of(RegistryKeys.WORLD, Identifier.of(loc.world))
                val serverWorld: ServerWorld? = source.server.getWorld(registryKey)
                if (serverWorld == null) throw INVALID_DIMENSION_EXCEPTION.create(loc.world)

                logger.info(java.lang.String.format("[toggle][teleport] %s -> %s", player, loc.asFullString()))
                player.teleport(serverWorld, loc.x, loc.y, loc.z, HashSet(), loc.yaw, loc.pitch, false)
            }
            if (playerToGameMode.containsKey(player.uuidAsString) && playerToGameMode[player.uuidAsString] != null) {
                player.changeGameMode(playerToGameMode[player.uuidAsString])
                playerToGameMode.remove(player.uuidAsString)
            } else {
                player.changeGameMode(net.minecraft.world.GameMode.DEFAULT)
            }
        }
        return 0
    }
}
