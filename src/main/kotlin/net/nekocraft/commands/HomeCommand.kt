package net.nekocraft.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.CommandSyntaxException
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
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


object HomeCommand {
    private val NO_HOME_EXCEPTION: SimpleCommandExceptionType =
        SimpleCommandExceptionType(net.minecraft.text.Text.of("你还没有设置过家"))
    private val INVALID_DIMENSION_EXCEPTION: DynamicCommandExceptionType =
        DynamicCommandExceptionType { id: Any? -> net.minecraft.text.Text.of("invalid home dimension: $id") }

    fun register(dispatcher: CommandDispatcher<ServerCommandSource?>) {
        dispatcher.register(
            CommandManager.literal("home").executes { context: CommandContext<ServerCommandSource?>? ->
                context?.source?.let { source ->
                    source.player?.let { player ->
                        execute(source, player)
                    }
                } ?: -1
            })
    }

    @Throws(CommandSyntaxException::class)
    private fun execute(source: ServerCommandSource, player: ServerPlayerEntity): Int {
        val loc: SavedLocation? = (player as IMixinServerPlayerEntity).homeLocation
        if (loc == null) throw NO_HOME_EXCEPTION.create()

        val registryKey: RegistryKey<World?>? = RegistryKey.of(RegistryKeys.WORLD, Identifier.of(loc.world))
        val serverWorld: ServerWorld? = source.server.getWorld(registryKey)
        if (serverWorld == null) throw INVALID_DIMENSION_EXCEPTION.create(loc.world)

        logger.info(java.lang.String.format("[home][teleport] %s -> %s", player, loc.asFullString()))
        (player as IMixinServerPlayerEntity).lastLocation = SavedLocation(
            player.world.registryKey.value.toString(), player.x, player.y, player.z, player.yaw, player.pitch
        )
        player.teleport(serverWorld, loc.x, loc.y, loc.z, HashSet(), loc.yaw, loc.pitch, false)
        source.sendFeedback({ net.minecraft.text.Text.of("已传送到家") }, false)

        return 0
    }
}
