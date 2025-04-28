package net.nekocraft.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.MutableText
import net.minecraft.text.PlainTextContent.Literal
import net.nekocraft.NekoEssentials.Companion.logger
import net.nekocraft.mixinInterfaces.IMixinServerPlayerEntity
import net.nekocraft.utils.SavedLocation

object SetHomeCommand {
    fun register(dispatcher: CommandDispatcher<ServerCommandSource?>) {
        dispatcher.register(
            CommandManager.literal("sethome").executes { context: CommandContext<ServerCommandSource?>? ->
                context?.source?.let { source ->
                    source.player?.let { player -> execute(source, player) }
                } ?: -1
            })
    }

    private fun execute(source: ServerCommandSource, player: ServerPlayerEntity): Int {
        val world = player.world.registryKey.value.toString()
        val loc = SavedLocation(
            world, player.x, player.y, player.z, player.yaw, player.pitch
        )
        logger.info(java.lang.String.format("[home][set] %s -> %s", player, loc.asFullString()))
        (player as IMixinServerPlayerEntity).homeLocation = loc
        source.sendFeedback(
            { MutableText.of(Literal("已成功在 " + loc.asString() + " 处设置家")) }, false
        )
        return 0
    }
}
