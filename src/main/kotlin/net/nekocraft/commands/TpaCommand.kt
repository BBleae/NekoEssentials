package net.nekocraft.commands

import com.mojang.brigadier.CommandDispatcher
import net.minecraft.command.argument.EntityArgumentType
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.ClickEvent.RunCommand
import net.minecraft.text.HoverEvent.ShowText
import net.minecraft.text.MutableText
import net.minecraft.text.PlainTextContent.Literal
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.nekocraft.NekoEssentials.Companion.logger
import net.nekocraft.mixinInterfaces.IMixinServerPlayerEntity
import net.nekocraft.utils.TpaRequest

object TpaCommand {
    fun register(dispatcher: CommandDispatcher<ServerCommandSource?>) {
        dispatcher.register(
            CommandManager.literal("tpa").then(CommandManager.argument("target", EntityArgumentType.player()).executes {
                val source = it.source
                val player = it.source.player
                if (source != null && player != null) execute(
                    source, player, EntityArgumentType.getPlayer(it, "target")
                )
                else -1
            })
        )
    }

    private fun execute(source: ServerCommandSource, player: ServerPlayerEntity, target: ServerPlayerEntity): Int {
        val req = TpaRequest(player.server, player.uuid, target.uuid)
        (player as IMixinServerPlayerEntity).requestedTpa(req)
        (target as IMixinServerPlayerEntity).requestTpa(req)
        logger.info(String.format("[tpa][send] %s -> %s", player, target))

        target.sendMessage(
            MutableText.of(Literal("")).append(player.getDisplayName()).append(" 想要传送到你的位置  ").append(
                MutableText.of(Literal("[接受]")).styled { style ->
                    style.withColor(Formatting.AQUA)
                        .withHoverEvent(ShowText(Text.of("/tpaccept " + player.name.string)))
                        .withClickEvent(RunCommand("/tpaccept " + player.name.string))
                }
            ).append("  ").append(
                MutableText.of(Literal("[拒绝]")).styled { style ->
                    style.withColor(Formatting.DARK_AQUA)
                        .withHoverEvent(ShowText(Text.of("/tpadeny " + player.name.string)))
                        .withClickEvent(RunCommand("/tpadeny " + player.name.string))
                }), false
        )
        source.sendFeedback({
            MutableText.of(Literal("已成功向 ")).append(target.getDisplayName()).append(" 发送传送请求")
        }, false)
        return 0
    }
}
