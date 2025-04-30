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
import net.minecraft.text.MutableText
import net.minecraft.text.PlainTextContent.Literal
import net.minecraft.util.Identifier
import net.nekocraft.NekoEssentials.Companion.logger
import net.nekocraft.mixinInterfaces.IMixinServerPlayerEntity
import net.nekocraft.utils.SavedLocation

object BackCommand {
    private val NO_BACK_EXCEPTION: SimpleCommandExceptionType =
        SimpleCommandExceptionType(net.minecraft.text.Text.of("没有返回点可以前往"))
    private val INVALID_DIMENSION_EXCEPTION: DynamicCommandExceptionType =
        DynamicCommandExceptionType { id: Any? -> net.minecraft.text.Text.of("invalid back dimension: $id") }

    fun register(dispatcher: CommandDispatcher<ServerCommandSource?>) {
        dispatcher.register(
            CommandManager.literal("back").executes { context: CommandContext<ServerCommandSource?>? ->
                context?.source?.let { source ->
                    source.player?.let { player -> execute(source, player) }
                } ?: -1
            })
    }

    @Throws(CommandSyntaxException::class)
    private fun execute(source: ServerCommandSource, player: ServerPlayerEntity): Int {
        val loc: SavedLocation? = (player as IMixinServerPlayerEntity).lastLocation
        if (loc == null) throw NO_BACK_EXCEPTION.create()

        val registryKey = RegistryKey.of(RegistryKeys.WORLD, Identifier.of(loc.world))
        val serverWorld: ServerWorld? = source.server.getWorld(registryKey)
        if (serverWorld == null) throw INVALID_DIMENSION_EXCEPTION.create(loc.world)

        logger.info(java.lang.String.format("[back][teleport] %s -> %s", player, loc.asFullString()))
        (player as IMixinServerPlayerEntity).lastLocation =
            SavedLocation(
                player.world.registryKey.value.toString(),
                player.x, player.y, player.z, player.yaw, player.pitch
            )
        player.teleport(serverWorld, loc.x, loc.y, loc.z, HashSet(), loc.yaw, loc.pitch, false)
        source.sendFeedback({ MutableText.of(Literal("已传送到上次传送的位置")) }, false)

        return 0
    }
}
