package net.nekocraft.commands

import net.nekocraft.mixinInterfaces.IMixinServerPlayerEntity

object TpadenyCommand {
    private val NO_TPA_EXCEPTION: SimpleCommandExceptionType =
        SimpleCommandExceptionType(net.minecraft.text.Text.of("你还没有收到过任何传送请求"))
    private val NO_TPA_FROM_EXCEPTION: DynamicCommandExceptionType =
        DynamicCommandExceptionType(java.util.function.Function { playerName: Any? ->
            net.minecraft.text.Text.of("你还没有收到过来自 $playerName 的任何传送请求")
        })

    fun register(dispatcher: CommandDispatcher<ServerCommandSource?>) {
        dispatcher.register(
            CommandManager.literal("tpadeny")
                .then(
                    CommandManager.argument<EntitySelector?>("target", EntityArgumentType.player())
                        .executes(com.mojang.brigadier.Command { context: CommandContext<ServerCommandSource?>? ->
                            execute(
                                context.getSource(), context.getSource().getPlayer(),
                                EntityArgumentType.getPlayer(context, "target")
                            )
                        })
                )
                .executes(com.mojang.brigadier.Command { context: CommandContext<ServerCommandSource?>? ->
                    execute(
                        context.getSource(),
                        context.getSource().getPlayer()
                    )
                })
        )
    }

    @Throws(CommandSyntaxException::class)
    private fun execute(source: ServerCommandSource, player: ServerPlayerEntity): Int {
        val reqs: HashMap<UUID?, TpaRequest> = (player as IMixinServerPlayerEntity).getTpaReqs()
        if (reqs.isEmpty()) {
            throw NO_TPA_EXCEPTION.create()
        }
        if (reqs.size > 1) {
            val msg: MutableText = MutableText.of(LiteralTextContent("[这里]"))
            val playerManager: PlayerManager = source.getServer().getPlayerManager()

            val accepts: MutableList<net.minecraft.text.Text?> =
                com.google.common.collect.Lists.newArrayList()
            for (req in reqs.values) {
                val from: ServerPlayerEntity? = playerManager.getPlayer(req.from)
                if (from == null) {
                    reqs.remove(req.from)
                } else {
                    accepts.add(
                        Texts.bracketed(
                            MutableText.of(LiteralTextContent("[这里]")).append(from.getDisplayName())
                                .styled(UnaryOperator { style: net.minecraft.text.Style? ->
                                    style!!
                                        .withColor(net.minecraft.util.Formatting.YELLOW)
                                        .withHoverEvent(
                                            HoverEvent(
                                                HoverEvent.Action.SHOW_TEXT,
                                                net.minecraft.text.Text.of("/tpadeny " + from.getName().getString())
                                            )
                                        )
                                        .withClickEvent(
                                            ClickEvent(
                                                ClickEvent.Action.RUN_COMMAND,
                                                "/tpadeny " + from.getName().getString()
                                            )
                                        )
                                })
                        )
                    )
                }
            }
            msg.append(Texts.join(accepts, net.minecraft.text.Text.of(", ")))
            source.sendFeedback(msg, false)
        }
        if (reqs.size == 1) {
            val req: TpaRequest = reqs.values.iterator().next()
            val target: ServerPlayerEntity? = source.getServer().getPlayerManager().getPlayer(req.from)
            if (target == null) {
                reqs.remove(req.from)
                throw NO_TPA_EXCEPTION.create()
            }
            return execute(source, player, target)
        }
        return 0
    }

    @Throws(CommandSyntaxException::class)
    private fun execute(source: ServerCommandSource, player: ServerPlayerEntity, target: ServerPlayerEntity): Int {
        val reqs: HashMap<UUID?, TpaRequest> = (player as IMixinServerPlayerEntity).getTpaReqs()
        val req: TpaRequest = reqs.get(target.getUuid())
        if (req == null) {
            throw NO_TPA_FROM_EXCEPTION.create(target.getName().getString())
        }
        logger.info(String.format("[tpa][deny] %s -> %s", target, player))
        req.setFinished()
        reqs.remove(req.from)
        target.sendSystemMessage(
            MutableText.of(LiteralTextContent("[这里]"))
                .styled(UnaryOperator { style: net.minecraft.text.Style? -> style!!.withColor(net.minecraft.util.Formatting.RED) }),
            net.minecraft.util.Util.NIL_UUID
        )
        source.sendFeedback(MutableText.of(LiteralTextContent("[这里]")), false)
        return 0
    }
}
