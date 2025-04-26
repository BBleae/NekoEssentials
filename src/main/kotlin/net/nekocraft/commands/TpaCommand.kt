package net.nekocraft.commands

import net.nekocraft.mixinInterfaces.IMixinServerPlayerEntity

object TpaCommand {
    fun register(dispatcher: CommandDispatcher<ServerCommandSource?>) {
        dispatcher.register(
            CommandManager.literal("tpa")
                .then(
                    CommandManager.argument<EntitySelector?>("target", EntityArgumentType.player())
                        .executes(com.mojang.brigadier.Command { context: CommandContext<ServerCommandSource?>? ->
                            execute(
                                context.getSource(), context.getSource().getPlayer(),
                                EntityArgumentType.getPlayer(context, "target")
                            )
                        })
                )
        )
    }

    private fun execute(source: ServerCommandSource, player: ServerPlayerEntity, target: ServerPlayerEntity): Int {
        val req = TpaRequest(player.getServer(), player.getUuid(), target.getUuid())
        (player as IMixinServerPlayerEntity).requestedTpa(req)
        (target as IMixinServerPlayerEntity).requestTpa(req)
        logger.info(String.format("[tpa][send] %s -> %s", player, target))

        target.sendMessage(
            MutableText.of(LiteralTextContent("[这里]"))
                .append(
                    MutableText.of(LiteralTextContent("[这里]"))
                        .styled(UnaryOperator { style: net.minecraft.text.Style? ->
                            style!!.withColor(net.minecraft.util.Formatting.AQUA)
                                .withHoverEvent(
                                    HoverEvent(
                                        HoverEvent.Action.SHOW_TEXT,
                                        net.minecraft.text.Text.of("/tpaccept " + player.getName().getString())
                                    )
                                )
                                .withClickEvent(
                                    ClickEvent(
                                        ClickEvent.Action.RUN_COMMAND,
                                        "/tpaccept " + player.getName().getString()
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
                                        net.minecraft.text.Text.of("/tpadeny " + player.getName().getString())
                                    )
                                )
                                .withClickEvent(
                                    ClickEvent(
                                        ClickEvent.Action.RUN_COMMAND,
                                        "/tpadeny " + player.getName().getString()
                                    )
                                )
                        })
                )
        )
        source.sendFeedback(MutableText.of(LiteralTextContent("[这里]")), false)
        return 0
    }
}
