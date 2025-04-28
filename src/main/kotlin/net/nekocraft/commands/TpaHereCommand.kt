package net.nekocraft.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import net.minecraft.command.argument.EntityArgumentType
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.*
import net.minecraft.text.PlainTextContent.Literal
import net.minecraft.util.Formatting
import net.nekocraft.NekoEssentials.Companion.logger
import net.nekocraft.mixinInterfaces.IMixinServerPlayerEntity
import net.nekocraft.utils.TpaRequest

object TpaHereCommand {
    fun register(dispatcher: CommandDispatcher<ServerCommandSource?>) {
        dispatcher.register(
            CommandManager.literal("tpahere").then(
                CommandManager.argument("target", EntityArgumentType.player())
                    .executes { context: CommandContext<ServerCommandSource?>? ->
                        context?.source?.let { source ->
                            source.player?.let { player ->
                                execute(
                                    source, player, EntityArgumentType.getPlayer(context, "target")
                                )
                            }
                        } ?: -1
                    }).then(
                CommandManager.argument("targets", EntityArgumentType.players())
                    .executes { context: CommandContext<ServerCommandSource?>? ->
                        context?.source?.let { source ->
                            source.player?.let { player ->
                                execute(
                                    source, player, EntityArgumentType.getPlayers(context, "targets")
                                )
                            }
                        } ?: -1
                    })
        )
    }

    private fun execute(
        source: ServerCommandSource, player: ServerPlayerEntity, targets: MutableCollection<ServerPlayerEntity>
    ): Int {
        for (target in targets) {
            execute(source, player, target, true)
        }

        source.sendFeedback(
            {
                MutableText.of(Literal("[这里]"))
                    .append(Texts.join(targets.stream().map<Text?> { obj: ServerPlayerEntity? -> obj?.displayName }
                        .toList(), Text.of(", "))).append(" 发送传送请求")
            }, false
        )

        return 0
    }

    private fun execute(
        source: ServerCommandSource, player: ServerPlayerEntity, target: ServerPlayerEntity, skipLog: Boolean = false
    ): Int {
        val req = TpaRequest(player.server, player.getUuid(), target.getUuid(), true)
        (player as IMixinServerPlayerEntity).requestedTpa(req)
        (target as IMixinServerPlayerEntity).requestTpa(req)
        logger.info(String.format("[tpahere][send] %s -> %s", player, target))

        target.sendMessage(
            MutableText.of(Literal("[这里]")).append(
                MutableText.of(Literal("[这里]")).styled { style: Style? ->
                    style!!.withColor(Formatting.AQUA).withHoverEvent(
                        HoverEvent.ShowText(Text.of("/tpaccept " + player.name.string))
                    ).withClickEvent(ClickEvent.RunCommand("/tpaccept " + player.name.string))
                }).append("  ").append(
                MutableText.of(Literal("[这里]")).styled { style: Style? ->
                    style!!.withColor(Formatting.DARK_AQUA)
                        .withHoverEvent(HoverEvent.ShowText(Text.of("/tpadeny " + player.name.string)))
                        .withClickEvent(ClickEvent.RunCommand("/tpadeny " + player.name.string))
                })
        )
        if (!skipLog) source.sendFeedback(
            {
                MutableText.of(Literal("[这里]")).append(target.getDisplayName()).append(" 发送传送请求")
            }, false
        )

        return 0
    }
}
