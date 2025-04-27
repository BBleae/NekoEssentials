package net.nekocraft.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import net.minecraft.command.EntitySelector
import net.minecraft.command.argument.EntityArgumentType
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import net.nekocraft.mixinInterfaces.IMixinServerPlayerEntity
import net.minecraft.text.MutableText
import net.minecraft.text.PlainTextContent.Literal

object TpahereCommand {
    fun register(dispatcher: CommandDispatcher<ServerCommandSource?>) {
        dispatcher.register(
            CommandManager.literal("tpahere")
                .then(
                    CommandManager.argument("target", EntityArgumentType.player())
                        .executes { context: CommandContext<ServerCommandSource?>? ->
                            execute(
                                context?.source, context?.source?.player,
                                EntityArgumentType.getPlayer(context, "target")
                            )
                        }
                )
                .then(
                    CommandManager.argument("targets", EntityArgumentType.players())
                        .executes { context: CommandContext<ServerCommandSource?>? ->
                            TpahereCommand.execute(
                                context.source, context?.source?.player,
                                EntityArgumentType.getPlayers(context, "targets")
                            )
                        }
                )
        )
    }

    private fun execute(
        source: ServerCommandSource,
        player: ServerPlayerEntity,
        targets: MutableCollection<ServerPlayerEntity>
    ): Int {
        for (target in targets) {
            execute(source, player, target, true)
        }

        source.sendFeedback(
            MutableText.of(Literal("[这里]"))
                .append(
                    Texts.join(
                        targets.stream()
                            .map<net.minecraft.text.Text?> { obj: ServerPlayerEntity? -> obj.getDisplayName() }
                            .toList(),
                        net.minecraft.text.Text.of(", ")))
                .append(" 发送传送请求"), false)

        return 0
    }

    private fun execute(
        source: ServerCommandSource,
        player: ServerPlayerEntity,
        target: ServerPlayerEntity,
        skipLog: Boolean = false
    ): Int {
        val req = TpaRequest(player.getServer(), player.getUuid(), target.getUuid(), true)
        (player as IMixinServerPlayerEntity).requestedTpa(req)
        (target as IMixinServerPlayerEntity).requestTpa(req)
        logger.info(String.format("[tpahere][send] %s -> %s", player, target))

        target.sendSystemMessage(
            MutableText.of(LiteralTextContent("[这里]"))
                .append(
                    MutableText.of(LiteralTextContent("[这里]"))
                        .styled(UnaryOperator { style: net.minecraft.text.Style? ->
                            style!!.withColor(net.minecraft.util.Formatting.AQUA)
                                .withHoverEvent(
                                    HoverEvent(
                                        HoverEvent.Action.SHOW_TEXT,
                                        net.minecraft.text.Text.of("/tpaccept " + player.name.asString())
                                    )
                                )
                                .withClickEvent(
                                    ClickEvent(
                                        ClickEvent.Action.RUN_COMMAND,
                                        "/tpaccept " + player.name.asString()
                                    )
                                )
                        })
                )
                .append("  ")
                .append(
                    MutableText.of(LiteralTextContent("[这里]"))
                        .styled(UnaryOperator { style: net.minecraft.text.Style? ->
                            style!!.withColor(net.minecraft.util.Formatting.DARK_AQUA)
                                .withHoverEvent(
                                    HoverEvent(
                                        HoverEvent.Action.SHOW_TEXT,
                                        net.minecraft.text.Text.of("/tpadeny " + player.name.asString())
                                    )
                                )
                                .withClickEvent(
                                    ClickEvent(
                                        ClickEvent.Action.RUN_COMMAND,
                                        "/tpadeny " + player.name.asString()
                                    )
                                )
                        })
                ), net.minecraft.util.Util.NIL_UUID
        )
        if (!skipLog) source.sendFeedback(
            MutableText.of(LiteralTextContent("[这里]"))
                .append(target.getDisplayName())
                .append(" 发送传送请求"), false
        )

        return 0
    }
}
