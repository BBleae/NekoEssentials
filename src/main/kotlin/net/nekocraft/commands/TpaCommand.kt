package net.nekocraft.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import net.minecraft.command.argument.EntityArgumentType
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.ClickEvent.RunCommand
import net.minecraft.text.HoverEvent.ShowText
import net.minecraft.text.MutableText
import net.minecraft.text.PlainTextContent.Literal
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.nekocraft.NekoEssentials.Companion.logger
import net.nekocraft.mixinInterfaces.IMixinServerPlayerEntity
import net.nekocraft.utils.TpaRequest

object TpaCommand {
    fun register(dispatcher: CommandDispatcher<ServerCommandSource?>) {
        dispatcher.register(
            CommandManager.literal("tpa").then(
                CommandManager.argument("target", EntityArgumentType.player())
                    .executes { context: CommandContext<ServerCommandSource?>? ->
                        context?.source?.let { source ->
                            context.source?.player?.let { player ->
                                execute(
                                    source, player, EntityArgumentType.getPlayer(context, "target")
                                )
                            }
                        } ?: -1
                    })
        )
    }

    private fun execute(source: ServerCommandSource, player: ServerPlayerEntity, target: ServerPlayerEntity): Int {
        val req = TpaRequest(player.server, player.uuid, target.uuid)
        (player as IMixinServerPlayerEntity).requestedTpa(req)
        (target as IMixinServerPlayerEntity).requestTpa(req)
        logger.info(String.format("[tpa][send] %s -> %s", player, target))

        target.sendMessage(
            MutableText.of(Literal("[这里]")).append(
                MutableText.of(Literal("[这里]")).styled { style: Style? ->
                    style!!.withColor(Formatting.AQUA)
                        .withHoverEvent(ShowText(Text.of("/tpaccept " + player.name.string)))
                        .withClickEvent(RunCommand("/tpaccept " + player.name.string))
                }).append("  ").append(
                MutableText.of(Literal("[这里]")).styled { style: Style? ->
                    style!!.withColor(Formatting.DARK_AQUA).withHoverEvent(
                        ShowText(
                            Text.of("/tpadeny " + player.name.string)
                        )
                    ).withClickEvent(
                        RunCommand(
                            "/tpadeny " + player.name.string
                        )
                    )
                })
        )
        source.sendFeedback(
            { MutableText.of(Literal("[这里]")) }, false
        )
        return 0
    }
}
